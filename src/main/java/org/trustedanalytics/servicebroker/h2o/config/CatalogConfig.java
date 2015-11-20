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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.DashboardClient;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
public class CatalogConfig {

    @Autowired
    private ExternalConfiguration configuration;

    @Bean
    public Catalog catalog() {
        final String SERVICE_ID = configuration.getCfServiceId();
        final String SERVICE_NAME = configuration.getCfServiceName();
        final String DESCRIPTION = "A simple h2o broker";
        final boolean BINDABLE = true;
        final boolean UPDATEABLE = true;
        final List<String> NO_TAGS = null;
        final String SYSLOG_DRAIN = "syslog_drain";
        final DashboardClient NO_DASHBOARD = null;

        return new Catalog(Arrays.asList(
            new ServiceDefinition(SERVICE_ID, SERVICE_NAME, DESCRIPTION, BINDABLE, UPDATEABLE,
                getSharedPlans(), NO_TAGS, getServiceDefinitionMetadata(),
                Arrays.asList(SYSLOG_DRAIN), NO_DASHBOARD)));
    }

    private List<Plan> getSharedPlans() {
        final String ID = configuration.getCfBaseId() + "-shared-plan";
        final String NAME = "shared";
        final String DESCRIPTION = "This is a default h2o plan.";
        final Map<String, Object> NO_METADATA = null;
        final boolean FREE_PLAN = true;

        return Lists.newArrayList(
            new Plan(ID, NAME, DESCRIPTION, NO_METADATA, FREE_PLAN));
    }

    private Map<String, Object> getServiceDefinitionMetadata() {
        final String IMAGE_URL_KEY = "imageUrl";
        return ImmutableMap.of(IMAGE_URL_KEY, configuration.getImageUrl());
    }
}
