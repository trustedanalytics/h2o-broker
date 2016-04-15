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
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.serialization.JSONSerDeFactory;
import org.trustedanalytics.cfbroker.store.serialization.RepositoryDeserializer;
import org.trustedanalytics.cfbroker.store.serialization.RepositorySerializer;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperStore;

import java.io.IOException;

@Configuration
public class BrokerStoreConfig {

  @Autowired
  private ZookeeperClient brokerZKClient;

  @Bean
  public BrokerStore<ServiceInstance> serviceInstanceStore(
      RepositorySerializer<ServiceInstance> instanceSerializer,
      RepositoryDeserializer<ServiceInstance> instanceDeserializer) throws IOException {
    return new ZookeeperStore<>(brokerZKClient, instanceSerializer, instanceDeserializer);
  }

  @Bean
  public BrokerStore<CreateServiceInstanceBindingRequest> serviceBindingStore(
      RepositorySerializer<CreateServiceInstanceBindingRequest> bindingSerializer,
      RepositoryDeserializer<CreateServiceInstanceBindingRequest> bindingDeserializer)
      throws IOException {
    return new ZookeeperStore<>(brokerZKClient, bindingSerializer, bindingDeserializer);
  }

  @Bean
  public RepositorySerializer<ServiceInstance> instanceSerializer() {
    return JSONSerDeFactory.getInstance().getSerializer();
  }

  @Bean
  public RepositorySerializer<CreateServiceInstanceBindingRequest> bindingSerializer() {
    return JSONSerDeFactory.getInstance().getSerializer();
  }

  @Bean
  public RepositoryDeserializer<ServiceInstance> instanceDeserializer() {
    return JSONSerDeFactory.getInstance().getDeserializer(ServiceInstance.class);
  }

  @Bean
  public RepositoryDeserializer<CreateServiceInstanceBindingRequest> bindingDeserializer() {
    return JSONSerDeFactory.getInstance()
        .getDeserializer(CreateServiceInstanceBindingRequest.class);
  }
}
