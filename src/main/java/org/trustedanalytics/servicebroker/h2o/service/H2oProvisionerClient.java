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

package org.trustedanalytics.servicebroker.h2o.service;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oProvisionerRestApi;

import java.util.Map;

public class H2oProvisionerClient implements H2oProvisioner {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2oProvisionerClient.class);

    private final String memory;
    private final String nodesCount;
    private final Map<String, String> yarnConf;
    private final H2oProvisionerRestApi h2oRest;

    public H2oProvisionerClient(String memory, String nodesCount, Map<String, String> yarnConf,
        H2oProvisionerRestApi h2oRest) {
        this.memory = memory;
        this.nodesCount = nodesCount;
        this.yarnConf = yarnConf;
        this.h2oRest = h2oRest;

        LOGGER.info("YARN CONFIG");
        yarnConf.forEach((k,v) -> LOGGER.info(k + ": " + v));
    }

    @Override public H2oCredentials provisionInstance(String serviceInstanceId)
        throws ServiceBrokerException {

        ResponseEntity<H2oCredentials> h2oCredentialsResponseEntity;
        try {
            h2oCredentialsResponseEntity = h2oRest.createH2oInstance(
                serviceInstanceId, nodesCount, memory, yarnConf);
            LOGGER.info("response: '" + h2oCredentialsResponseEntity.getStatusCode() + "'");
        } catch (RestClientException e) {
            throw new ServiceBrokerException(errorMsg(serviceInstanceId), e);
        }

        if (h2oCredentialsResponseEntity.getStatusCode() == HttpStatus.OK) {
            return h2oCredentialsResponseEntity.getBody();
        } else {
            throw new ServiceBrokerException(errorMsg(serviceInstanceId));
        }
    }

    private String errorMsg(String serviceInstanceId) {
        return "Unable to provision h2o for: " + serviceInstanceId;
    }
}
