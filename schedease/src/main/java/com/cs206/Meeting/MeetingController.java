package com.cs206.Meeting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.cs206.Event.*;
import com.cs206.Interval.Interval;
import com.cs206.Team.*;
import com.cs206.User.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// import com.example.SchedEase.Event.EventService;
// import com.example.SchedEase.User.UserService;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/meeting")
public class MeetingController {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

     @Autowired
     private EventRepository eventRepository;

    @GetMapping("/getAllMeetings")
    public ResponseEntity<List<Meeting>> getAllEvents(){
        return new ResponseEntity<List<Meeting>>(meetingService.allEvents(), HttpStatus.OK);
    }

    @GetMapping("{meetingId}/getMeeting")
    public ResponseEntity<Meeting> getMeeting(@PathVariable(value = "meetingId") String meetingId) {
        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
        Meeting meeting = new Meeting();
        if (optionalMeeting.isPresent()) {
            meeting = optionalMeeting.get();
        }
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
    }

//    @GetMapping("{meetingId}/getTeam")
//    public ResponseEntity<String> getTeam(@PathVariable(value = "meetingId") String meetingId){
//        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
//        Meeting meeting = new Meeting();
//        if (optionalMeeting.isPresent()){
//            meeting = optionalMeeting.get();
//        }
//
//        String teamId = meeting.getTeamId();
//        System.out.println(teamId);
//        return new ResponseEntity<String>("Done", HttpStatus.OK);
//    }

//    @GetMapping("{meetingId}/getUser")
//    public ResponseEntity<User> getUser(@PathVariable(value = "meetingId") String meetingId){
//        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
//        Meeting meeting = new Meeting();
//        if (optionalMeeting.isPresent()){
//            meeting = optionalMeeting.get();
//        }
//
//        String teamId = meeting.getTeamId();
//
//        Optional<Team> optionalTeam = teamRepository.findById(teamId);
//        Team team = new Team();
//        if (optionalTeam.isPresent()){
//            team = optionalTeam.get();
//        }
//
//        List<String> userIds = team.getUserId();
//        String firstUserId = userIds.get(0);
//
//        User user = new User();
//        Optional<User> optionalUser = userRepository.findById(firstUserId);
//        if (optionalTeam.isPresent()){
//            user = optionalUser.get();
//        }
//
//        return new ResponseEntity<User>(user, HttpStatus.OK);
//    }

//    @PutMapping("{meetingId}/setTeam")
//    public ResponseEntity<String> setTeam (@PathVariable(value = "meetingId") String meetingId){
//        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
//        Meeting meeting = new Meeting();
//        if (optionalMeeting.isPresent()){
//            meeting = optionalMeeting.get();
//        }
//
//        meeting.setTeamId("65d5b5bd4d003d38ec53a72d");
//        return new ResponseEntity<String>("Done", HttpStatus.OK);
//    }


    //create a new meeting, generate meeting Id
    @PostMapping("/{teamId}/{meetingName}/{firstDateTimeLimit}/{lastDateTimeLimit}/{DurationInSeconds}/createMeeting")
    public ResponseEntity<?> createMeeting(@PathVariable(value = "teamId") String teamId,
                                           @PathVariable(value = "meetingName") String meetingName,
                                           @PathVariable(value = "firstDateTimeLimit") LocalDateTime firstDateTimeLimit,
                                           @PathVariable(value = "lastDateTimeLimit") LocalDateTime lastDateTimeLimit,
                                           @PathVariable(value = "durationInSeconds") long durationInSeconds,
                                           @PathVariable(value = "frequency") String frequency){

        //create new interval timeLimit, put firstDateTime, LastDateTime into it (this would be TimeLimit)
        Interval timeLimit = new Interval(firstDateTimeLimit, lastDateTimeLimit);
        LocalDate firstMeetingDate = firstDateTimeLimit.toLocalDate();
        LocalDate lastMeetingDate = lastDateTimeLimit.toLocalDate();

        //using the teamId find team
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Team team = new Team();
        if (optionalTeam.isPresent()){
            team = optionalTeam.get();
        }


        //get the list of usedIds from the team
        List<String> userIds = team.getTeamUserIds();

        //get a list of userIds from the team
        List<User> users = new ArrayList<>();
        for (String userId : userIds){
            User user = new User();
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()){
                user = optionalUser.get();
                //add users to users list
                users.add(user);
            }
        }

        //get eventIds from the users, for each eventId, get the event, check the time within meeting date, add it to list
        List<Interval> unavailableTimings = new ArrayList<Interval>();
        List<String> eventIds = new ArrayList<>();
        for (User user : users){
            List<String> userEventIds = user.getUserEventIds();
            //for each user, get the list of userEventIds
            for (String userEventId : userEventIds){
                Event event = new Event();
                Optional<Event> optionalEvent = eventRepository.findById(userEventId);
                //get the event
                if (optionalEvent.isPresent()){
                    event = optionalEvent.get();
                    //get the startDate and endDate for the event
                    LocalDate eventStartDate = event.getEventStartDateTime().toLocalDate();
                    LocalDate eventEndDate = event.getEventEndDateTime().toLocalDate();

                    //check the eventStartDate & eventEndDate is within the meeting dates
                    if ((eventStartDate.isAfter(firstMeetingDate) || eventStartDate.isEqual(firstMeetingDate))
                    && (eventStartDate.isBefore(lastMeetingDate) || eventStartDate.isEqual(lastMeetingDate))){
                        unavailableTimings.add(new Interval(event.getEventStartDateTime(), event.getEventEndDateTime()));
                    }
                }
            }
        }

