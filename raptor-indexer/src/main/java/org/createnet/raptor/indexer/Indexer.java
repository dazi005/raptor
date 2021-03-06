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
package org.createnet.raptor.indexer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.createnet.raptor.indexer.query.Query;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public interface Indexer {
    
    final static ObjectMapper mapper = new ObjectMapper();
    
    public static ObjectMapper getObjectMapper() {
        return mapper;
    }
    
    public class IndexerException extends RuntimeException {

        public IndexerException(Exception ex) {
            super("IndexerException", ex);
        }

        public IndexerException(String reason) {
            super(reason);
        }

        public IndexerException() {
        }
    }

    public class SearchException extends IndexerException {

        public SearchException(Query.QueryException ex) {
            super(ex);
        }
        
        public SearchException(Exception ex) {
            super(ex);
        }
    };

    public class IndexRecordValidationException extends IndexerException {

        public IndexRecordValidationException(Exception ex) {
            super(ex);
        }

        public IndexRecordValidationException(String reason) {
            super(reason);
        }

    }

    public class IndexOperationException extends IndexerException {

        public IndexOperationException(Exception ex) {
            super(ex);
        }
    }

    public class IndexOperation {

        public IndexOperation(Type type, IndexRecord record) {
            this.type = type;
            this.record = record;
        }

        public enum Type {
            CREATE, UPDATE, UPSERT, SAVE, DELETE
        }

        public Type type;
        public IndexRecord record;
    }

    public class IndexRecord {

        private boolean isNew = false;

        public String index;
        public String type;
        public String id;
        public String body = null;

        public IndexRecord(String index, String type, String id, String body) {
            this.index = index;
            this.type = type;
            this.id = id;
            this.body = body;
        }

        public IndexRecord(String index, String type, String id) {
            this.index = index;
            this.type = type;
            this.id = id;
        }

        public IndexRecord(String index, String type) {
            this.index = index;
            this.type = type;
        }

        public boolean isNew() {
            return isNew;
        }

        public boolean isNew(boolean isNew) {
            this.isNew = isNew;
            return isNew;
        }

        public void validate() throws IndexRecordValidationException {
            if (this.id == null) {
                throw new IndexRecordValidationException("record.id cannot be null");
            }
            if (this.body == null) {
                throw new IndexRecordValidationException("record.body cannot be null");
            }
            if (this.type == null) {
                throw new IndexRecordValidationException("record.type cannot be null");
            }
        }

    }
    
    /**
     * Open a connection to the Indexer service
     * @throws IndexerException
     */
    public void open() throws IndexerException;
    
    /**
     * Perform initialization operations 
     * @throws IndexerException
     */    
    public void initialize(IndexerConfiguration configuration) throws IndexerException;
    
    /**
     * Setup the connection
     * @param forceSetup force the setup, removing previous data eventually
     * @throws IndexerException
     */    
    public void setup(boolean forceSetup) throws IndexerException;
    
    /**
     * Close the connection to the indexer
     * @throws IndexerException
     */
    public void close() throws IndexerException;
    
    /**
     * Create a new record
     * @param record
     * @throws IndexerException
     */
    public void save(IndexRecord record) throws IndexerException;

    /**
     * Delete a record
     * @param record
     * @throws IndexerException
     */
    public void delete(IndexRecord record) throws IndexerException;

    /**
     * Perform a batch operation
     * @param list
     * @throws IndexerException
     */
    public void batch(List<IndexOperation> list) throws IndexerException;

    /**
     * Reset the indexer status to a clean status, dropping any data and configuration.
     * Depending on implementation, it may destroy your data
     * @throws IndexerException
     */
    public void reset() throws IndexerException;

    /**
     * Perform a search on the indexer dataset
     * @param query
     * @return
     * @throws SearchException
     */
    public List<IndexRecord> search(Query query) throws SearchException;

}
