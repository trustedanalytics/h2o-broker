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
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.api.Location;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceBindingServiceStore;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class H2oServiceInstanceBindingService extends ForwardingServiceInstanceBindingServiceStore {

  private final BrokerStore<H2oCredentials> credentialsStore;

  public H2oServiceInstanceBindingService(ServiceInstanceBindingService delegate,
      BrokerStore<H2oCredentials> credentialsStore) {
    super(delegate);
    this.credentialsStore = credentialsStore;
  }

  @Override
  public ServiceInstanceBinding createServiceInstanceBinding(
      CreateServiceInstanceBindingRequest request)
      throws ServiceInstanceBindingExistsException, ServiceBrokerException {

    try {
      return withCredentials(super.createServiceInstanceBinding(request));
    } catch (IOException e) {
      throw new ServiceBrokerException(e);
    }
  }

  private ServiceInstanceBinding withCredentials(ServiceInstanceBinding serviceInstanceBinding)
      throws IOException {
    return new ServiceInstanceBinding(serviceInstanceBinding.getId(),
        serviceInstanceBinding.getServiceInstanceId(),
        getCredentialsFor(serviceInstanceBinding.getServiceInstanceId()),
        serviceInstanceBinding.getSyslogDrainUrl(), serviceInstanceBinding.getAppGuid());
  }

  private Map<String, Object> getCredentialsFor(String serviceInstanceId) throws IOException {
    Optional<H2oCredentials> credentials =
        credentialsStore.getById(Location.newInstance(serviceInstanceId));
    return credentials
        .orElseThrow(() -> new IOException(
            "There are no stored credentials for service instance '" + serviceInstanceId + "'"))
        .toMap();
  }
}
