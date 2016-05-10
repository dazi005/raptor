/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.db.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import java.util.Iterator;
import java.util.Map;
import org.createnet.raptor.db.AbstractStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class CouchbaseStorage extends AbstractStorage {

  final Logger logger = LoggerFactory.getLogger(CouchbaseStorage.class);

  final String clusterDefaultPassword = "";
  final String bucketDefaultPassword = "";

  final int bucketDefaultQuota = 240;
  final int bucketDefaultReplica = 0;
  final boolean bucketDefaultIndexReplica = false;
  final boolean bucketDefaultEnableFlush = false;

  protected Cluster cluster;

  protected Cluster connectCluster() {

    if (cluster == null) {
      // Connect to localhost
      logger.debug("Connecting to couchbase cluster");
      cluster = CouchbaseCluster.create(getConfiguration().couchbase.nodes);
    }

    return cluster;
  }

  @Override
  public void connect() {

    connectCluster();

    Map<String, String> buckets = getConfiguration().couchbase.buckets;

    Iterator<Map.Entry<String, String>> it = buckets.entrySet().iterator();
    while (it.hasNext()) {

      Map.Entry<String, String> item = it.next();

      if (getConnection(item.getKey()) != null) {
        continue;
      }

      logger.debug("Connecting bucket {}", item.getValue());

      Bucket bucket = cluster.openBucket(item.getValue(), clusterDefaultPassword);
      Connection conn = new CouchbaseConnection(item.getKey(), bucket);

      conn.connect();

      addConnection(conn);
    }

  }

  @Override
  public void disconnect() {

    logger.debug("Disconnecting from couchbase");

    super.disconnect();
    // Disconnect and clear all allocated resources
    cluster.disconnect();
  }

  @Override
  public void setup(boolean forceSetup) {

    logger.debug("Setup database");

    connectCluster();

    Map<String, String> buckets = getConfiguration().couchbase.buckets;

    String adminUsername = getConfiguration().couchbase.username;
    String adminPassword = getConfiguration().couchbase.password;

    ClusterManager clusterManager = cluster.clusterManager(adminUsername, adminPassword);
    for (Map.Entry<String, String> el : buckets.entrySet()) {
      String bucketName = el.getValue();

      boolean exists = clusterManager.hasBucket(bucketName);

      if (exists && forceSetup) {
        logger.debug("Drop bucket {}", bucketName);
        clusterManager.removeBucket(bucketName);
        exists = false;
      }

      if (!exists) {

        logger.debug("Creating bucket {}", bucketName);
        BucketSettings bucketSettings = new DefaultBucketSettings.Builder()
                .type(BucketType.COUCHBASE)
                .name(bucketName)
                .password(bucketDefaultPassword)
                .quota(bucketDefaultQuota) // megabytes
                .replicas(bucketDefaultReplica)
                .indexReplicas(bucketDefaultIndexReplica)
                .enableFlush(bucketDefaultEnableFlush)
                .build();

        clusterManager.insertBucket(bucketSettings);
      }
    }

    logger.debug("Setup completed");
  }


}
