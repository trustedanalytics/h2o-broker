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

package org.trustedanalytics.servicebroker.h2o.helper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ZookeeperNodeOperationsTest {

  private TestingServer zkServer;
  private String zkConnectionString;

  @Before
  public void initEmbeddedZKServer() throws Exception {
    zkServer = new TestingServer();
    zkServer.start();
    zkConnectionString = zkServer.getConnectString();
  }

  @After
  public void closeZKServer() throws IOException {
    zkServer.close();
  }

  @Test
  public void createDir_correctPath_znodeCreated() throws Exception {
    // act
    ZookeeperNodeOperations.createNode(zkConnectionString, "/node");

    // assert
    CuratorFramework tempClient = ZookeeperNodeOperations.getNewTempClient(zkConnectionString);
    Stat stat = tempClient.checkExists().forPath("/node");
    tempClient.close();
    assertThat(stat, is(notNullValue()));
  }

  @Test
  public void createDir_correctComplexPath_znodesCreated() throws Exception {
    // act
    ZookeeperNodeOperations.createNode(zkConnectionString, "/node/nodeLevel2");

    // assert
    CuratorFramework tempClient = ZookeeperNodeOperations.getNewTempClient(zkConnectionString);
    Stat stat = tempClient.checkExists().forPath("/node/nodeLevel2");
    tempClient.close();
    assertThat(stat, is(notNullValue()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createDir_incorrectPath_exceptionThrown() throws Exception {
    // act
    ZookeeperNodeOperations.createNode(zkConnectionString, "node");
  }

  @Test
  public void checkExists_dirExists_returnsTrue() throws Exception {
    // arrange
    CuratorFramework tempClient = ZookeeperNodeOperations.getNewTempClient(zkConnectionString);
    tempClient.create().forPath("/newnode");
    tempClient.close();

    // act
    boolean exists = ZookeeperNodeOperations.checkExists(zkConnectionString, "/newnode");

    // assert
    assertThat(exists, equalTo(true));
  }

  @Test
  public void checkExists_dirNotExist_returnsFalse() throws Exception {
    // act
    boolean exists = ZookeeperNodeOperations.checkExists(zkConnectionString, "/newnode2");

    // assert
    assertThat(exists, equalTo(false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkExists_incorrectPath_exceptionThrown() throws Exception {
    // act
    ZookeeperNodeOperations.checkExists(zkConnectionString, "newnode3");
  }
}
