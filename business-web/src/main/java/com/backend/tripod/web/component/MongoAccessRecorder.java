package com.backend.tripod.web.component;

import org.springframework.data.mongodb.core.MongoTemplate;

public class MongoAccessRecorder implements AccessRecorder {

    private final String collectionName;
    private final MongoTemplate mongoTemplate;

    public MongoAccessRecorder(String collectionName, MongoTemplate mongoTemplate) {
        this.collectionName = collectionName;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void record(AccessRecord record) {
        mongoTemplate.insert(record, collectionName);
    }

}
