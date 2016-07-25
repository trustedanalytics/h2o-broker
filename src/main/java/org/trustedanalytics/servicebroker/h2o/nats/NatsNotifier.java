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
package org.trustedanalytics.servicebroker.h2o.nats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import nats.client.Nats;

public class NatsNotifier {

  private static final Logger LOGGER = LoggerFactory.getLogger(NatsNotifier.class);

  private Nats natsClient;

  private String serviceCreationTopic;
  
  private NatsMessageBuilder messageBuilder;

  public NatsNotifier(Nats natsClient, String serviceCreationTopic, NatsMessageBuilder messageBuilder) {
    this.natsClient = natsClient;
    this.serviceCreationTopic = serviceCreationTopic;
    this.messageBuilder = messageBuilder;
  }

  public void notifyServiceCreationStarted(ServiceMetadata service) {
    String message = "Started creating h2o instance " + service.getName() + " with instanceId "
        + service.getId();
    notifyServiceCreationStatus(service, message);
  }

  public void notifyServiceCreationSucceeded(ServiceMetadata service) {
    String message = "H2o instance " + service.getName() + " created successfully.";
    notifyServiceCreationStatus(service, message);
  }

  public void notifyServiceCreationStatus(ServiceMetadata service, String message) {
    notifyNats(
        new NatsEvent(service.getId(), service.getName(), service.getOrganizationGuid(), message));
  }

  private void notifyNats(NatsEvent event) {
    String message;
    try {
      message = messageBuilder.buildMessage(event);
      LOGGER.info("Sending message to nats: " + message);
      natsClient.publish(serviceCreationTopic, message);
    } catch (JsonProcessingException e) {
      LOGGER.warn("Unable to create nats message. Nats was not notified.", e);
    }
  }
}
