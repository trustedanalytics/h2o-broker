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

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.api.Location;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class H2oServiceInstanceServiceTest {

  private static final String INSTANCE_ID = "instanceId0";

  private H2oServiceInstanceService instanceService;

  @Mock
  private ServiceInstanceService delegateMock;

  @Mock
  private H2oProvisioner h2oProvisioner;

  @Mock
  private BrokerStore<H2oCredentials> credentialsStoreMock;

  @Before
  public void setup() {
    instanceService =
        new H2oServiceInstanceService(delegateMock, h2oProvisioner, credentialsStoreMock);
  }

  @Test
  public void createServiceInstance_provisionerAndStoreWorks_instanceCreated() throws Exception {
    // arrange
    CreateServiceInstanceRequest request =
        CfBrokerRequestsFactory.getCreateInstanceRequest(INSTANCE_ID);
    ServiceInstance expectedInstance = new ServiceInstance(request);
    H2oCredentials expectedCredentials = new H2oCredentials("a", "b", "c", "d");

    when(delegateMock.createServiceInstance(request)).thenReturn(expectedInstance);
    when(h2oProvisioner.provisionInstance(INSTANCE_ID)).thenReturn(expectedCredentials);
    doNothing().when(credentialsStoreMock).save(Location.newInstance(INSTANCE_ID),
        expectedCredentials);

    // act
    ServiceInstance createdInstance = instanceService.createServiceInstance(request);

    // assert
    assertThat(createdInstance, equalTo(expectedInstance));
    verify(delegateMock, timeout(200)).createServiceInstance(request);
    verify(h2oProvisioner, timeout(200)).provisionInstance(INSTANCE_ID);
    verify(credentialsStoreMock, timeout(200)).save(Location.newInstance(INSTANCE_ID),
        expectedCredentials);
  }

  @Test
  public void createServiceInstance_provisionerFails_instanceCreated() throws Exception {
    // arrange
    CreateServiceInstanceRequest request =
        CfBrokerRequestsFactory.getCreateInstanceRequest(INSTANCE_ID);
    ServiceInstance expectedInstance = new ServiceInstance(request);

    when(delegateMock.createServiceInstance(request)).thenReturn(expectedInstance);
    when(h2oProvisioner.provisionInstance(INSTANCE_ID)).thenThrow(new ServiceBrokerException(""));

    // act
    ServiceInstance createdInstance = instanceService.createServiceInstance(request);

    // assert
    assertThat(createdInstance, equalTo(expectedInstance));
    verify(delegateMock, timeout(200)).createServiceInstance(request);
    verify(h2oProvisioner, timeout(200)).provisionInstance(INSTANCE_ID);
  }

  @Test
  public void createServiceInstance_storeFails_instanceCreated() throws Exception {
    // arrange
    CreateServiceInstanceRequest request =
        CfBrokerRequestsFactory.getCreateInstanceRequest(INSTANCE_ID);
    ServiceInstance expectedInstance = new ServiceInstance(request);
    H2oCredentials expectedCredentials = new H2oCredentials("a", "b", "c", "d");

    when(delegateMock.createServiceInstance(request)).thenReturn(expectedInstance);
    when(h2oProvisioner.provisionInstance(INSTANCE_ID)).thenReturn(expectedCredentials);
    doThrow(new IOException()).when(credentialsStoreMock).save(Location.newInstance(INSTANCE_ID),
        expectedCredentials);

    // act
    ServiceInstance createdInstance = instanceService.createServiceInstance(request);

    // assert
    assertThat(createdInstance, equalTo(expectedInstance));
    verify(delegateMock, timeout(200)).createServiceInstance(request);
    verify(h2oProvisioner, timeout(200)).provisionInstance(INSTANCE_ID);
    verify(credentialsStoreMock, timeout(200)).save(Location.newInstance(INSTANCE_ID),
        expectedCredentials);
  }
}
