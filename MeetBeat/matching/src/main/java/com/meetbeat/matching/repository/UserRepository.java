package com.meetbeat.matching.repository;

import com.meetbeat.matching.model.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends Neo4jRepository<UserNode, Long> {
}