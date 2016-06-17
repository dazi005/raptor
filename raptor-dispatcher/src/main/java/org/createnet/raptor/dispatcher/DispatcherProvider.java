/*
 * The MIT License
 *
 * Copyright 2016 CREATE-NET http://create-net.org
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.createnet.raptor.dispatcher.client.BrokerClient;
import org.createnet.raptor.dispatcher.configuration.DispatcherConfiguration;
import org.createnet.raptor.dispatcher.router.MessageRouter;
import org.createnet.raptor.plugin.PluginConfiguration;
import org.createnet.raptor.plugin.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class DispatcherProvider extends AbstractDispatcher<DispatcherConfiguration> {

  final protected Logger logger = LoggerFactory.getLogger(DispatcherProvider.class);

  protected BlockingQueue<Runnable> queue;
  protected final Queue messageQueue = new Queue();

  protected BrokerClient client;

  protected final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
  protected ThreadPoolExecutor executorService;

  protected final PluginLoader<MessageRouter> routerPluginLoader = new PluginLoader();
  protected final PluginLoader<BrokerClient> clientPluginLoader = new PluginLoader();
  
  @Override
  public PluginConfiguration<DispatcherConfiguration> getPluginConfiguration() {
    return null;
  }

  class MessageDispatcher implements Runnable {

    final protected Queue.QueueMessage qm;
    final protected BrokerClient client;

    public MessageDispatcher(Queue.QueueMessage qm, BrokerClient client) {
      this.qm = qm;
      this.client = client;
    }

    public Queue.QueueMessage getQm() {
      return qm;
    }

    @Override
    public void run() {
      try {
        client.send(qm.topic, qm.message);
        logger.debug("Message sent to {}", qm.topic);
      } catch (BrokerClient.BrokerClientException ex) {
        requeue(qm);
        logger.error("Error sending message", ex);
      }
    }

  }
  
  protected void setup() {

    queue = new LinkedBlockingQueue(100);

    executorService = new ThreadPoolExecutor(5, 15, 1000, TimeUnit.MILLISECONDS, queue, new ThreadPoolExecutor.CallerRunsPolicy());
    executorService.setRejectedExecutionHandler(new RejectedExecutionHandler() {
      @Override
      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor1) {
        try {
          logger.warn("Message delivery failed, add back to queue");
          Thread.sleep(1000);
          requeue(((MessageDispatcher) r).getQm());
        } catch (InterruptedException e) {
          logger.warn("Cannot add back to queue, got [InterruptedException]: {}", e.getMessage());
        }
      }
    });

    client.initialize(configuration);

    try {
      client.connect();
    } catch (BrokerClient.BrokerClientException ex) {
      logger.error("Cannot connect to the broker", ex);
    }

    executorService.prestartAllCoreThreads();

    scheduledExecutor.scheduleAtFixedRate(() -> {
      dispatch();
    }, 0, 100, TimeUnit.MILLISECONDS);
  
  }
  
  @Override
  public void initialize(DispatcherConfiguration configuration) {

    super.initialize(configuration);
    
    MessageRouter router = routerPluginLoader.load(configuration.router, MessageRouter.class);
    client = clientPluginLoader.load(configuration.client, BrokerClient.class);
    
    setup();
  }

  public void add(String topic, String message) {
    Queue.QueueMessage qm = new Queue.QueueMessage(topic, message);
    add(qm);
  }

  @Override
  public void add(Queue.QueueMessage qm) {
    if (messageQueue.size() > configuration.queueLength) {
      logger.warn("Message queue limit reached ({})", configuration.queueLength);
    }
    messageQueue.add(qm);
    dispatch();
  }
  
  @Override
  public int size() {
    return messageQueue.size();
  }

  protected void requeue(Queue.QueueMessage qm) {
    qm.tries++;
    if (qm.valid()) {
      logger.debug("Message added back to queue due to dispatcher error: {}/{}", qm.tries, qm.maxRetries);
      add(qm);
    } else {
      logger.debug("Message dropped");
    }
  }

  @Override
  public void dispatch() {

    if (messageQueue.size() == 0) {
      return;
    }
    
    logger.debug("Message queue has {} records", messageQueue.size());
    
    Queue.QueueMessage qm = messageQueue.pop();

    if (qm == null) {
      return;
    }

    executorService.execute(new MessageDispatcher(qm, client));
    dispatch();
  }

  protected void shutdown(ExecutorService executor) {
    try {
      logger.debug("Attempt to shutdown executor");
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.warn("Tasks interrupted");
    } finally {
      if (!executor.isTerminated()) {
        logger.warn("cancel non-finished tasks");
      }
      executor.shutdownNow();
      logger.warn("Shutdown finished");
    }
  }

  @Override
  public void close() {
    shutdown(scheduledExecutor);
    shutdown(executorService);
    client.disconnect();
    logger.debug("Closed dispatcher");
  }
  
  
  
}
