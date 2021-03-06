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
package org.createnet.raptor.auth.service.acl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class RaptorPermission extends BasePermission {

  public RaptorPermission(int mask) {
    super(mask);
  }

  public RaptorPermission(int mask, char code) {
    super(mask, code);
  }

  public static final Permission READ = new RaptorPermission(1 << 0, 'R'); // 1
  public static final Permission WRITE = new RaptorPermission(1 << 1, 'W'); // 2
  public static final Permission CREATE = new RaptorPermission(1 << 2, 'C'); // 4
  public static final Permission DELETE = new RaptorPermission(1 << 3, 'D'); // 8
  public static final Permission ADMINISTRATION = new RaptorPermission(1 << 4, 'A'); // 16

  public static final Permission PUSH = new RaptorPermission(1 << 5, 'P'); // 32
  public static final Permission PULL = new RaptorPermission(1 << 6, 'U'); // 64
  public static final Permission SUBSCRIBE = new RaptorPermission(1 << 7, 'S'); // 128
  public static final Permission EXECUTE = new RaptorPermission(1 << 8, 'E'); // 256
  public static final Permission LIST = new RaptorPermission(1 << 9, 'L'); // 512
  
  // Aliasing
  public static final Permission UPDATE = WRITE; // 2
  
  public static String toLabel(Permission p) {
    switch (p.getMask()) {
      case 1:
        return "read";
      case 2:
        return "update";
      case 4:
        return "create";
      case 8:
        return "delete";
      case 16:
        return "admin";
      case 32:
        return "push";
      case 64:
        return "pull";
      case 128:
        return "subscribe";
      case 256:
        return "execute";
      case 512:
        return "list";
    }
    return null;
  }

  public static List<String> toLabel(List<Permission> p) {
    return p.stream().map(RaptorPermission::toLabel).collect(Collectors.toList());
  }
  
  public static Permission fromLabel(String name) {
    switch (name.toLowerCase()) {
      case "read":
        return RaptorPermission.READ;
      case "update":
        return RaptorPermission.WRITE;
      case "create":
        return RaptorPermission.CREATE;
      case "delete":
        return RaptorPermission.DELETE;
      case "admin":
        return RaptorPermission.ADMINISTRATION;
      case "push":
        return RaptorPermission.PUSH;
      case "pull":
        return RaptorPermission.PULL;
      case "subscribe":
        return RaptorPermission.SUBSCRIBE;
      case "execute":
        return RaptorPermission.EXECUTE;
      case "list":
        return RaptorPermission.LIST;
    }
    return null;
  }

  public static List<Permission> fromLabel(List<String> p) {
    return p.stream().map(RaptorPermission::fromLabel).collect(Collectors.toList());
  }  
  
}
