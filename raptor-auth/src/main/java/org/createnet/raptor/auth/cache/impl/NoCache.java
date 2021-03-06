/*
 * Copyright 2017 FBK/CREATE-NET
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
package org.createnet.raptor.auth.cache.impl;

import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.Authorization;
import org.createnet.raptor.auth.cache.AbstractCache;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class NoCache extends AbstractCache {

  @Override
  public Boolean get(String userId, String id, Authorization.Permission op) {
    return null;
  }

  @Override
  public void set(String userId, String id, Authorization.Permission op, boolean result) {
  }

  
  @Override
  public void set(Authentication.UserInfo user) {
  }

  @Override
  public Authentication.UserInfo get(String userId) {
    return null;
  }

}
