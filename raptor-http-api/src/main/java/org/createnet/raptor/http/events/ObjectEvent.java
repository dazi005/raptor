/*
 * Copyright 2016 CREATE-NET http://create-net.org
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
package org.createnet.raptor.http.events;

import org.createnet.raptor.events.AbstractEvent;
import javax.inject.Inject;
import org.createnet.raptor.http.service.AuthService;
import org.createnet.raptor.models.objects.ServiceObject;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class ObjectEvent extends AbstractEvent {
  
  @Inject
  AuthService auth;
  
  protected ServiceObject obj;
  protected String accessToken;
  
  public ObjectEvent() {
  }
  
  public ObjectEvent(ServiceObject obj) {
    this.obj = obj;
    this.accessToken = auth.getAccessToken();
  }
  
  public ObjectEvent(ServiceObject obj, String accessToken) {
    this.obj = obj;
    this.accessToken = accessToken;
  }

  public ServiceObject getObject() {
    return obj;
  }

  public String getAccessToken() {
    return accessToken;
  }
  
}
