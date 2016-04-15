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

import com.google.common.annotations.VisibleForTesting;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.data.Stat;

public class ZookeeperNodeOperations {

  private ZookeeperNodeOperations() {}

  @VisibleForTesting
  static CuratorFramework getNewTempClient(String connectionString) {
    CuratorFramework tempClient = CuratorFrameworkFactory.builder().connectString(connectionString)
        .retryPolicy(new RetryOneTime(100)).build();
    tempClient.start();
    return tempClient;
  }

  public static void createNode(String connectionString, String path) throws Exception {

    CuratorFramework tempClient = getNewTempClient(connectionString);

    tempClient.create().creatingParentsIfNeeded().forPath(path);

    tempClient.close();
  }

  public static boolean checkExists(String connectionString, String path) throws Exception {

    CuratorFramework tempClient = getNewTempClient(connectionString);

    Stat stat = tempClient.checkExists().forPath(path);

    tempClient.close();

    return stat != null;
  }
}