       //sort the timings based on startDateTime
        unavailableTimings.sort(Comparator.comparing(interval -> interval.getStartDateTime()));

        //get possible meeting timings based on timeLimit, the list of unavailable timings and meeting duration
        List<Interval> availableTimings = meetingService.findCommonAvailableTimes(timeLimit, unavailableTimings, durationInSeconds);

        //debug to get available timings
        System.out.println("Common Available Times:");
        for (Interval interval : availableTimings) {
            System.out.println(interval);
        }

        List<String> availableTimingString = new ArrayList<>();
        for (Interval interval : availableTimings){
            availableTimingString.add(interval.convertToString());
        }

        Meeting meeting = new Meeting();

        //set meetingTeamId to teamId, and meetingName, and usersCount
        meeting.setMeetingTeamId(teamId);
        meeting.setMeetingName(meetingName);
        meeting.setUserCount(userIds.size());

        //set FirstMeetingDateTime and LastMeetingDateTime
        meeting.setFirstMeetingDateTime(firstDateTimeLimit);
        meeting.setLastMeetingDateTimee(lastDateTimeLimit);

        //set meetingFrequency and Duration
        meeting.setMeetingFrequency(frequency);
        meeting.setMeetingDurationInSeconds(durationInSeconds);

        //set Meeting time to null
        meeting.setMeetingStartDateTime(null);
        meeting.setMeetingEndDateTime(null);

        //set meetingAvailabilities to a map with available timings with zero votes
        Map<String, Integer> meetingAvailabilities = new TreeMap<>();
        for (String availableTiming : availableTimingString){
            meetingAvailabilities.putIfAbsent(availableTiming, 0);
        }
        meeting.setMeetingAvailabilites(meetingAvailabilities);

        //set hasUserVoted with the userId and false
        Map<String, Boolean> hasUserVoted = new HashMap<>();
        for (String userId : userIds){
            hasUserVoted.putIfAbsent(userId, false);
        }
        meeting.setHasUserVoted(hasUserVoted);

        //save the meeting then get the id
        meetingRepository.save(meeting);

        //return the meeting details
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
    }

    //use meeting id to get the common availabilities
    @GetMapping("/{meetingId}/getCommonAvailabilities")
    public ResponseEntity<?> getCommonAvailabilities(@PathVariable(value = "meetingId") String meetingId){
        //Get meeting using MeetingId
        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
        Meeting meeting = new Meeting();
        if (optionalMeeting.isPresent()){
            meeting = optionalMeeting.get();
        }

        //get the availabilities for meeting
        Map<String, Integer> availabilities = meeting.getMeetingAvailabilites();

        //return all the possible timings and their votes, front end need to show all the timings, then add to the votes
        //before sending back
        return new ResponseEntity<Map<String, Integer>>(availabilities, HttpStatus.OK);
    }

    @PutMapping("/{meetingId}/{userId}/{availabilitiesVotes}/addVote")
    public ResponseEntity<?> addVote(@PathVariable(value = "meetingId") String meetingId,
                                     @PathVariable(value = "userId") String userId,
                                     @PathVariable(value = "availabilitiesVotes") Map<String, Integer> availabilitiesVotes){

        //Get meeting using MeetingId
        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
        Meeting meeting = new Meeting();
        if (optionalMeeting.isPresent()){
            meeting = optionalMeeting.get();
        }

        int userCount = meeting.getUserCount();

        //set the availability votes with the new numbers after a user has voted
        meeting.setMeetingAvailabilites(availabilitiesVotes);

        //set the userVoted to true for the userId
        Map<String, Boolean> usersVoted = meeting.getHasUserVoted();
        usersVoted.put(userId, true);

        //check if everyone has voted
        boolean hasAllVoted = true;
        for (String userVoted : usersVoted.keySet()){
            if (!usersVoted.get(userVoted)){
                hasAllVoted = false;
                break;
            }
        }

        String earliestDateTimeString = "";
        //if all has voted need
        if (hasAllVoted){
            LocalDateTime earliestStartDateTime = null;
            //find timing that is earliest & with all votes
            for (String availability : availabilitiesVotes.keySet()){
                //if votes == userCount (all can make it)
                if (availabilitiesVotes.get(availability) == userCount){
                    // if no earliest time, set the whole timing and the start time
                    if (earliestDateTimeString.compareTo("") == 0){
                        String[] arr = availability.split("_");
                        //set the earliest startDateTime to the earliest time
                        earliestStartDateTime = LocalDateTime.parse(arr[0], formatter);
                        //check if the earliest startDateTime is after the current time, or else need to delete from map
                        if (earliestStartDateTime.isBefore(LocalDateTime.now())){
                            earliestStartDateTime = null;
                            availabilitiesVotes.remove(availability);
                        } else {
                            earliestDateTimeString = availability;
                            //break;
                        }
                    }

                    //Might not need this if the map is already sorted in order (need check)
                    else {
                        String[] arr = availability.split("_");
                        LocalDateTime startTime = LocalDateTime.parse(arr[0], formatter);
                        if (startTime.isBefore(earliestStartDateTime)){
                            earliestStartDateTime = startTime;
                            earliestDateTimeString = availability;
                        }
                    }
                }

            }
        }

        //set the values of the meeting time for the event
        String[] arr = earliestDateTimeString.split("_");
        LocalDateTime meetingStartTime = LocalDateTime.parse(arr[0], formatter);
        LocalDateTime meetingEndTime = LocalDateTime.parse(arr[1], formatter);
        meeting.setMeetingStartDateTime(meetingStartTime);
        meeting.setMeetingEndDateTime(meetingEndTime);

        //returns the meeting details
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK); //need send notification
    }
}



