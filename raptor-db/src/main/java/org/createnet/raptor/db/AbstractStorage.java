/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
abstract public class AbstractStorage implements Storage {
  
  protected Map<String, Object> config;
  final protected Map<String, Connection> connections = new HashMap<>();

  static  public String generateId() {
    return UUID.randomUUID().toString();
  }

  public Map<String, Connection> getConnections() {
    return connections;
  }

  public void addConnection(Connection conn) {
    connections.put(conn.getId(), conn);
  }

  @Override
  public Connection getConnection(String id) {
    return connections.get(id);
  }

  @Override
  public void disconnect() {
    Iterator<Map.Entry<String, Connection>> it = getConnections().entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Connection> item = it.next();
      item.getValue().disconnect();
    }
  }

  @Override
  public void initialize(Map<String, Object> configuration) {
    this.config = configuration;
  }
  
  protected Map<String, Object> getConfiguration() {
    return config;
  }

  public void set(String connectionId, String id, String data, int ttl) {
    getConnection(connectionId).set(id, data, ttl);
  }

  public String get(String connectionId, String id) {
    return getConnection(connectionId).get(id);
  }

  public void delete(String connectionId, String id) {
    getConnection(connectionId).delete(id);
  }  
  
}
