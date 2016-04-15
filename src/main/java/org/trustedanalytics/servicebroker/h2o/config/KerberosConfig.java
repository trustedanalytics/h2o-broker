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
import org.trustedanalytics.hadoop.config.ConfigurationHelperImpl;
import org.trustedanalytics.hadoop.config.PropertyLocator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"cloud", "default"})
public class KerberosConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(KerberosConfig.class);
  private static final String DEFAULT_VALUE = "";
  
  private final ConfigurationHelper confHelper;

  public KerberosConfig() {
    confHelper = ConfigurationHelperImpl.getInstance();
  }

  @VisibleForTesting
  KerberosConfig(ConfigurationHelper confHelper) {
    this.confHelper = confHelper;
  }

  @Bean
  public boolean isKerberosEnabled() {
    String kdc = getProperty(PropertyLocator.KRB_KDC);
    String realm = getProperty(PropertyLocator.KRB_REALM);
    String user = getProperty(PropertyLocator.USER);
    String password = getProperty(PropertyLocator.PASSWORD);
    return !(Strings.isNullOrEmpty(kdc) || Strings.isNullOrEmpty(realm)
        || Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(password));
  }

  private String getProperty(PropertyLocator property) {
    try {
      return confHelper.getPropertyFromEnv(property).orElseGet(() -> DEFAULT_VALUE);
    } catch (Exception e) {
      LOGGER.warn("Problem while getting env property: " + property, e);
      return DEFAULT_VALUE;
    }
  }
}
