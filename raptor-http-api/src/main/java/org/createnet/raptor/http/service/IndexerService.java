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
package org.createnet.raptor.http.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.createnet.raptor.config.exception.ConfigurationException;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.Stream;
import org.jvnet.hk2.annotations.Service;
import org.createnet.raptor.search.Indexer;
import org.createnet.raptor.search.impl.IndexerConfiguration;
import org.createnet.raptor.search.IndexerProvider;
import org.createnet.raptor.search.query.Query;
import org.createnet.raptor.search.query.impl.es.DataQuery;
import org.createnet.raptor.search.query.impl.es.LastUpdateQuery;
import org.createnet.raptor.search.query.impl.es.ObjectListQuery;
import org.createnet.raptor.search.query.impl.es.ObjectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
@Service
public class IndexerService extends AbstractRaptorService {

    private final Logger logger = LoggerFactory.getLogger(IndexerService.class);

    @Inject
    ConfigurationService configuration;
    
    @Inject
    CacheService cache;

    /**
     * Limit of records that can be fetched per request
     */
    private final int defaultRecordLimit = 1000;

    private Indexer indexer;

    public enum IndexNames {
        object, data, subscriptions
    }

    @PostConstruct
    @Override
    public void initialize() {
        try {
            getIndexer();
        } catch (Indexer.IndexerException | ConfigurationException e) {
            throw new ServiceException(e);
        }
    }

    @PreDestroy
    @Override
    public void shutdown() {
        try {
            getIndexer().close();
            indexer = null;
        } catch (Indexer.IndexerException | ConfigurationException e) {
            throw new ServiceException(e);
        }
    }

    public Indexer getIndexer() {

        if (indexer == null) {
            indexer = new IndexerProvider();
            indexer.initialize(configuration.getIndexer());
            indexer.open();
            indexer.setup(false);
        }

        return indexer;
    }

    protected IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor getIndexDescriptor(IndexNames name) {
        return configuration.getIndexer().elasticsearch.indices.names.get(name.toString());
    }

    public Indexer.IndexRecord getIndexRecord(IndexNames name) {
        IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor desc = getIndexDescriptor(name);
        return new Indexer.IndexRecord(desc.index, desc.type);
    }

    public void setQueryIndex(Query query, IndexNames name) {
        IndexerConfiguration.ElasticSearch.Indices.IndexDescriptor desc = getIndexDescriptor(name);
        query.setIndex(desc.index);
        query.setType(desc.type);
    }

    public List<ServiceObject> getObjectsByUser(String userId) {
        ObjectQuery q = new ObjectQuery();
        q.setUserId(userId);
        return searchObject(q);
    }

    public List<ServiceObject> getObjects(List<String> ids) {
        
        List<ServiceObject> cached = ids.stream().map(id -> cache.getObject(id)).filter(o -> o != null).collect(Collectors.toList());
        
        if(cached.size() == ids.size()) {
            return cached;
        }
        
        ObjectListQuery q = new ObjectListQuery();
        q.ids.addAll(ids);
        return searchObject(q);
    }

    public ServiceObject getObject(String id) {
        ServiceObject obj = getObjects(Arrays.asList(id)).get(0);
        if (obj == null) {
            throw new Indexer.IndexerException("Object " + id + " not found");
        }
        return obj;
    }

    public void indexObject(ServiceObject obj, boolean isNew) {

        Indexer.IndexRecord record = getIndexRecord(IndexNames.object);

        record.id = obj.id;
        record.body = obj.toJSON();

        // add to cache
        cache.setObject(obj);
        
        // force creation
        record.isNew(isNew);
        getIndexer().save(record);
        
//        if (!lookupObject(obj.id)) {
//            throw new Indexer.IndexerException("Index timeout while processing " + obj.id);
//        }

    }

//    /**
//     * @TODO Remove on upgrade to ES 5.x
//     */
//    @Deprecated
//    protected boolean lookupObject(String objectId) {
//        int max = 5, curr = max, wait = 200; //ms
//        while (curr > 0) {
//            try {
//                List<ServiceObject> objs = getObjects(Arrays.asList(objectId));
//                if (!objs.isEmpty()) {
//                    logger.warn("Object {} avail in index after {}ms", objectId, (max - curr) * wait);
//                    return true;
//                }
//                Thread.sleep(wait);
//            } catch (Exception ex) {
//                logger.debug("Detected exception loading objects: {}", ex.getMessage());
//            } finally {
//                curr--;
//            }
//        }
//        return false;
//    }

    public void deleteObject(ServiceObject obj) {
        Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
        record.id = obj.id;
        
        cache.removeObject(obj.id);
        
        getIndexer().delete(record);
        deleteData(obj.streams.values());
    }

