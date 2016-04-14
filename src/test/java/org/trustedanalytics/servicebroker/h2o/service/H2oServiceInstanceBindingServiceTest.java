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
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.api.Location;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class H2oServiceInstanceBindingServiceTest {

  private static final String INSTANCE_ID = "instanceId";
  private static final String BINDING_ID = "bindingId";
  private static final String SYSLOG = "syslog";
  private static final String APP_GUID = "appGuid";

  private H2oServiceInstanceBindingService bindingService;

  @Mock
  private ServiceInstanceBindingService delegateMock;

  @Mock
  private BrokerStore<H2oCredentials> credentialsStoreMock;

  @Before
  public void setup() {
    bindingService = new H2oServiceInstanceBindingService(delegateMock, credentialsStoreMock);
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void createServiceInstanceBinding_credentialsInStore_bindingCreated() throws Exception {
    // arrange
    CreateServiceInstanceBindingRequest request =
        CfBrokerRequestsFactory.getCreateServiceBindingRequest(INSTANCE_ID, BINDING_ID);
    when(delegateMock.createServiceInstanceBinding(request))
        .thenReturn(new ServiceInstanceBinding(BINDING_ID, INSTANCE_ID, null, SYSLOG, APP_GUID));

    H2oCredentials expectedCredentials = new H2oCredentials("a", "b", "c", "d");
    when(credentialsStoreMock.getById(Location.newInstance(INSTANCE_ID)))
        .thenReturn(Optional.of(expectedCredentials));

    // act
    ServiceInstanceBinding createdBinding = bindingService.createServiceInstanceBinding(request);

    // assert
    assertThat(createdBinding.getId(), equalTo(BINDING_ID));
    assertThat(createdBinding.getServiceInstanceId(), equalTo(INSTANCE_ID));
    assertThat(createdBinding.getSyslogDrainUrl(), equalTo(SYSLOG));
    assertThat(createdBinding.getAppGuid(), equalTo(APP_GUID));
    assertThat(createdBinding.getCredentials(), equalTo(expectedCredentials.toMap()));
  }

  @Test
  public void createServiceInstanceBinding_credentialsNotExistInStore_exceptionThrown()
      throws Exception {

    // arrange
    expectedException.expect(ServiceBrokerException.class);
    expectedException.expectMessage(
        "There are no stored credentials for service instance '" + INSTANCE_ID + "'");

    CreateServiceInstanceBindingRequest request =
        CfBrokerRequestsFactory.getCreateServiceBindingRequest(INSTANCE_ID, BINDING_ID);
    when(delegateMock.createServiceInstanceBinding(request))
        .thenReturn(new ServiceInstanceBinding(BINDING_ID, INSTANCE_ID, null, SYSLOG, APP_GUID));

    when(credentialsStoreMock.getById(Location.newInstance(INSTANCE_ID)))
        .thenReturn(Optional.ofNullable(null));

    // act
    bindingService.createServiceInstanceBinding(request);
  }

  @Test
  public void createServiceInstanceBinding_credentialsStoreError_exceptionThrown()
      throws Exception {

    // arrange
    expectedException.expect(ServiceBrokerException.class);
    expectedException.expectMessage("Error while reading from store.");

    CreateServiceInstanceBindingRequest request =
        CfBrokerRequestsFactory.getCreateServiceBindingRequest(INSTANCE_ID, BINDING_ID);
    when(delegateMock.createServiceInstanceBinding(request))
        .thenReturn(new ServiceInstanceBinding(BINDING_ID, INSTANCE_ID, null, SYSLOG, APP_GUID));

    when(credentialsStoreMock.getById(Location.newInstance(INSTANCE_ID)))
        .thenThrow(new IOException("Error while reading from store."));

    // act
    bindingService.createServiceInstanceBinding(request);
  }
}
