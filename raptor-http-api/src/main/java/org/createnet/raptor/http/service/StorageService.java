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
package org.createnet.raptor.http.service;

import javax.inject.Inject;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.db.Storage;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */

@Service
public class StorageService {
    
  @Inject ConfigurationService configuration;
  
  Storage storage;
  
  protected Storage getStorage() {
    if(storage == null) {
//      configuration.get("storage").get("")
    }
    return storage;
  }
  
  public ServiceObject getObject(String id) throws Storage.StorageException, RaptorComponent.ParserException {
    String json = storage.getConnection("so_bucket").get(id);
    ServiceObject obj = new ServiceObject();
    obj.parse(json);
    return obj;
    
  }
  
}
