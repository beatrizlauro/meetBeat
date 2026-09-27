package com.meetbeat.matching.repository;

import com.meetbeat.matching.model.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface UserRepository extends Neo4jRepository<UserNode, Long> {

    Optional<UserNode> findByGoogleId(String googleId);

    Optional<UserNode> findByUsername(String username);

    Optional<UserNode> findByEmail(String email);
}