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
package org.createnet.search.raptor.search.impl.es;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.createnet.search.raptor.search.impl.ElasticSearchIndexer;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ElasticSearchIndexAdmin {

  public class IndexAdminException extends Exception {

    public IndexAdminException(String message) {
      super(message);
    }

  }

  final protected Logger logger = LoggerFactory.getLogger(ElasticSearchIndexer.class);
  private Client client;

  public Client getClient() throws IndexAdminException {
    if (client == null) {
      throw new IndexAdminException("ES client must be set");
    }
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public boolean exists(String name) throws InterruptedException, ExecutionException, IndexAdminException {
    Future<IndicesExistsResponse> req = getClient().admin().indices().exists(new IndicesExistsRequest(name));
    IndicesExistsResponse res = req.get();
    logger.debug("Index does {} exists", res.isExists() ? "" : "not");
    return res.isExists();
  }

  public void delete(String name) throws InterruptedException, ExecutionException, IndexAdminException {
    
    DeleteIndexRequest deleteIndexReq = new DeleteIndexRequest(name);
    Future<DeleteIndexResponse> reqCreate = client.admin().indices().delete(deleteIndexReq);
    DeleteIndexResponse resDelete = reqCreate.get();
    
    logger.debug("Delete index {}", name);
  }

  public void create(String name, String definition) throws InterruptedException, ExecutionException {
    
    Settings indexSettings = Settings.settingsBuilder()
            .loadFromSource(definition).build();
    
    CreateIndexRequest createIndexReq = new CreateIndexRequest(name, indexSettings);
    Future<CreateIndexResponse> reqCreate = client.admin().indices().create(createIndexReq);
    
    CreateIndexResponse resCreate = reqCreate.get();
    
    logger.debug("Created index {}", name);
  }

}
