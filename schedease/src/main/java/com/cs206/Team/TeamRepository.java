package com.cs206.Team;

import com.cs206.User.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends MongoRepository<Team, String> {
    // List<Team> findbyUserId(String userId);

    Optional<Team> findByTeamName(String teamName);
}
