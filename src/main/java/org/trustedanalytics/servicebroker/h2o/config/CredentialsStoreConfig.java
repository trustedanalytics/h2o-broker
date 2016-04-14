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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.serialization.JSONSerDeFactory;
import org.trustedanalytics.cfbroker.store.serialization.RepositoryDeserializer;
import org.trustedanalytics.cfbroker.store.serialization.RepositorySerializer;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperStore;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;

import java.io.IOException;

@Configuration
public class CredentialsStoreConfig {

  @Autowired
  private ZookeeperClient credentialsZKClient;

  @Bean
  public BrokerStore<H2oCredentials> credentialsStore(
      RepositorySerializer<H2oCredentials> h2oSerializer,
      RepositoryDeserializer<H2oCredentials> h2oDeserializer) throws IOException {
    return new ZookeeperStore<>(credentialsZKClient, h2oSerializer, h2oDeserializer);
  }

  @Bean
  public RepositorySerializer<H2oCredentials> credentialsSerializer() {
    return JSONSerDeFactory.getInstance().getSerializer();
  }

  @Bean
  public RepositoryDeserializer<H2oCredentials> credentialsDeserializer() {
    return JSONSerDeFactory.getInstance().getDeserializer(H2oCredentials.class);
  }
}
