/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.trustedanalytics.servicebroker.h2o.service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.api.Location;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceServiceStore;
import org.trustedanalytics.servicebroker.h2o.nats.NatsNotifier;
import org.trustedanalytics.servicebroker.h2o.nats.ServiceMetadata;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;

public class H2oServiceInstanceService extends ForwardingServiceInstanceServiceStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(H2oServiceInstanceService.class);
  private final long provisionerTimeout;
  private final H2oProvisioner h2oProvisioner;
  private final BrokerStore<H2oCredentials> credentialsStore;
  private final NatsNotifier natsNotifier;

  public H2oServiceInstanceService(ServiceInstanceService delegate, H2oProvisioner h2oProvisioner,
      BrokerStore<H2oCredentials> credentialsStore, NatsNotifier natsNotifier,
      long provisionerTimeout) {
    super(delegate);
    this.h2oProvisioner = h2oProvisioner;
    this.credentialsStore = credentialsStore;
    this.natsNotifier = natsNotifier;
    this.provisionerTimeout = provisionerTimeout;
  }

  @Override
  public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request)
      throws ServiceInstanceExistsException, ServiceBrokerException {

    ServiceInstance serviceInstance = super.createServiceInstance(request);
    String instanceId = serviceInstance.getServiceInstanceId();
    // if name not provided use instanceId instead
    String instanceName =
        (String) Optional.ofNullable(request.getParameters().get("name")).orElse(instanceId);

    ServiceMetadata service =
        new ServiceMetadata(instanceId, instanceName, serviceInstance.getOrganizationGuid());
    natsNotifier.notifyServiceCreationStarted(service);

    ProvisioningResult provisioningResult = provisionH2o(instanceId);

    switch (provisioningResult.getStatus()) {
      case SUCCESS:
        natsNotifier.notifyServiceCreationSucceeded(service);
        break;
      case ERROR:
        natsNotifier.notifyServiceCreationStatus(service, "Creating h2o server " + service.getName()
            + " failed. See h2o-broker logs for more information.");
        logAndRethrow(createErrorMessage(service.getId()), provisioningResult.getException());
        break;
      case TIMEOUT:
        String timeoutMessage = createTimeoutMessage(service.getName());
        natsNotifier.notifyServiceCreationStatus(service, timeoutMessage);
        logAndRethrow(timeoutMessage, provisioningResult.getException());
        break;
      default:
        break;
    }
    return serviceInstance;
  }

  @Override
  public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request)
      throws ServiceBrokerException {
    ServiceInstance serviceInstance = super.deleteServiceInstance(request);
    if (serviceInstance == null) {
      LOGGER.warn(
          "Got delete request for non-existing service instance " + request.getServiceInstanceId());
      return null;
    }

    String serviceInstanceId = serviceInstance.getServiceInstanceId();

    String killedJob = h2oProvisioner.deprovisionInstance(serviceInstanceId);
    LOGGER.info("Killed YARN job: " + killedJob + " for H2O instance " + serviceInstanceId);
    return serviceInstance;
  }

  private ProvisioningResult provisionH2o(String instanceId) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    FutureTask<Object> task =
        new FutureTask<>(new ProvisioningJob(h2oProvisioner, credentialsStore, instanceId), null);
    executor.execute(task);

    try {
      task.get(provisionerTimeout, TimeUnit.SECONDS);
      return new ProvisioningResult(ProvisioningStatus.SUCCESS);
    } catch (InterruptedException | ExecutionException e) {
      return new ProvisioningResult(ProvisioningStatus.ERROR, e);
    } catch (TimeoutException e) {
      return new ProvisioningResult(ProvisioningStatus.TIMEOUT, e);
    }
  }

  private void logAndRethrow(String message, Exception exception) throws ServiceBrokerException {
    LOGGER.error(message, exception);
    throw new ServiceBrokerException(message, exception);
  }

  private String createErrorMessage(String serviceId) {
    return "Unable to provision h2o for " + serviceId + ": ";
  }

  private String createTimeoutMessage(String serviceName) {
    return "H2o instance " + serviceName + " hasn't been started in " + provisionerTimeout
        + " seconds. There may not be enough YARN resources available.";
  }

  private static class ProvisioningJob implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisioningJob.class);
    private final H2oProvisioner h2oProvisioner;
    private final BrokerStore<H2oCredentials> credentialsStore;
    private final String instanceId;

    private ProvisioningJob(H2oProvisioner h2oProvisioner,
        BrokerStore<H2oCredentials> credentialsStore, String instanceId) {
      this.h2oProvisioner = h2oProvisioner;
      this.credentialsStore = credentialsStore;
      this.instanceId = instanceId;
    }

    @Override
    public void run() {
      try {
        H2oCredentials credentials = h2oProvisioner.provisionInstance(instanceId);

        saveCredentials(credentials);
        LOGGER.info("Created h2o instance with address '" + credentials.getHostname() + ":"
            + credentials.getPort() + "'");
      } catch (IOException | ServiceBrokerException e) {
        throw new RuntimeException(e);
      }
    }

    private void saveCredentials(H2oCredentials credentials) throws IOException {
      credentialsStore.save(Location.newInstance(instanceId), credentials);
    }
  }
}
