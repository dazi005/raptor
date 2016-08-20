/*
 * Copyright 2016 CREATE-NET.
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
package org.createnet.raptor.db.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class StorageConfiguration {

  public StorageConfiguration() {
  }
  
  public String type;
  public Couchbase couchbase = new Couchbase();
  public Jdbc jdbc = new Jdbc();
  
  static public class Jdbc {
    
    public String connection = "";    
    final public Map<String, Table> tables = new HashMap();
    
    static public class Table {
      
      public String name;
      public Map<String, String> fields;
      
      public Table() {}
    }
    
    public Jdbc() {
    }
  }
  static public class Couchbase {

    public Couchbase() {
    }
    
    final public List<String> nodes = new ArrayList();
    public String username;
    public String password;
    final public Map<String, String> buckets = new HashMap();
    final public Map<String, List<List<String>>> bucketsIndex = new HashMap();
    final public BucketDefaults bucketDefaults = new BucketDefaults();
    
    static public class BucketDefaults {

      public BucketDefaults() {
      }
      
      public String password = "";
      public int quota = 120;
      public int replica = 0;
      public boolean indexReplica = false;
      public boolean enableFlush = false;
      
    }
  }

}
