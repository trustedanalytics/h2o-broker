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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClientBuilder;
import org.trustedanalytics.hadoop.config.ConfigurationHelper;
import org.trustedanalytics.hadoop.config.ConfigurationHelperImpl;
import org.trustedanalytics.hadoop.config.PropertyLocator;
import org.trustedanalytics.servicebroker.h2o.helper.ZookeeperNodeOperations;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.NoSuchElementException;

@Configuration
@Profile({"cloud", "default"})
public class ZookeeperConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfig.class);

  private String zkUri;
  private String zkUser;
  private String zkPass;
  private String zkBaseNode;
  private String zkBrokerNode;
  private String zkCredentialsNode;

  private ConfigurationHelper confHelper = ConfigurationHelperImpl.getInstance();

  @Autowired
  private ExternalConfiguration config;

  @PostConstruct
  public void postConstruct() throws IOException {
    zkUri = getPropertyFromCredentials(PropertyLocator.ZOOKEPER_URI);
    zkUser = getPropertyFromCredentials(PropertyLocator.USER);
    zkPass = getPropertyFromCredentials(PropertyLocator.PASSWORD);
    zkBaseNode = getPropertyFromCredentials(PropertyLocator.ZOOKEPER_ZNODE);
    zkBrokerNode = config.getZookeeperMetadataNode();
    zkCredentialsNode = config.getZookeeperCredentialsNode();
  }

  @Bean
  public ZookeeperClient brokerZKClient() throws Exception {
    LOGGER.info("Creating zkClient with zkUri='{}' zkNode='{}'", zkUri, zkBrokerNode);
    return getZookeeperClient(zkBrokerNode);
  }

  @Bean
  public ZookeeperClient credentialsZKClient() throws Exception {
    LOGGER.info("Creating zkClient with zkUri='{}' zkNode='{}'", zkUri, zkCredentialsNode);
    return getZookeeperClient(zkCredentialsNode);
  }

  private ZookeeperClient getZookeeperClient(String node) throws Exception {
    String absoluteNode = zkBaseNode + node;
    createIfNotExist(zkUri, absoluteNode);
    ZookeeperClient zkClient =
        new ZookeeperClientBuilder(zkUri, zkUser, zkPass, absoluteNode).build();
    zkClient.init();
    return zkClient;
  }

  private void createIfNotExist(String zkUri, String absoluteNode) throws Exception {
    if (!ZookeeperNodeOperations.checkExists(zkUri, absoluteNode)) {
      ZookeeperNodeOperations.createNode(zkUri, absoluteNode);
    }
  }

  private String getPropertyFromCredentials(PropertyLocator property) throws IOException {
    return confHelper.getPropertyFromEnv(property).orElseThrow(
        () -> new NoSuchElementException(property.name() + " not found in VCAP_SERVICES"));
  }
}
