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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.createnet.raptor.auth.AuthHttpClient;
import org.createnet.raptor.auth.authorization.AbstractAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class TokenAuthorization extends AbstractAuthorization {

  final private Logger logger = LoggerFactory.getLogger(TokenAuthorization.class);
  final private AuthHttpClient client = new AuthHttpClient();

  @Override
  public boolean isAuthorized(String id, Permission op) throws AuthorizationException {

    try {

      logger.debug("Check authorization of user {} for permission {}", id, op.name());

      String response = request(id, op.name());

      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(response);

      boolean allowed = node.get("result").booleanValue();

      logger.debug("User {} {} allowed to {}", id, (allowed ? "NOT" : ""), op.name());

      return allowed;

    } catch (IOException | AuthHttpClient.ClientException ex) {
      logger.error("Error checking authorization request", ex);
      throw new AuthorizationException(ex);
    }
  }

  @Override
  public void initialize(Map<String, Object> configuration) {
    super.initialize(configuration);
    client.setUrl((String) configuration.get("token_url"));
  }

  protected String request(String id, String permission) throws IOException, AuthHttpClient.ClientException {

    List<NameValuePair> args = new ArrayList();

    args.add(new BasicNameValuePair("operation", "checkPermission"));
    args.add(new BasicNameValuePair("permission", permission));
    args.add(new BasicNameValuePair("soid", id));

    return client.request(getAccessToken(), args);
  }

}
