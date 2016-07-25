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

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class ExternalConfiguration {

  @Value("${cf.serviceid}")
  @NotNull
  private String cfServiceId;

  @Value("${cf.servicename}")
  @NotNull
  private String cfServiceName;

  @Value("${cf.baseid}")
  @NotNull
  private String cfBaseId;

  @Value("${zookeeper.metadataNode}")
  @NotNull
  private String zookeeperMetadataNode;

  @Value("${zookeeper.credentialsNode}")
  @NotNull
  private String zookeeperCredentialsNode;

  @Value("${h2o.provisioner.url}")
  @NotNull
  private String h2oProvisionerUrl;

  @Value("${h2o.provisioner.memory}")
  @NotNull
  private String h2oMapperMemory;

  @Value("${h2o.provisioner.nodes}")
  @NotNull
  private String h2oMapperNodes;
  
  @Value("${h2o.provisioner.timeout}")
  @NotNull
  private String provisionerTimeout;

  @Value("${metadata.imageUrl}")
  @NotNull
  private String imageUrl;

  @Value("${yarn.config}")
  @NotNull
  private String yarnConfig;
  
  @Value("${nats.url}")
  @NotNull
  private String natsUrl;
  
  @Value("${nats.serviceCreationTopic}")
  @NotNull
  private String natsServiceCreationTopic;
}
