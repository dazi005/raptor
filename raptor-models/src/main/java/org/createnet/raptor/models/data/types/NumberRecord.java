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
package org.createnet.raptor.models.data.types;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.NumberFormat;
import java.text.ParsePosition;
import org.createnet.raptor.models.data.Record;
import org.createnet.raptor.models.objects.RaptorComponent;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
public class NumberRecord extends Record<Number> {

    protected Number value;

    @Override
    public Number getValue() {
        return value;
    }

    @Override
    public String getType() {
        return "number";
    }

    @Override
    public void setValue(Object value) {
        Number n = parseValue(value);
        this.value = n;
    }

    @Override
    public Number parseValue(Object value) {
        try {

            if (value instanceof Number) {
                return (Number) value;
            }

            if (value instanceof JsonNode) {

                JsonNode node = (JsonNode) value;

                if (node.isNumber()) {

                    if (node.isInt() || node.isShort()) {
                        return (Number) node.asInt();
                    }

                    if (node.isDouble() || node.isFloat()) {
                        return (Number) node.asDouble();
                    }

                    if (node.isLong()) {
                        return (Number) node.asLong();
                    }
                }

                if (node.isTextual()) {
                    value = node.asText();
                }

            }

            NumberFormat formatter = NumberFormat.getInstance();
            ParsePosition pos = new ParsePosition(0);
            Number numVal = formatter.parse((String) value, pos);

            return numVal;
        } catch (Exception e) {
            throw new RaptorComponent.ParserException(e);
        }
    }

    @Override
    public Class<Number> getClassType() {
        return Number.class;
    }

}
