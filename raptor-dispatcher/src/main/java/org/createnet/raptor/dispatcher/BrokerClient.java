/*
 * The MIT License
 *
 * Copyright 2016 CREATE-NET
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.createnet.raptor.dispatcher;

import org.createnet.raptor.dispatcher.exception.DispatchException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class BrokerClient {
    
  Logger logger = LoggerFactory.getLogger(BrokerClient.class);
  
  private static MqttClient connection = null;
  private DispatcherConfiguration configuration;
    
  private final String clientName = "raptor-dispatcher";
  private final int connectionTimeout = 10;
  private final MemoryPersistence clientPersistence = new MemoryPersistence();
  
  private final int qos = 2;
  private final boolean retain = false;

  public void initialize(DispatcherConfiguration config) {
    configuration = config;  
  }

  protected synchronized void connect() throws MqttException {
    
    if(connection != null && connection.isConnected()) {
      logger.debug("Mqtt connection available");
      return;
    }
    
    logger.debug("Connecting to mqtt broker");
    
    try {

      String uri = configuration.uri;
      String username = configuration.username;
      String password = configuration.password;
      
      logger.debug("Connecting to broker {}", uri);
      
      connection = new MqttClient(uri, clientName, clientPersistence);
      
      MqttConnectOptions connOpts = new MqttConnectOptions();
      
      connOpts.setCleanSession(true);
      connOpts.setConnectionTimeout(connectionTimeout);
      
      if(username != null && password != null) {
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
      }
      
      connection.connect(connOpts);
      
    } catch (MqttException me) {
//      logger.error("Failed to connect to broker", me);
      logger.error("Failed to connect to broker", me);
      throw me;
    }
  }
  
  public MqttClient getConnection() throws MqttException {
    connect();
    return connection;
  }

  public void sendMessage(String topic, String message) throws DispatchException {
    try {
      
      MqttClient conn = getConnection();
      if(conn == null || !conn.isConnected() ) {
        throw new DispatchException("Connection is not available");
      }
      
      getConnection().publish(topic, message.getBytes(), qos, retain);
    }
    catch(MqttException e) {
      logger.error("MQTT exception", e);
      throw new DispatchException();
    }
  }
  
  public void disconnect() {
    if(connection != null && connection.isConnected()) {
      try {
        connection.disconnect();
      } catch (MqttException ex) {
        logger.error("Cannot close connection properly", ex);
      }
    }
  }

  boolean isConnected() {
    return connection == null ? false : connection.isConnected();
  }
  
}
