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
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;

import java.io.IOException;

public class H2oServiceInstanceService extends ForwardingServiceInstanceServiceStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(H2oServiceInstanceService.class);
  private final H2oProvisioner h2oProvisioner;
  private final BrokerStore<H2oCredentials> credentialsStore;

  public H2oServiceInstanceService(ServiceInstanceService delegate, H2oProvisioner h2oProvisioner,
      BrokerStore<H2oCredentials> credentialsStore) {
    super(delegate);
    this.h2oProvisioner = h2oProvisioner;
    this.credentialsStore = credentialsStore;
  }

  @Override
  public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request)
      throws ServiceInstanceExistsException, ServiceBrokerException {

    ServiceInstance serviceInstance = super.createServiceInstance(request);
    String instanceId = serviceInstance.getServiceInstanceId();

    new Thread(new ProvisioningJob(h2oProvisioner, credentialsStore, instanceId)).start();

    return serviceInstance;
  }

  @Override
  public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request)
      throws ServiceBrokerException {
    ServiceInstance serviceInstance = super.deleteServiceInstance(request);
    String serviceInstanceId = serviceInstance.getServiceInstanceId();

    String killedJob = h2oProvisioner.deprovisionInstance(serviceInstanceId);
    LOGGER.info("Killed YARN job: " + killedJob + " for H2O instance " + serviceInstanceId
        + ". H2O deleted.");
    return serviceInstance;
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

      } catch (ServiceBrokerException e) {
        logError(e);

        // TODO: this (exception muting) will change after upgrading
        // to 2.7 Broker API which can deal with creating instances asynchronously
      }
    }

    private void saveCredentials(H2oCredentials credentials) {
      try {
        credentialsStore.save(Location.newInstance(instanceId), credentials);
      } catch (IOException e) {
        logError(e);

        // TODO: this (exception muting) will change after upgrading
        // to 2.7 Broker API which can deal with creating instances asynchronously
      }
    }

    private void logError(Exception e) {
      LOGGER.error("Unable to create h2o instance for '" + instanceId + "'", e);
    }
  }
}
