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

    //create new team, add first user, set allMembersAdded to false
    @PostMapping("/{teamName}/{teamUserId}/{userCount}/{firstTeamDateTime}/{lastTeamDateTime}/createTeam")
    public ResponseEntity<Team> createTeam(@PathVariable(value = "teamName") String teamName,
                                           @PathVariable(value = "teamUserId") String teamUserId,
                                           @PathVariable(value = "firstTeamDateTime") LocalDateTime firstTeamDateTime,
                                           @PathVariable(value = "lastTeamDateTime") LocalDateTime lastTeamDateTime){

        List<String> teamUserIds = new ArrayList<>();
        teamUserIds.add(teamUserId);

        Team team = new Team();
        team.setTeamName(teamName);
        team.setTeamUserIds(teamUserIds);
        team.setTeamMeetingIds(new ArrayList<String>());
        team.setFirstTeamDateTime(firstTeamDateTime);
        team.setLastTeamDateTime(lastTeamDateTime);
        team.setAllMembersAdded(false);

        teamRepository.save(team);

        return new ResponseEntity<Team>(team, HttpStatus.OK);
    }

    //add user to the team
    @PutMapping("/{userId}/{teamId}/addUser")
    public ResponseEntity<String> addUser(@PathVariable(value = "userId") String userId, @PathVariable(value = "teamId")String teamId){
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Team team = new Team();
        if (optionalTeam.isPresent()){
            team = optionalTeam.get();
        }

        //create a new list of userIds and add the new user
        List<String> userIds = team.getTeamUserIds();
        userIds.add(userId);

        team.setTeamUserIds(userIds);

        //need to send a notification to everyone in the team
        return new ResponseEntity<String>("User Added", HttpStatus.OK);
    }

    //get team by teamId
    @GetMapping("/{teamId}/getTeamById")
    public ResponseEntity<Team> getTeamById(@PathVariable(value = "teamId")String teamId){
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Team team = new Team();
        if (optionalTeam.isPresent()){
            team = optionalTeam.get();
        }

        return new ResponseEntity<Team>(team, HttpStatus.OK);
    }

    //delete meeting by teamId
    @DeleteMapping("/{teamId}/deleteTeamById")
    public ResponseEntity<String> deleteTeamById(@PathVariable(value = "teamId")String teamId){
        teamRepository.deleteById(teamId);
        return new ResponseEntity<String>("Team Deleted", HttpStatus.OK);
    }


    //create a new meeting
//    @PostMapping("/{TeamId}/{firstMeetingDateTime}/{LastMeetingDateTime}/{meetingFrequency}/{meetingDurationInSeconds}/createMeeting")
//    public ResponseEntity<Meeting> createMeeting(@PathVariable(value = "teamId") String teamId,
//                                              @PathVariable(value = "firstMeetingDateTime") LocalDateTime firstMeetingDateTime,
//                                              @PathVariable(value = "lastMeetingDateTime") LocalDateTime lastMeetingDateTime,
//                                              @PathVariable(value = "meetingFrequency") String meetingFrequency,
//                                              @PathVariable(value = "meetingDurationInSeconds") long meetingDurationInSeconds){
//
//        Optional<Team> optionalTeam = teamRepository.findById(teamId);
//        Team team = new Team();
//        if (optionalTeam.isPresent()){
//            team = optionalTeam.get();
//        }
//
//        List<String> meetingUserIds = team.getTeamUserIds();
//
//        Meeting meeting = new Meeting();
//        //set meetingTeamId to teamId
//        meeting.setMeetingTeamId(teamId);
//
//        //set FirstMeetingDateTime and LastMeetingDateTime
//        meeting.setFirstMeetingDateTime(firstMeetingDateTime);
//        meeting.setLastMeetingDateTimee(lastMeetingDateTime);
//
//        //set meetingFrequency and Duration
//        meeting.setMeetingFrequency(meetingFrequency);
//        meeting.setMeetingDurationInSeconds(meetingDurationInSeconds);
//
//        //set Meeting time to null
//        meeting.setMeetingStartDateTime(null);
//        meeting.setMeetingEndDateTime(null);
//
//        //set meetingAvailablities to empty map
//        Map<String, Integer> meetingAvailabilities = new HashMap<>();
//        meeting.setMeetingAvailabilites(meetingAvailabilities);
//
//        //set hasUserVoted to empty map
//        Map<String, Boolean> hasUserVoted = new HashMap<>();
//        for (String userId : meetingUserIds){
//            hasUserVoted.putIfAbsent(userId, false);
//        }
//        meeting.setHasUserVoted(hasUserVoted);
//
//
//        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
//    }
}
