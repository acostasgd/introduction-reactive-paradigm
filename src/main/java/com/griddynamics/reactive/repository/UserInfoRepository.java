package com.griddynamics.reactive.repository;

import com.griddynamics.reactive.document.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends ReactiveMongoRepository<User, String> {}
