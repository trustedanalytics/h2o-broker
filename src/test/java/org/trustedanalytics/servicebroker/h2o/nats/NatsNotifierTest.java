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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nats.client.Nats;

@RunWith(MockitoJUnitRunner.class)
public class NatsNotifierTest {

  @Mock
  private Nats natsClientMock;

  private String natsTopicMock = "aksjfasiouagjk";

  private ServiceMetadata testService = new ServiceMetadata("test-id", "test-name", "test-org");

  private NatsNotifier notifierSUT;
  
  @Mock
  private NatsMessageBuilder messageBuilderMock;

  @Before
  public void setUp() {
    notifierSUT = new NatsNotifier(natsClientMock, natsTopicMock, new NatsMessageBuilder());
    testService.hashCode();
  }

  @Test
  public void notifyServiceCreationStarted_allSubsequentCallsOccured() throws Exception {
    // given
    String expectedMessageStart = "Started creating h2o instance " + testService.getName()
        + " with instanceId " + testService.getId();

    // when
    notifierSUT.notifyServiceCreationStarted(testService);

    // then
    ArgumentCaptor<String> natsNotificationCaptor = ArgumentCaptor.forClass(String.class);
    verify(natsClientMock).publish(same(natsTopicMock), natsNotificationCaptor.capture());
    verifyNatsMessage(natsNotificationCaptor.getValue(), expectedMessageStart);
  }

  @Test
  public void notifyServiceCreationSucceeded_allSubsequentCallsOccured() throws Exception {
    // given
    String expectedMessageStart =
        "H2o instance " + testService.getName() + " created successfully.";

    // when
    notifierSUT.notifyServiceCreationSucceeded(testService);

    // then
    ArgumentCaptor<String> natsNotificationCaptor = ArgumentCaptor.forClass(String.class);
    verify(natsClientMock).publish(same(natsTopicMock), natsNotificationCaptor.capture());
    verifyNatsMessage(natsNotificationCaptor.getValue(), expectedMessageStart);
  }

  @Test
  public void notifyServiceCreationStatus_allSubsequentCallsOccured() throws Exception {
    // given
    String testMessage = "Some message";

    // when
    notifierSUT.notifyServiceCreationStatus(testService, testMessage);

    // then
    ArgumentCaptor<String> natsNotificationCaptor = ArgumentCaptor.forClass(String.class);
    verify(natsClientMock).publish(same(natsTopicMock), natsNotificationCaptor.capture());
    verifyNatsMessage(natsNotificationCaptor.getValue(), testMessage);
  }
  
  @Test
  public void anyNotification_JsonProcessingExceptionOccured_NatsClientNotCalled() throws Exception {
    // given
    NatsMessageBuilder messageBuilderMock = mock(NatsMessageBuilder.class);
    NatsNotifier notifierSUT = new NatsNotifier(natsClientMock, natsTopicMock, messageBuilderMock);  
    
    // when
    when(messageBuilderMock.buildMessage(any())).thenThrow(new JsonGenerationException(""));
    notifierSUT.notifyServiceCreationStarted(testService);

    // then
    verifyZeroInteractions(natsClientMock);
  }
  
  private void verifyNatsMessage(String natsNotification, String expectedMessage)
      throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper jsonMapper = new ObjectMapper();
    NatsEvent actualNatsEvent = jsonMapper.readValue(natsNotification, NatsEvent.class);
    NatsEvent expectedNatsEvent = new NatsEvent(testService.getId(), testService.getName(),
        testService.getOrganizationGuid(), expectedMessage);

    assertThat(expectedNatsEvent.getServiceId(), equalTo(actualNatsEvent.getServiceId()));
    assertThat(expectedNatsEvent.getOrganizationId(), equalTo(actualNatsEvent.getOrganizationId()));
    assertThat(expectedNatsEvent.getServiceName(), equalTo(actualNatsEvent.getServiceName()));
    assertThat(actualNatsEvent.getMessage(), containsString(expectedMessage));
  }

}
