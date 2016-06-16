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
package org.createnet.raptor.auth.authorization.impl;
import org.createnet.raptor.auth.authorization.AbstractAuthorization;
import org.createnet.raptor.plugin.BasePluginConfiguration;
import org.createnet.raptor.plugin.PluginConfiguration;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AllowAllAuthorization extends AbstractAuthorization {

  static final public String defaultAccessToken = "Bearer 1234567890";
  static final public String defaultUserId = "default-user";

  @Override
  public boolean isAuthorized(String accessToken, String id, Permission op) throws AuthorizationException {
    return !accessToken.isEmpty();
  }

  @Override
  public PluginConfiguration getPluginConfiguration() {
    return new BasePluginConfiguration("allow_all");
  }


}
