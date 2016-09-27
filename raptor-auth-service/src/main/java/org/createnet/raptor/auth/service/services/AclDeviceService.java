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
package org.createnet.raptor.auth.service.services;

import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.auth.entity.AuthorizationRequest;
import org.createnet.raptor.auth.service.acl.RaptorPermission;
import org.createnet.raptor.auth.service.acl.UserSid;
import org.createnet.raptor.auth.service.entity.Device;
import org.createnet.raptor.auth.service.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;

/**
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class AclDeviceService {

  private final Logger logger = LoggerFactory.getLogger(AclDeviceService.class);

  @Autowired
  private AclManagerService aclManagerService;

  protected Permission[] defaultPermissions = new Permission[]{
    RaptorPermission.READ,
    RaptorPermission.WRITE,};

  public void add(Device device, User user, Permission permission) {
    aclManagerService.addPermission(Device.class, device.getId(), new UserSid(user), permission);
  }

  public void add(Device device, User user, List<Permission> permissions) {
    aclManagerService.addPermissions(Device.class, device.getId(), new UserSid(user), permissions);
  }

  public void set(Device device, User user, List<Permission> permissions) {
    aclManagerService.setPermissions(Device.class, device.getId(), new UserSid(user), permissions);
  }

  public List<Permission> list(Device device, User user) {
    ObjectIdentity oiDevice = new ObjectIdentityImpl(Device.class, device.getId());
    return aclManagerService.getPermissionList(user, oiDevice);
  }

  public void remove(Device device, User user, Permission permission) {
    aclManagerService.removePermission(Device.class, device.getId(), new UserSid(user), permission);
  }

  public boolean isGranted(Device device, User user, Permission permission) {
    return aclManagerService.isPermissionGranted(Device.class, device.getId(), new UserSid(user), permission);
  }

  public void register(Device device) {

    User owner = device.getOwner();
    List<Permission> permissions = list(device, owner);
    Sid sid = new UserSid(owner);

    logger.debug("Found {} permissions for {}", permissions.size(), owner.getUuid());
    if (permissions.isEmpty()) {

      logger.debug("Set default permission");

      if (owner.getId().equals(device.getOwner().getId())) {
        aclManagerService.addPermission(Device.class, device.getId(), sid, RaptorPermission.ADMINISTRATION);
        permissions.add(RaptorPermission.ADMINISTRATION);
      }

      for (Permission permission : defaultPermissions) {
        aclManagerService.addPermission(Device.class, device.getId(), sid, permission);
        permissions.add(permission);
      }

    }

    String perms = permissions.stream().map(Permission::toString).collect(Collectors.joining("\n - "));
    logger.debug("Permission set for device {} to {}\n - {}", device.getUuid(), device.getOwner().getUuid(), perms);

  }

  public boolean check(Device device, User user, Permission permission) {

    if (user == null) {
      return false;
    }
    if (device == null) {
      return false;
    }
    if (permission == null) {
      return false;
    }

    // check if user has ADMINISTRATION permission on device 
    if (isGranted(device, user, RaptorPermission.ADMINISTRATION)) {
      return true;
    }

    // check device specific permission first
    if (isGranted(device, user, permission)) {
      return true;
    }

    // check parent permission if available
    if (device.hasParent()) {
      return check(device.getParent(), user, permission);
    }

    return false;
  }

}
