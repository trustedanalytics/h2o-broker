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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.servicebroker.h2o.config.ExternalConfiguration;

@Configuration
@Profile("test")
public class TestZookeeperServerConfig {

  @Autowired
  private ExternalConfiguration config;

  @Bean
  public TestingServer zkServer() throws Exception {
    return initEmbeddedZKServer();
  }

  private TestingServer initEmbeddedZKServer() throws Exception {
    TestingServer zkServer = new TestingServer();
    zkServer.start();
    createZnode(zkServer, config.getZookeeperMetadataNode());
    createZnode(zkServer, config.getZookeeperCredentialsNode());
    return zkServer;
  }

  private void createZnode(TestingServer zkServer, String node) throws Exception {
    CuratorFramework tempClient = CuratorFrameworkFactory.builder()
        .connectString(zkServer.getConnectString()).retryPolicy(new RetryOneTime(100)).build();
    tempClient.start();

    tempClient.create().creatingParentsIfNeeded().forPath(node);

    tempClient.close();
  }
}
