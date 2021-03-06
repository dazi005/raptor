/*
 * Copyright 2017 FBK/CREATE-NET <http://create-net.fbk.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.models.objects;

import org.createnet.raptor.models.objects.serializer.ActionSerializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@JsonSerialize(using = ActionSerializer.class)
public class Action extends ServiceObjectContainer {

    Logger logger = LoggerFactory.getLogger(Action.class);

    public String id = null;
    public String status = null;
    public String name;
    public String description;

    public static Action create(String name, String status) {
        Action a = new Action();
        a.name = name;
        a.status = status;
        return a;
    }
    
    public static Action create(String name) {
        return Action.create(name, null);
    }
    
    public Action(String json) {
        JsonNode tree;
        try {
            tree = mapper.readTree(json);
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
        parse(tree, null);
    }

    public Action(String json, ServiceObject object) {
        ObjectMapper mapper = ServiceObject.getMapper();
        JsonNode tree;
        try {
            tree = mapper.readTree(json);
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
        parse(tree, object);
    }

    public Action(JsonNode json, ServiceObject object) {
        parse(json, object);
    }

    public Action(JsonNode json) {
        parse(json, null);
    }

    public Action(String name, JsonNode json, ServiceObject object) {
        this.name = name;
        parse(json, object);
    }

    public Action(String name, JsonNode json) {
        this.name = name;
        parse(json, null);
    }

    public Action() {
    }

    protected void parse(JsonNode json) {

        if (json.isTextual()) {
            name = json.asText();
        } else {

            if (json.has("id")) {
                id = json.get("id").asText();
            }

            if (json.has("status")) {
                status = json.get("status").asText();
            }

            if (json.has("name")) {
                name = json.get("name").asText();
            }

            if (json.has("description")) {
                description = json.get("description").asText();
            }
        }
    }

    protected void parse(JsonNode json, ServiceObject object) {
        this.setServiceObject(object);
        parse(json);
    }

    @Override
    public void validate() {

        if (name == null) {
            throw new ValidationException("Action name is empty");
        }

    }

    @Override
    public void parse(String json)  {
        try {
            parse(mapper.readTree(json));
        } catch (IOException ex) {
            throw new ParserException(ex);
        }
    }

}
