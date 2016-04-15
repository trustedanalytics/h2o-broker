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

package org.trustedanalytics.servicebroker.h2o.config;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.impl.ServiceInstanceBindingServiceStore;
import org.trustedanalytics.servicebroker.h2o.service.H2oServiceInstanceBindingService;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;

@Configuration
public class ServiceInstanceBindingServiceConfig {

  @Bean
  public ServiceInstanceBindingService getServiceInstanceBindingService(
      BrokerStore<CreateServiceInstanceBindingRequest> serviceBindingStore,
      BrokerStore<H2oCredentials> credentialsStore) {

    return new H2oServiceInstanceBindingService(
        new ServiceInstanceBindingServiceStore(serviceBindingStore), credentialsStore);
  }
}
