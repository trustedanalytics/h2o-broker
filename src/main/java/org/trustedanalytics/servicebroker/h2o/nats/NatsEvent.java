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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NatsEvent {

  @JsonProperty("ServiceId")
  private String serviceId;

  @JsonProperty("ServiceName")
  private String serviceName;

  @JsonProperty("ServiceType")
  private String serviceType = "h2o";

  @JsonProperty("OrgGuid")
  private String organizationId;

  @JsonProperty("Message")
  private String message;

  @JsonProperty("Timestamp")
  private long timestamp;

  public NatsEvent(String serviceId, String serviceName, String organizationId,
      String message) {
    this.serviceId = serviceId;
    this.serviceName = serviceName;
    this.organizationId = organizationId;
    this.message = message;
    this.timestamp = System.currentTimeMillis();
  }

}
