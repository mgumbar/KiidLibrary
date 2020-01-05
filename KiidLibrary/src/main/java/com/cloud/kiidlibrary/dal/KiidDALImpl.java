package com.cloud.kiidlibrary.dal;

import com.cloud.kiidlibrary.model.Kiid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class KiidDALImpl implements KiidDAL {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Kiid getByCloudId(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("nextCloudId").is(id));
        return mongoTemplate.findOne(query, Kiid.class);
    }
}
