package com.cloud.kiidlibrary.dal;

import com.cloud.kiidlibrary.model.Kiid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KiidRepository extends MongoRepository<Kiid, String> {
}