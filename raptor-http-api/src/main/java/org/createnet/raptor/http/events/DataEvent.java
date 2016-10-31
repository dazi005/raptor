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
package org.createnet.raptor.http.events;

import javax.inject.Inject;
import org.createnet.raptor.http.service.AuthService;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class DataEvent  extends ObjectEvent {
  
  final private Stream stream;
  final private RecordSet record;
  
  public DataEvent(Stream stream, RecordSet record) {
    super(stream.getServiceObject());
    this.stream = stream;
    this.record = record;
  }
  
  public DataEvent(Stream stream, RecordSet record, String accessToken) {
    super(stream.getServiceObject(), accessToken);
    this.stream = stream;
    this.record = record;
  }

  public Stream getStream() {
    return stream;
  }

  public RecordSet getRecord() {
    return record;
  }
  
}
