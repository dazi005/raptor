/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
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
package org.createnet.raptor.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.createnet.raptor.models.objects.ServiceObject;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
abstract public class TestUtils {
  
  protected JsonNode jsonServiceObject = null;
  protected ServiceObject serviceObject;
  
  protected final ObjectMapper mapper = ServiceObject.getMapper();
  
  final protected String defaultStreamName = "mylocation";  
  
  protected void loadObject() throws IOException {
    jsonServiceObject = loadData("model");
    serviceObject = new ServiceObject();
  };
  
  protected JsonNode loadData(String filename) throws IOException {
    
    String filepath = filename + ".json";
    URL res = getClass().getClassLoader().getResource(filepath);
    
    if(res == null) throw new IOException("Cannot load " + filepath);
    
    String strpath = res.getPath();
    
    Path path = Paths.get(strpath);
    byte[] content = Files.readAllBytes(path);
    
    return mapper.readTree(content);  
  };
  
}
