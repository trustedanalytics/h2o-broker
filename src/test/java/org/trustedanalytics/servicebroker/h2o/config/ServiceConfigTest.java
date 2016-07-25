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

package org.trustedanalytics.servicebroker.h2o.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ServiceConfigTest {

    private ServiceInstanceServiceConfig serviceConfig;

    @Before
    public void setup() {
        serviceConfig = new ServiceInstanceServiceConfig();
    }

    @Test
    public void responseHandler_treat410Gone_asNormalBehaviour() throws IOException {
        //arrange
        ResponseErrorHandler handler = serviceConfig.responseHandler();
        MockClientHttpResponse response = new MockClientHttpResponse((byte[]) null, HttpStatus.GONE);

        //act
        boolean result = handler.hasError(response);

        //assert
        assertThat(result, is(false));
    }

    @Test
    public void responseHandler_treat400BadRequest_asError() throws IOException {
        //arrange
        ResponseErrorHandler handler = serviceConfig.responseHandler();
        MockClientHttpResponse response = new MockClientHttpResponse((byte[]) null, HttpStatus.BAD_REQUEST);

        //act
        boolean result = handler.hasError(response);

        //assert
        assertThat(result, is(true));
    }

    @Test
    public void responseHandler_treat500InternalError_asError() throws IOException {
        //arrange
        ResponseErrorHandler handler = serviceConfig.responseHandler();
        MockClientHttpResponse response = new MockClientHttpResponse((byte[]) null, HttpStatus.INTERNAL_SERVER_ERROR);

        //act
        boolean result = handler.hasError(response);

        //assert
        assertThat(result, is(true));
    }
}
