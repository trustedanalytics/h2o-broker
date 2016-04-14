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

package org.trustedanalytics.servicebroker.h2o.integration;

import org.apache.curator.test.TestingServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClientBuilder;
import org.trustedanalytics.servicebroker.h2o.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oProvisionerRestApi;

import java.io.IOException;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class TestConfig {

  @Autowired
  private ExternalConfiguration config;

  @Autowired
  private TestingServer zkServer;

  @Bean(initMethod = "init", destroyMethod = "destroy")
  public ZookeeperClient brokerZKClient() throws Exception {
    return getZookeeperClient(zkServer.getConnectString(), config.getZookeeperMetadataNode());
  }

  @Bean(initMethod = "init", destroyMethod = "destroy")
  public ZookeeperClient credentialsZKClient() throws Exception {
    return getZookeeperClient(zkServer.getConnectString(), config.getZookeeperCredentialsNode());
  }

  private ZookeeperClient getZookeeperClient(String connectionString, String rootNode)
      throws IOException {
    return new ZookeeperClientBuilder(connectionString, "user", "password", rootNode).build();
  }

  @Bean
  public H2oProvisionerRestApi h2oProvisionerRestApi() {
    return mock(H2oProvisionerRestApi.class);
  }

  @Bean
  public boolean isKerberosEnabled() {
    return true;
  }
}
