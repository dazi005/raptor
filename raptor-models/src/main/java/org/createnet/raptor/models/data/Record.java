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
package org.createnet.raptor.models.data;

import java.util.Date;
import org.createnet.raptor.models.objects.Channel;
import org.createnet.raptor.models.objects.RaptorComponent;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
abstract public class Record<E> implements IRecord<E> {
    
    protected Channel channel;
    protected E value;
    protected Date timestamp;
    
    protected RecordSet recordset;
    
    @Override
    abstract public String getType();
    
    @Override
    abstract public E parseValue(Object raw) throws RaptorComponent.ParserException;
    
    @Override
    abstract public E getValue();
    
    @Override
    abstract public void setValue(Object value) throws RaptorComponent.ParserException;

    @Override
    public String getName() {
        return channel != null ? channel.name : null;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }
    
    @Override
    public void  setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Date getTimestamp() {
        
        if(timestamp != null) {
            return timestamp;
        }
        
        if(recordset != null) {
            return recordset.getTimestamp();
        }
        
        // force value
        setTimestamp(new Date());
        return timestamp;
    }
    
    @Override
    public Long getTimestampTime() {
        return (Long)(getTimestamp().getTime() / 1000);
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public RecordSet getRecordSet() {
        return recordset;
    }

    @Override
    public void setRecordSet(RecordSet recordset) {
        this.recordset = recordset;
    }

}
