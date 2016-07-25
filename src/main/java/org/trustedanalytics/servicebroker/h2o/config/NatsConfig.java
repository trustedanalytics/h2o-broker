/**
 * Copyright (c) 2016 Intel Corporation
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.servicebroker.h2o.nats.NatsMessageBuilder;
import org.trustedanalytics.servicebroker.h2o.nats.NatsNotifier;

import nats.client.Nats;
import nats.client.NatsConnector;

@Configuration
@Profile({"cloud", "default"})
public class NatsConfig {
  
  @Bean
  public Nats natsClient(ExternalConfiguration config) {
    return new NatsConnector().addHost(config.getNatsUrl()).connect();
  }

  @Bean
  public String natsServiceCreationTopic(ExternalConfiguration config) {
    return config.getNatsServiceCreationTopic();
  }
  
  @Bean
  public NatsNotifier natsNotifier(Nats natsClient, String natsServiceCreationTopic) {
    return new NatsNotifier(natsClient, natsServiceCreationTopic, new NatsMessageBuilder());
  }
}
