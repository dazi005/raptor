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
package org.createnet.raptor.http.filter;

import java.security.Principal;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.http.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AuthorizationRequestFilter implements ContainerRequestFilter {

  final private Logger logger = LoggerFactory.getLogger(AuthorizationRequestFilter.class);

  @Inject
  AuthService auth;

  @Override
  public void filter(ContainerRequestContext requestContext) {
      
    String path = requestContext.getUriInfo().getPath();
    if(path.startsWith("swagger.")) {
      return;
    }
    
    // skip CORS requests
    if (requestContext.getMethod().toUpperCase().equals("OPTIONS")
            && requestContext.getHeaderString("Access-Control-Request-Method") != null) {
      logger.debug("CORS request detected, skip auth check");
      return;
    }

    try {

      String accessToken = requestContext.getHeaderString("Authorization");

      Authentication.UserInfo loggedUser = auth.getUser(accessToken);

      logger.debug("User {} authenticated", loggedUser.getUserId());

      requestContext.setSecurityContext(new Authorizer(loggedUser));

    } catch (Authentication.AuthenticationException ex) {

      logger.warn("Token is not valid: {}", ex.getMessage());

      requestContext.abortWith(
              Response
              .status(Response.Status.UNAUTHORIZED)
              .entity(getErrorMessage(Response.Status.UNAUTHORIZED, "Token is not valid"))
              .build()
      );

    } catch (ConfigurationException ex) {

      logger.error("Error loading auth configuration", ex);

      requestContext.abortWith(
              Response
              .status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "Internal error on authentication"))
              .build()
      );
    }

  }

  private String getErrorMessage(Response.Status status, String msg) {
    return "{ \"code\": " + status.getStatusCode() + ", \"message\": \"" + msg + "\" }";
  }

  public class Authorizer implements SecurityContext {

    final private Authentication.UserInfo user;
    private final Principal principal;

    public class AppPrincipal implements Principal {

      private Authentication.UserInfo user;

      public AppPrincipal(Authentication.UserInfo user) {
        super();
        this.user = user;
      }

      public Authentication.UserInfo getUser() {
        return user;
      }

      @Override
      public String getName() {
        return user.getAccessToken();
      }

    }

    public Authorizer(final Authentication.UserInfo user) {
      this.user = user;
      this.principal = new AppPrincipal(user);
    }

    @Override
    public Principal getUserPrincipal() {
      return this.principal;
    }

    @Override
    public boolean isUserInRole(String requiredRole) {
      return user.getRoles().contains(requiredRole);
    }

    @Override
    public boolean isSecure() {
      return false;
    }

    @Override
    public String getAuthenticationScheme() {
      return "token";
    }

  }

}
