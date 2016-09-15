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
package org.createnet.raptor.auth.service;

import javax.sql.DataSource;
import org.createnet.raptor.auth.service.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;


/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Configuration
public class AclConfiguration {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private CacheManager cacheManager;

  @Bean
  public LookupStrategy lookupStrategy() {
    return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), auditLogger());
  }

  @Bean
  public AclAuthorizationStrategy aclAuthorizationStrategy() {
    return new AclAuthorizationStrategyImpl(new Role(Role.Roles.super_admin.name()));
  }

  @Bean
  public EhCacheBasedAclCache aclCache() {
    return new EhCacheBasedAclCache(aclEhCacheFactoryBean().getObject(), permissionGrantingStrategy(), aclAuthorizationStrategy());
  }

  @Bean
  public EhCacheFactoryBean aclEhCacheFactoryBean() {
    EhCacheFactoryBean ehCacheFactoryBean = new EhCacheFactoryBean();
    ehCacheFactoryBean.setCacheManager(aclCacheManager().getObject());
    ehCacheFactoryBean.setCacheName("aclCache");
    return ehCacheFactoryBean;
  }

  @Bean
  public EhCacheManagerFactoryBean aclCacheManager() {
    return new EhCacheManagerFactoryBean();
  }

  @Bean
  public DefaultPermissionGrantingStrategy permissionGrantingStrategy() {
    return new DefaultPermissionGrantingStrategy(auditLogger());
  }

  @Bean
  public JdbcMutableAclService aclService() {
    JdbcMutableAclService service = new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
    return service;
  }
  
  @Bean
  public AuditLogger auditLogger() {
    return new ConsoleAuditLogger();
  }

//  @Bean
//  public DefaultMethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler() {
//    return new DefaultMethodSecurityExpressionHandler();
//  }

//  @Bean
//  public MethodSecurityExpressionHandler createExpressionHandler() {
//    DefaultMethodSecurityExpressionHandler expressionHandler = defaultMethodSecurityExpressionHandler();
//    expressionHandler.setPermissionEvaluator(new AclPermissionEvaluator(aclService()));
//    expressionHandler.setPermissionCacheOptimizer(new AclPermissionCacheOptimizer(aclService()));
//    return expressionHandler;
//  }

}
