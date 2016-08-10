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
package org.createnet.raptor.broker.util;

import java.util.UUID;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class TopicChecker {

  public boolean checkUUID(String objectId) {
    
    int idLen = objectId.length();

    // UUID without hyphens (legacy id format)
    if(idLen == 45) {
      return true;      
    }
    
    try {
      UUID.fromString(objectId);
    } catch (IllegalArgumentException e) {
      return false;
    }
    
    return true;
  }

}
