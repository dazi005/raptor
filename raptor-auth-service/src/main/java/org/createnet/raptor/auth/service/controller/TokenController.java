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
package org.createnet.raptor.auth.service.controller;

import org.createnet.raptor.auth.service.entity.Token;
import org.createnet.raptor.auth.service.entity.User;
import org.createnet.raptor.auth.service.jwt.JwtTokenUtil;
import org.createnet.raptor.auth.service.services.TokenService;
import org.createnet.raptor.auth.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@RestController
public class TokenController {

  @Autowired
  private TokenService tokenService;
  
  @Autowired
  private UserService userService;

  @RequestMapping("/user/{uuid}/tokens")
  public Iterable<Token> getTokens(
          @AuthenticationPrincipal User user,
          @PathVariable String uuid
  ) {
    // TODO add ACL checks
    return tokenService.list(uuid);
  }

  @RequestMapping(value = "/user/{uuid}/tokens/{tid}", method = RequestMethod.GET)
  public Token get(
          @AuthenticationPrincipal User user,
          @PathVariable String uuid,
          @PathVariable Long tokenId
  ) {
    // TODO add ACL checks
    return tokenService.read(tokenId);
  }

  @RequestMapping(value = "/user/{uuid}/tokens/{tid}", method = RequestMethod.PUT)
  public Token update(
          @AuthenticationPrincipal User user,
          @PathVariable String uuid,
          @PathVariable Long tokenId,
          @RequestBody Token token
  ) {
    token.setId(tokenId);
    return tokenService.update(token);
  }

  @RequestMapping(value = "/user/{uuid}/tokens", method = RequestMethod.POST)
  public ResponseEntity<Token> create(
          @AuthenticationPrincipal User currentUser,
          @PathVariable String uuid,
          @RequestBody Token rawToken
  ) {
    
    User user = userService.getByUuid(uuid);
    if(user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    
    Token token = tokenService.create(rawToken.getName(), user, rawToken.getSecret());
    
    if(token == null) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    
    return ResponseEntity.status(HttpStatus.CREATED).body(token);
  }

}
