package com.meetbeat.matching.repository;

import com.meetbeat.matching.model.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface UserRepository extends Neo4jRepository<UserNode, Long> {
}