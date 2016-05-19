/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.db.couchbase;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.JsonNode;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.db.StorageProvider;
import org.createnet.raptor.db.config.StorageConfiguration;
import org.createnet.raptor.db.query.BaseQuery;
import org.createnet.raptor.db.query.ListQuery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class CouchbaseConnectionTest {

  static String bucketKey = "stream";

  static final protected ObjectMapper mapper = new ObjectMapper();
  static final StorageProvider storage = new StorageProvider();

  static Storage.Connection instance = null;

  protected ObjectNode loadData(String filename) throws IOException {

    String filepath = filename + ".json";
    URL res = getClass().getClassLoader().getResource(filepath);

    if (res == null) {
      throw new IOException("Cannot load " + filepath);
    }

    String strpath = res.getPath();

    Path path = Paths.get(strpath);
    byte[] content = Files.readAllBytes(path);

    return (ObjectNode) mapper.readTree(content);
  }

  ;
  
  public CouchbaseConnectionTest() {
  }

  public static Storage.Connection getConnection() throws Storage.StorageException, IOException {

    if (instance == null) {

      StorageConfiguration config;
      config = new StorageConfiguration();

      config.type = "couchbase";

      config.couchbase.buckets.put(bucketKey, "test_stream");

      List<List<String>> indexList = new ArrayList();

      List<String> indexFields = new ArrayList();
      // userId
      indexFields.add("userId");
      indexList.add(indexFields);

      // userId + streamId
      indexFields.clear();
      indexFields.add("userId");
      indexFields.add("streamId");
      indexList.add(indexFields);

      // userId + objectId
      indexFields.clear();
      indexFields.add("userId");
      indexFields.add("objectId");
      indexList.add(indexFields);

      config.couchbase.bucketsIndex.put(bucketKey, indexList);

      config.couchbase.nodes.add("raptor.local");

      config.couchbase.username = "Administrator";
      config.couchbase.password = "password";

      // bootstrap
      storage.initialize(config);
      storage.setup(true);
      storage.connect();

      instance = storage.getConnection(bucketKey);

      assertNotNull(instance);
    }

    return instance;
  }

  @BeforeClass
  public static void setUpClass() throws Storage.StorageException, IOException {
    // setup & connect
    getConnection();
  }

  @AfterClass
  public static void tearDownClass() throws Storage.StorageException, IOException {
    // drop test buckets
    storage.destroy();
    instance = null;
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  protected String getStreamId(ObjectNode record, int time) {
    String id = record.get("objectId") + "-" + record.get("streamId") + "-" + time;
    return id;
  }

  protected int getTime() {
    return (int) (System.currentTimeMillis() / 1000);
  }

  protected int getTTL(int time) {
    return time + (1000 * 60 * 60 * 24 * 90); // +3 months;
  } 

  @Test
  public void testSetGet() throws IOException, Storage.StorageException {

    int time = getTime();

    ObjectNode record = record = loadData("record");
    record.put("lastUpdate", time);

    String id = getStreamId(record, time);

    instance.set(id, record.toString(), 0);

    String result = instance.get(id);

    assertNotNull(result);

    JsonNode json2 = mapper.readTree(result);

    assertTrue(json2.has("channels"));
    assertEquals(json2.get("lastUpdate").asInt(), time);

    instance.delete(id);

  }

  /**
   * Test of delete method, of class CouchbaseConnection.
   */
  @Test
  public void testDelete() throws Storage.StorageException, IOException {

    int time = getTime();

    ObjectNode record = record = loadData("record");
    record.put("lastUpdate", time);

    String id = getStreamId(record, time);

    instance.set(id, record.toString(), 0);

    // load data, ensure is set
    String result = instance.get(id);
    assertNotNull(result);

    instance.delete(id);

    // load data, ensure is gone
    result = instance.get(id);
    assertNull(result);

  }

  /**
   * Test of list method, of class CouchbaseConnection.
   */
  @Test
  public void testList() throws Exception {

    String userId = "dude";
    String streamId = "sensorstuff";

    int count = 5;
    for (int i = 0; i < count; i++) {

      int time = getTime();

      ObjectNode record = record = loadData("record");

      record.put("lastUpdate", time + (i*100));
      record.put("userId", userId);
      record.put("streamId", streamId);

      String id = getStreamId(record, time + (i*100));
      instance.set(id, record.toString(), 0);
      
      String res = instance.get(id);
      assertNotNull(res);
      
    }

    // list records by userId + streamId
    BaseQuery query = BaseQuery.queryBy(
            new BaseQuery.QueryParam("userId", userId),
            new BaseQuery.QueryParam("streamId", streamId)
    );

    List<String> result = instance.list(query);

    assertEquals(result.size(), count);

  }

}
