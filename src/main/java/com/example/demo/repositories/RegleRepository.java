package com.example.demo.repositories;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entitiesNoSQL.Regle;

@Repository
public interface RegleRepository extends MongoRepository<Regle, String> {

}
