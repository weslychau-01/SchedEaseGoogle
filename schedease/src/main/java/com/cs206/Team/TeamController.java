package com.cs206.Team;

import com.cs206.Team.*;
import com.cs206.User.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/team")
public class TeamController {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    //create new team, add first user, set allMembersAdded to false
    @PostMapping("/{teamName}/{teamUserEmails}/createTeam")
    public ResponseEntity<Team> createTeam(@PathVariable(value = "teamName") String teamName,
                                           @PathVariable(value = "teamUserId") List<String> teamUserEmails) {

        //create teamUserIds and users
        List<String> teamUserIds = new ArrayList<>();
        List<User> users = new ArrayList<>();

        //find users by email and add them to the teamUserIds
        for (String userEmail: teamUserEmails){
            Optional<User> optionalUser = userRepository.findByUserEmail(userEmail);
            User user = new User();
            if(optionalUser.isPresent()){
                user = optionalUser.get();
            }
            users.add(user);
            teamUserIds.add(user.getId());

        }


        //create team
        Team team = new Team();
        team.setTeamName(teamName);
        team.setTeamUserIds(teamUserIds);
        team.setTeamMeetingIds(new ArrayList<String>());

        //save the team
        teamRepository.save(team);

        //update for users their team
        for (User user : users){
            List<String> teamIds = user.getTeamIds();
            teamIds.add(team.get_id());
            user.setTeamIds(teamIds);
            userRepository.save(user);
        }

        return new ResponseEntity<Team>(team, HttpStatus.OK);
    }

    //add user to the team
    @PutMapping("/{userId}/{teamId}/addUser")
    public ResponseEntity<String> addUser(@PathVariable(value = "userId") String userId, @PathVariable(value = "teamId") String teamId) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Team team = new Team();
        if (optionalTeam.isPresent()) {
            team = optionalTeam.get();
        }

        //create a new list of userIds and add the new user
        List<String> userIds = team.getTeamUserIds();
        userIds.add(userId);

        team.setTeamUserIds(userIds);
        teamRepository.save(team);

        //need to send a notification to everyone in the team
        return new ResponseEntity<String>("User Added", HttpStatus.OK);
    }

    //get team by teamId
    @GetMapping("/{teamId}/getTeamById")
    public ResponseEntity<Team> getTeamById(@PathVariable(value = "teamId") String teamId) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Team team = new Team();
        if (optionalTeam.isPresent()) {
            team = optionalTeam.get();
        }

        return new ResponseEntity<Team>(team, HttpStatus.OK);
    }

    @GetMapping("/{teamName}/getTeamByName")
    public ResponseEntity<Team> getTeamByName(@PathVariable(value = "teamName") String teamName) {
        Optional<Team> optionalTeam = teamRepository.findByTeamName(teamName);
        Team team = new Team();
        if (optionalTeam.isPresent()) {
            team = optionalTeam.get();
        }

        return new ResponseEntity<Team>(team, HttpStatus.OK);
    }

    //delete meeting by teamId
    @DeleteMapping("/{teamId}/deleteTeamById")
    public ResponseEntity<String> deleteTeamById(@PathVariable(value = "teamId") String teamId) {
        teamRepository.deleteById(teamId);
        return new ResponseEntity<String>("Team Deleted", HttpStatus.OK);
    }

}