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
package org.createnet.raptor.models.objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.HashMap;
import java.util.Map;
import org.createnet.raptor.models.data.BooleanRecord;
import org.createnet.raptor.models.data.GeoPointRecord;
import org.createnet.raptor.models.data.NumberRecord;
import org.createnet.raptor.models.data.Record;
import org.createnet.raptor.models.data.StringRecord;
import org.createnet.raptor.models.events.ServiceObjectEventListener;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
abstract class ServiceObjectContainer extends RaptorContainer {

    @JsonBackReference
    protected ServiceObjectEventListener listener;
    
    @JsonBackReference
    protected ServiceObject serviceObject;
    
    @JsonBackReference
    protected Map<String, Record> types = new HashMap();
    
    protected Map<String, Record> getTypes() {
      if(types.isEmpty()) {
        
        Record instance;
        
        // String
        instance = new StringRecord();
        types.put(instance.getType(), instance);
        
        // Number
        instance = new NumberRecord();
        types.put(instance.getType(), instance);
        
        // Boolean
        instance = new BooleanRecord();
        types.put(instance.getType(), instance);
        
        // Boolean
        instance = new GeoPointRecord();
        types.put(instance.getType(), instance);

      }
      
      return types;
    }
    
    @Override
    public RaptorComponent getContainer() {
        if(getServiceObject() == null) return null;
        return getServiceObject().getContainer();
    }
    
    public void setServiceObject(ServiceObject _serviceObject) {
        this.serviceObject = _serviceObject;
//        if(_serviceObject != null) {
//          this.setContainer(_serviceObject.getContainer());
//        }
    }

    public ServiceObject getServiceObject() {
        return serviceObject;
    }

    @Override
    public ServiceObjectEventListener getListener() {
        return listener;
    }

    public void setListener(ServiceObjectEventListener listener) {
        this.listener = listener;
    }    
    
    @Override
    abstract public void validate() throws ValidationException;

    @Override
    abstract public void parse(String json) throws ParserException;
    
}
