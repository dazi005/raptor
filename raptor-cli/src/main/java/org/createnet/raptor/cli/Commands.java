/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.cli;

import javax.inject.Inject;
import org.createnet.raptor.db.Storage;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.http.service.IndexerService;
import org.createnet.raptor.http.service.StorageService;
import org.createnet.search.raptor.search.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Commands {
  
  @Inject
  StorageService storage;
  
  @Inject
  IndexerService indexer;
  
  public void setup(boolean force) throws Storage.StorageException, ConfigurationException, Indexer.IndexerException {
    indexer.getIndexer().setup(force);    
    storage.getStorage().setup(force);
  }
  
  public void index()  {
    try {
      ObjectIndexer objIndexer = new ObjectIndexer(storage, indexer);
      objIndexer.sync();
    }
    catch(ObjectIndexer.ObjectIndexerException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void launch() {    
    new ServiceLauncher().start();
  }
  
}