    public List<ServiceObject> searchObject(Query query) {

        setQueryIndex(query, IndexNames.object);

        List<Indexer.IndexRecord> results = getIndexer().search(query);
        List<ServiceObject> list = new ArrayList();
        
        for (Indexer.IndexRecord result : results) {
            ServiceObject obj = ServiceObject.fromJSON(result.body);
            list.add(obj);
        }

        return list;
    }

    public RecordSet searchLastUpdate(Stream stream) {

        LastUpdateQuery lastUpdateQuery = new LastUpdateQuery(stream.getServiceObject().id, stream.name);
        setQueryIndex(lastUpdateQuery, IndexNames.data);

        lastUpdateQuery.setOffset(0);
        lastUpdateQuery.setLimit(1);
        lastUpdateQuery.setSort(new Query.SortBy("lastUpdate", Query.Sort.DESC));

        List<Indexer.IndexRecord> results = getIndexer().search(lastUpdateQuery);

        if (results.isEmpty()) {
            return null;
        }

        return new RecordSet(stream, results.get(0).body);
    }

    public void indexData(Stream stream, RecordSet recordSet) {

        Indexer.IndexRecord record = getIndexRecord(IndexNames.data);
        record.id = stream.getServiceObject().id + "-" + stream.name + "-" + recordSet.getLastUpdate().getTime();
        record.isNew(true);

        ObjectNode data = (ObjectNode) recordSet.toJsonNode();

        data.put("streamId", stream.name);
        data.put("objectId", stream.getServiceObject().getId());
        data.put("userId", stream.getServiceObject().getUserId());

        record.body = data.toString();

        getIndexer().save(record);
    }

    public void saveObjects(List<ServiceObject> ids) {
        saveObjects(ids, null);
    }

    public void saveObjects(List<ServiceObject> ids, Boolean isNew) {

        List<Indexer.IndexOperation> ops = new ArrayList();
        for (ServiceObject obj : ids) {
            Indexer.IndexRecord record = getIndexRecord(IndexNames.object);
            record.id = obj.id;
            if (isNew != null) {
                record.isNew(isNew);
            }
            record.body = obj.toJSON();
            
            // add cache
            cache.setObject(obj);
            
            Indexer.IndexOperation.Type opType;
            if(isNew == null)
                opType = Indexer.IndexOperation.Type.UPSERT;
            else
                opType = isNew ? Indexer.IndexOperation.Type.CREATE : Indexer.IndexOperation.Type.UPDATE;
            
            Indexer.IndexOperation op = new Indexer.IndexOperation(opType, record);
            ops.add(op);
        }

        getIndexer().batch(ops);

//        if (!ids.isEmpty()) {
//            lookupObject(ids.get(0).id);
//        }
    }

    public List<RecordSet> getStreamData(Stream stream) {

        DataQuery query = new DataQuery();
        setQueryIndex(query, IndexNames.data);

        query.match = true;
        query.matchfield = "streamId";
        query.matchstring = stream.name;

        List<Indexer.IndexRecord> records = getIndexer().search(query);
        List<RecordSet> results = new ArrayList();

        for (Indexer.IndexRecord record : records) {
            RecordSet recordSet = new RecordSet(stream, record.body);
            results.add(recordSet);
        }

        return results;
    }

    public void deleteData(Stream stream) {

        List<Indexer.IndexOperation> deletes = new ArrayList();
        List<RecordSet> results = getStreamData(stream);

        for (RecordSet recordSet : results) {

            Indexer.IndexRecord record = getIndexRecord(IndexNames.data);
            record.id = stream.getServiceObject().id + "-" + stream.name + "-" + recordSet.getLastUpdate().getTime();

            Indexer.IndexOperation op = new Indexer.IndexOperation(Indexer.IndexOperation.Type.DELETE, record);
            deletes.add(op);
        }

        getIndexer().batch(deletes);

    }

    public ResultSet searchData(Stream stream, DataQuery query) {

        setQueryIndex(query, IndexNames.data);
        List<Indexer.IndexRecord> records = getIndexer().search(query);

        ResultSet resultset = new ResultSet(stream);
        for (Indexer.IndexRecord record : records) {
            resultset.add(new RecordSet(stream, record.body));
        }

        return resultset;
    }

    public void deleteData(Collection<Stream> changedStreams) {
        for (Stream changedStream : changedStreams) {
            deleteData(changedStream);
        }
    }

    public ResultSet fetchData(Stream stream) {
        return fetchData(stream, 0);
    }

    public ResultSet fetchData(Stream stream, long limit) {

        // query for all the data
        DataQuery query = new DataQuery();
        setQueryIndex(query, IndexNames.data);

        query.setLimit(defaultRecordLimit);
        query.setSort(new Query.SortBy("lastUpdate", Query.Sort.DESC));
        query.timeRange(Instant.EPOCH);

        ResultSet data = searchData(stream, query);
        return data;
    }

    public RecordSet fetchLastUpdate(Stream stream) {
        ResultSet data = fetchData(stream, 1);
        return data.size() > 0 ? data.get(0) : null;
    }

}
