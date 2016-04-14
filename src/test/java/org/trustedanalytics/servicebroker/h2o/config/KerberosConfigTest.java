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

import org.trustedanalytics.hadoop.config.ConfigurationHelper;
import org.trustedanalytics.hadoop.config.PropertyLocator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KerberosConfigTest {

  @Mock
  private ConfigurationHelper configurationHelper;

  private KerberosConfig kerberosConfig;

  @Before
  public void setup() {
    kerberosConfig = new KerberosConfig(configurationHelper);
  }

  @Test
  public void isKerberosEnabled_allPropertiesSet_returnsTrue() throws Exception {
    arrangeTestEnvironment("kdc", "realm", "user", "password");
    boolean kerberosEnabled = kerberosConfig.isKerberosEnabled();
    assertThat(kerberosEnabled, is(true));
  }

  @Test
  public void isKerberosEnabled_kdcEmpty_returnsFalse() throws Exception {
    arrangeTestEnvironment("", "realm", "user", "password");
    boolean kerberosEnabled = kerberosConfig.isKerberosEnabled();
    assertThat(kerberosEnabled, is(false));
  }

  @Test
  public void isKerberosEnabled_realmEmpty_returnsFalse() throws Exception {
    arrangeTestEnvironment("kdc", "", "user", "password");
    boolean kerberosEnabled = kerberosConfig.isKerberosEnabled();
    assertThat(kerberosEnabled, is(false));
  }

  @Test
  public void isKerberosEnabled_userEmpty_returnsFalse() throws Exception {
    arrangeTestEnvironment("kdc", "realm", "", "password");
    boolean kerberosEnabled = kerberosConfig.isKerberosEnabled();
    assertThat(kerberosEnabled, is(false));
  }

  @Test
  public void isKerberosEnabled_passwordEmpty_returnsFalse() throws Exception {
    arrangeTestEnvironment("kdc", "realm", "user", "");
    boolean kerberosEnabled = kerberosConfig.isKerberosEnabled();
    assertThat(kerberosEnabled, is(false));
  }

  @Test
  public void isKerberosEnabled_allPropertiesAreEmptyOptionals_returnsFalse() throws Exception {
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.KRB_KDC))
        .thenReturn(Optional.empty());
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.KRB_REALM))
        .thenReturn(Optional.empty());
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.USER)).thenReturn(Optional.empty());
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.PASSWORD))
        .thenReturn(Optional.empty());
    boolean kerberosEnabled = kerberosConfig.isKerberosEnabled();
    assertThat(kerberosEnabled, is(false));
  }

  @Test
  public void isKerberosEnabled_allPropertiesReadThrowException_returnsFalse() throws Exception {
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.KRB_KDC))
        .thenThrow(new IOException());
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.KRB_REALM))
        .thenThrow(new IOException());
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.USER)).thenThrow(new IOException());
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.PASSWORD))
        .thenThrow(new IOException());
    boolean kerberosEnabled = kerberosConfig.isKerberosEnabled();
    assertThat(kerberosEnabled, is(false));
  }

  private void arrangeTestEnvironment(String kdc, String realm, String user, String password)
      throws IOException {
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.KRB_KDC))
        .thenReturn(Optional.of(kdc));
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.KRB_REALM))
        .thenReturn(Optional.of(realm));
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.USER))
        .thenReturn(Optional.of(user));
    when(configurationHelper.getPropertyFromEnv(PropertyLocator.PASSWORD))
        .thenReturn(Optional.of(password));
  }
}
