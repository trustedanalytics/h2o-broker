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

package org.trustedanalytics.servicebroker.h2o.service;

import com.google.common.collect.ImmutableMap;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oProvisionerRestApi;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class H2oProvisionerClientTest {

  private static final String H2O_MEMORY = "h2oMemory";
  private static final String H2O_NODES = "h2oNodes";
  private static final boolean KERBEROS = true;
  private static final String INSTANCE_ID = "instanceId";
  private static final Map<String, String> YARN_CONF =
      ImmutableMap.of("key1", "value1", "key2", "value2");

  private H2oProvisioner h2oProvisioner;

  @Mock
  private H2oProvisionerRestApi h2oRestMock;

  @Before
  public void setup() {
    h2oProvisioner =
        new H2oProvisionerClient(H2O_MEMORY, H2O_NODES, KERBEROS, YARN_CONF, h2oRestMock);
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void provisionInstance_provisionerEndsWithStatus200_credentialsReturned()
      throws Exception {

    // arrange
    H2oCredentials expectedCredentials = new H2oCredentials("a", "b", "c", "d");
    when(h2oRestMock.createH2oInstance(INSTANCE_ID, H2O_NODES, H2O_MEMORY, KERBEROS, YARN_CONF))
        .thenReturn(new ResponseEntity<>(expectedCredentials, HttpStatus.OK));

    // act
    H2oCredentials actualCredentials = h2oProvisioner.provisionInstance(INSTANCE_ID);

    // assert
    assertThat(actualCredentials, equalTo(expectedCredentials));
  }

  @Test
  public void provisionInstance_provisionerEndsWithStatus500_exceptionThrown() throws Exception {
    // arrange
    expectedException.expect(ServiceBrokerException.class);
    expectedException.expectMessage("Unable to provision h2o for: " + INSTANCE_ID);
    when(h2oRestMock.createH2oInstance(INSTANCE_ID, H2O_NODES, H2O_MEMORY, KERBEROS, YARN_CONF))
        .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    // act
    h2oProvisioner.provisionInstance(INSTANCE_ID);
  }

  @Test
  public void provisionInstance_provisionerEndsWithException_exceptionRethrown() throws Exception {
    // arrange
    expectedException.expect(ServiceBrokerException.class);
    expectedException.expectMessage("Unable to provision h2o for: " + INSTANCE_ID);
    when(h2oRestMock.createH2oInstance(INSTANCE_ID, H2O_NODES, H2O_MEMORY, KERBEROS, YARN_CONF))
        .thenThrow(new RestClientException(""));

    // act
    h2oProvisioner.provisionInstance(INSTANCE_ID);
  }

  @Test
  public void deprovisionInstance_provisionerEndsWith200_jobIdReturned() throws Exception {
    // arrange
    String expectedJobId = "0295513498943";
    when(h2oRestMock.deleteH2oInstance(INSTANCE_ID, YARN_CONF))
        .thenReturn(new ResponseEntity<>(expectedJobId, HttpStatus.OK));

    // act
    String actualJobId = h2oProvisioner.deprovisionInstance(INSTANCE_ID);

    // assert
    assertThat(actualJobId, equalTo(expectedJobId));
  }

  @Test
  public void deprovisionInstance_provisionerEndsWith500_exceptionThrown() throws Exception {
    // arrange
    expectedException.expect(ServiceBrokerException.class);
    expectedException.expectMessage("Unable to deprovision h2o for: " + INSTANCE_ID);
    when(h2oRestMock.deleteH2oInstance(INSTANCE_ID, YARN_CONF))
        .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

    // act
    h2oProvisioner.deprovisionInstance(INSTANCE_ID);
  }

  @Test
  public void deprovisionInstance_provisionerEndsWithException_exceptionThrown() throws Exception {
    // arrange
    expectedException.expect(ServiceBrokerException.class);
    expectedException.expectMessage("Unable to deprovision h2o for: " + INSTANCE_ID);
    when(h2oRestMock.deleteH2oInstance(INSTANCE_ID, YARN_CONF))
        .thenThrow(new RestClientException(""));

    // act
    h2oProvisioner.deprovisionInstance(INSTANCE_ID);
  }
}

