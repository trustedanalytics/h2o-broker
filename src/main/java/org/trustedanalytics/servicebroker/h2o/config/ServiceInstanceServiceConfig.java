/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.servicebroker.h2o.config;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.impl.ServiceInstanceServiceStore;
import org.trustedanalytics.hadoop.config.ConfigurationHelper;
import org.trustedanalytics.hadoop.config.ConfigurationHelperImpl;
import org.trustedanalytics.hadoop.config.ConfigurationLocator;
import org.trustedanalytics.servicebroker.h2o.service.H2oProvisioner;
import org.trustedanalytics.servicebroker.h2o.service.H2oProvisionerClient;
import org.trustedanalytics.servicebroker.h2o.service.H2oServiceInstanceService;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oProvisionerRestApi;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oProvisionerRestClient;

import java.io.IOException;
import java.util.Map;

@Configuration
public class ServiceInstanceServiceConfig {

    @Bean
    public ServiceInstanceService getServiceInstanceService(
        BrokerStore<ServiceInstance> serviceInstanceStore, H2oProvisioner h2oProvisioner,
        BrokerStore<H2oCredentials> credentialsStore) {
        return new H2oServiceInstanceService(new ServiceInstanceServiceStore(serviceInstanceStore),
            h2oProvisioner, credentialsStore);
    }

    @Bean
    public H2oProvisioner h2oProvisioner(ExternalConfiguration config,
        H2oProvisionerRestApi h2oProvisionerRestApi) throws IOException {

        return new H2oProvisionerClient(config.getH2oMapperMemory(), config.getH2oMapperNodes(),
            getYarnConf(config), h2oProvisionerRestApi);
    }

    private Map<String, String> getYarnConf(ExternalConfiguration config) throws IOException {
        ConfigurationHelper confHelper = ConfigurationHelperImpl.getInstance();
        return confHelper.getConfigurationFromJson(

            //            System.getenv("VCAP_SERVICES"),

            //TODO: bind it to yarn instance after solving "dollar issue" in yarn-broker
            config.getYarnConfig(),

            ConfigurationLocator.HADOOP);
    }

    @Bean
    @Profile({"cloud", "default"})
    public H2oProvisionerRestApi h2oProvisionerRestApi(ExternalConfiguration config) {
        return new H2oProvisionerRestClient(config.getH2oProvisionerUrl(), new RestTemplate());
    }
}
