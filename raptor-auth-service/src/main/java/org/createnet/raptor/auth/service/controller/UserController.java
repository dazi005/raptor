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
package org.createnet.raptor.auth.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.createnet.raptor.auth.service.RaptorUserDetailsService;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
@RestController
@PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
@Api(tags = {"User"})
@ApiResponses(value = {
    @ApiResponse(
            code = 200,
            message = "Ok"
    )
    ,
    @ApiResponse(
            code = 401,
            message = "Not authorized"
    )
    ,
    @ApiResponse(
            code = 403,
            message = "Forbidden"
    )
    ,
    @ApiResponse(
            code = 500,
            message = "Internal error"
    )
})
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @ApiOperation(
            value = "List available user",
            notes = "",
            response = User.class,
            responseContainer = "Iterable",
            nickname = "getUsers"
    )
    public Iterable<User> getUsers() {
        return userService.list();
    }

    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    @RequestMapping(value = {"/user"}, method = RequestMethod.POST)
    @ApiOperation(
            value = "Create a new user",
            notes = "",
            response = User.class,
            nickname = "createUser"
    )
    public ResponseEntity<User> create(
            @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
            @RequestBody User rawUser
    ) {

        boolean exists = userService.exists(rawUser);

        if (exists) {
            return ResponseEntity.status(403).body(null);
        }

        return ResponseEntity.ok(userService.create(rawUser));
    }

    @RequestMapping(value = {"/me"}, method = RequestMethod.GET)
    @ApiOperation(
            value = "Get the current user profile",
            notes = "",
            response = User.class,
            nickname = "getProfile"
    )
    public User getProfile(
            @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails user
    ) {
        return (User) user;
    }


    @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
    @RequestMapping(value = {"/me"}, method = RequestMethod.PUT)
    @ApiOperation(
            value = "Update an user profile",
            notes = "",
            response = User.class,
            nickname = "updateProfile"
    )
    public User updateProfile(
            @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
            @RequestBody User rawUser
    ) {
        return userService.update(currentUser.getUuid(), rawUser);
    }    
    
    @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
    @RequestMapping(value = {"/user/{uuid}"}, method = RequestMethod.GET)
    @ApiOperation(
            value = "Get an user profile",
            notes = "",
            response = User.class,
            nickname = "getUser"
    )
    public User getUser(
            @PathVariable String uuid,
            @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser
    ) {
        return userService.getByUuid(uuid);
    }

    @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
    @RequestMapping(value = {"/user"}, method = RequestMethod.PUT)
    @ApiOperation(
            value = "Update current user profile",
            notes = "",
            response = User.class,
            nickname = "updateUser"
    )
    public User updateUser(
            @AuthenticationPrincipal RaptorUserDetailsService.RaptorUserDetails currentUser,
            @RequestBody User rawUser
    ) {
        return userService.update(currentUser.getUuid(), rawUser);
    }

    @PreAuthorize("(hasAuthority('admin') or hasAuthority('super_admin')) or principal.uuid == #uuid")
    @RequestMapping(value = {"/user/{uuid}"}, method = RequestMethod.DELETE)
    @ApiOperation(
            value = "Delete an user profile",
            notes = "",
            code = 202,
            nickname = "deleteUser"
    )
    public ResponseEntity<String> delete(@PathVariable String uuid) {

        User user = userService.getByUuid(uuid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        userService.delete(user);

        return ResponseEntity.accepted().body(null);
    }

}
