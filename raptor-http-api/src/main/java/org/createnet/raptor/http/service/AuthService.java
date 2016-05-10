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

import java.io.IOException;
import javax.inject.Inject;
import org.createnet.raptor.auth.AuthProvider;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authentication.Authentication.UserInfo;
import org.createnet.raptor.auth.authorization.Authorization;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AuthService {
  
  @Inject ConfigurationService config;
  
  private AuthProvider auth;
  
  protected AuthProvider getProvider() throws IOException {
    if(auth == null) {
     auth = new AuthProvider();
     auth.initialize(config.getAuth());
    }
    return auth;
  }
  
  public boolean validToken(String accessToken) throws IOException, Authentication.AutenticationException {
    UserInfo user = getProvider().getUser(accessToken);
    return (user != null);
  }
  
  public String getUserId() throws IOException {
    return getProvider().getUserId();
  }
  
  public boolean isAllowed(String id, Authorization.Permission op) throws Authorization.AuthorizationException {
    return auth.isAuthorized(id, op);
  }
  
  public boolean isAllowed(Authorization.Permission op) throws Authorization.AuthorizationException {
    return isAllowed(null, op);
  }
}
