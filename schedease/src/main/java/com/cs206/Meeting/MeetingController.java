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

    @PostMapping("/{firstMeeting}/{lastMeeting}/createNewMeeting")
    public ResponseEntity<?> createNewMeeting(@PathVariable(value = "firstMeeting") LocalDateTime firstMeeting,
                                              @PathVariable(value = "lastMeeting") LocalDateTime lastMeeting){
        Meeting meeting = new Meeting();

        meeting.setMeetingTeamId("65eea7fa31aa3e7018267b5d");
        meeting.setMeetingName("Another New Meeting");
        meeting.setUserCount(2);
        meeting.setHasNoConflicts(true);
        meeting.setIsMeetingSet(false);

//        String firstMeetingString = "2024-03-15T10:00:00";
//        String lastMeetingString = "2024-03-18T18:00:00";
//        //set FirstMeetingDateTime and LastMeetingDateTime
//        meeting.setFirstMeetingDateTime(LocalDateTime.parse(firstMeetingString, formatter));
//        meeting.setLastMeetingDateTime(LocalDateTime.parse(lastMeetingString, formatter));

        meeting.setFirstMeetingDateTime(firstMeeting);
        meeting.setLastMeetingDateTime(lastMeeting);

        //set meetingFrequency and Duration
        meeting.setMeetingFrequency("Once");
        meeting.setMeetingDurationInSeconds(3600);

        //set Meeting time
        meeting.setMeetingStartDateTime(null);
        meeting.setMeetingEndDateTime(null);

        Map<String, Integer> meetingAvailabilities = new TreeMap<>();
        meetingAvailabilities.put("2024-03-15T10:00:00_2024-03-15T11:00:00", 0);
        meetingAvailabilities.put("2024-03-15T12:00:00_2024-03-15T12:30:00", 0);
        meetingAvailabilities.put("2024-03-15T15:00:00_2024-03-15T14:00:00", 0);
        meetingAvailabilities.put("2024-03-16T12:00:00_2024-03-16T13:00:00", 0);
        meetingAvailabilities.put("2024-03-17T12:00:00_2024-03-17T13:00:00", 0);
        meetingAvailabilities.put("2024-03-18T14:00:00_2024-03-18T15:00:00", 0);
        meeting.setMeetingAvailabilities(meetingAvailabilities);


        Map<String, Boolean> hasUserVoted = new HashMap<>();
        hasUserVoted.put("65eea6418119e25bbacea7d3", false);
        hasUserVoted.put("65eea64b8119e25bbacea7d4", false);
        meeting.setHasUserVoted(hasUserVoted);


        meetingRepository.save(meeting);
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
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


//        using the teamId find team
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Team team = new Team();
        if (optionalTeam.isPresent()){
            team = optionalTeam.get();
        }


        //get the list of usedIds from the team
        List<String> userIds = team.getTeamUserIds();

        //get the unavailable timings for the users
        List<Interval> unavailableTimings = meetingService.getUnavailableTimings(team, firstDateTimeLimit, lastDateTimeLimit);
        //sort the timings based on startDateTime
        unavailableTimings.sort(Comparator.comparing(interval -> interval.getStartDateTime()));

        //get possible meeting timings based on timeLimit, the list of unavailable timings and meeting duration
        List<Interval> availableTimings = meetingService.findCommonAvailableTimes(timeLimit, unavailableTimings, durationInSeconds);

        Map<String, Integer> meetingAvailabilities = new TreeMap<>();
        for (Interval interval : availableTimings){
            meetingAvailabilities.putIfAbsent(interval.convertToString(), 0);
        }

        Meeting meeting = new Meeting();

        //set meetingTeamId to teamId, and meetingName, and usersCount
        meeting.setMeetingTeamId(teamId);
        meeting.setMeetingName(meetingName);
        meeting.setUserCount(userIds.size());
        meeting.setHasNoConflicts(true);
        meeting.setIsMeetingSet(false);

        //set FirstMeetingDateTime and LastMeetingDateTime
        meeting.setFirstMeetingDateTime(firstDateTimeLimit);
        meeting.setLastMeetingDateTime(lastDateTimeLimit);

        //set meetingFrequency and Duration
        meeting.setMeetingFrequency(frequency);
        meeting.setMeetingDurationInSeconds(durationInSeconds);

        //set Meeting time to null
        meeting.setMeetingStartDateTime(null);
        meeting.setMeetingEndDateTime(null);

        //set meetingAvailabilities to a map with available timings with zero votes
        meeting.setMeetingAvailabilities(meetingAvailabilities);

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
        Map<String, Integer> availabilities = meeting.getMeetingAvailabilities();

        //return all the possible timings and their votes, front end need to show all the timings, then add to the votes
        //before sending back
        return new ResponseEntity<Map<String, Integer>>(availabilities, HttpStatus.OK);
    }

    @PutMapping("{meetingId}/rescheduleMeeting")
    public ResponseEntity<?> rescheduleMeeting(@PathVariable(value = "meetingId") String meetingId){

        //find the meeting that needs to be rescheduled
        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
        Meeting meeting = new Meeting();
        if (optionalMeeting.isPresent()){
            meeting = optionalMeeting.get();
        }

        //get the meetingTeam
        Optional<Team> optionalTeam = teamRepository.findById(meeting.getMeetingTeamId());
        Team team = new Team();
        if (optionalTeam.isPresent()){
            team = optionalTeam.get();
        }

        //if frequency is only once
        if (meeting.getMeetingFrequency().compareTo("Once") == 0){
            //create a new map
            Map<String, Integer> meetingAvailabilities = new TreeMap<>();
            for (String availableTiming : meeting.getMeetingAvailabilities().keySet()){

                String[] array = availableTiming.split("_");
                LocalDateTime availableTimingStartDateTime = LocalDateTime.parse(array[0], formatter);
                LocalDateTime availableTimingEndDateTime = LocalDateTime.parse(array[1], formatter);

                //
                if (availableTimingStartDateTime.isAfter(LocalDateTime.now().plusHours(1)) || availableTimingStartDateTime.isEqual(LocalDateTime.now().plusHours(1))){
                    meetingAvailabilities.putIfAbsent(availableTiming, 0);
                }
            }

            meeting.setMeetingAvailabilities(meetingAvailabilities);
            meetingRepository.save(meeting);
            return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
        }

        else {
            //set the local firstMeetingDateTime to be within 3 days before the original meeting date / actual firstMeetingDateTime
            //or the localDateTimeNow
            LocalDateTime firstMeetingDateTime = meeting.getMeetingStartDateTime().minusDays(3);
            if (firstMeetingDateTime.isBefore(LocalDateTime.now())){
                firstMeetingDateTime = LocalDateTime.now();
            } if (firstMeetingDateTime.isBefore(meeting.getFirstMeetingDateTime())){
                firstMeetingDateTime = meeting.getFirstMeetingDateTime();
            }

            //set the lastMeetingDateTime to be within 3 days after the original meeting date / actual lastMeetingDateTime
            LocalDateTime lastMeetingDateTime = meeting.getMeetingEndDateTime().plusDays(3);
            if (lastMeetingDateTime.isAfter(meeting.getLastMeetingDateTime())){
                lastMeetingDateTime = meeting.getLastMeetingDateTime();
            }

            //find the unavailableTimings of the users
            Interval timeLimit = new Interval(meeting.getFirstMeetingDateTime(), meeting.getLastMeetingDateTime());
            List<Interval> unavailableTimings = meetingService.getUnavailableTimings(team, firstMeetingDateTime, lastMeetingDateTime);
            unavailableTimings.sort(Comparator.comparing(interval -> interval.getStartDateTime()));

            //get possible meeting timings based on timeLimit, the list of unavailable timings and meeting duration
            List<Interval> availableTimings = meetingService.findCommonAvailableTimes(timeLimit, unavailableTimings, meeting.getMeetingDurationInSeconds());

            //put the possible timings
            Map<String, Integer> meetingAvailabilities = new TreeMap<>();
            for (Interval interval : availableTimings){
                meetingAvailabilities.putIfAbsent(interval.convertToString(), 0);
            }


            //reset the meeting details and set the new details (override the current meeting)
            meeting.setMeetingTeamId(meeting.getMeetingTeamId());
            meeting.setMeetingName(meeting.getMeetingName());
            meeting.setUserCount(meeting.getUserCount());
            meeting.setHasNoConflicts(true);
            meeting.setIsMeetingSet(false);

            //set FirstMeetingDateTime and LastMeetingDateTime
            meeting.setFirstMeetingDateTime(meeting.getFirstMeetingDateTime());
            meeting.setLastMeetingDateTime(meeting.getLastMeetingDateTime());

            //set meetingFrequency and Duration
            meeting.setMeetingFrequency(meeting.getMeetingFrequency());
            meeting.setMeetingDurationInSeconds(meeting.getMeetingDurationInSeconds());

            //set Meeting time
            meeting.setMeetingStartDateTime(null);
            meeting.setMeetingEndDateTime(null);

            meeting.setMeetingAvailabilities(meetingAvailabilities);


            Map<String, Boolean> hasUserVoted = new HashMap<>();
            for (String userId : meeting.getHasUserVoted().keySet()){
                hasUserVoted.putIfAbsent(userId, false);
            }
            meeting.setHasUserVoted(hasUserVoted);

            //re
            meetingRepository.save(meeting);

            return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
        }
        //Need to think of how to reschedule, what timeline to give (if frequency 1 or if anything else)

    }

    // this method adds the vote
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

        LocalDateTime firstMeetingDateTime = meeting.getFirstMeetingDateTime();
        LocalDateTime lastMeetingDateTime = meeting.getLastMeetingDateTime();

        int userCount = meeting.getUserCount();

        //set the availability votes with the new numbers after a user has voted
        meeting.setMeetingAvailabilities(availabilitiesVotes);

        //set the userVoted to true for the userId
        Map<String, Boolean> usersVoted = meeting.getHasUserVoted();
        usersVoted.put(userId, true);

        //check if everyone has voted
        boolean hasAllVoted = true;
        for (String userVoted : usersVoted.keySet()){
            if (usersVoted.get(userVoted) == false){
                hasAllVoted = false;
                break;
            }
        }

        //if not all has voted then just return user voted
        if (!hasAllVoted) {
            return new ResponseEntity<String>("User Voted Successfully", HttpStatus.OK);
        }

        //if all has voted, then need to generate meeting timing, check for frequency and generate all meetings and
        //their status
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
        meeting.setIsMeetingSet(true);

        if  (meeting.getMeetingFrequency().compareTo("Once") == 0){
            return new ResponseEntity <Meeting> (meeting, HttpStatus.OK);
        }

        //for repeated meetings, check the frequency
        int weekCount = 0;
        if (meeting.getMeetingFrequency().compareTo("Weekly") == 0){
            weekCount = 1;
        } else if (meeting.getMeetingFrequency().compareTo("Fortnightly") == 0) {
            weekCount = 2;
        } else if (meeting.getMeetingFrequency().compareTo("Monthly") == 0) {
            weekCount = 4;
        }

        //get the list of next Meeting timings (After the first one)
        Map<String, Boolean> nextMeetingTimings = new TreeMap<>();
        nextMeetingTimings = meetingService.getConsecutiveMeetingTimings(meeting, weekCount);

        //get the team for the meeting
        Optional<Team> optionalTeam = teamRepository.findById(meeting.getMeetingTeamId());
        Team team = new Team();
        if (optionalTeam.isPresent()){
            team = optionalTeam.get();
        }

        //find the list of unavailable timings
        List<Interval> unavailableTimings = meetingService.getUnavailableTimings(team, firstMeetingDateTime, lastMeetingDateTime);

        //Create the next meetings and put all meetings into a map
        Map<Meeting, Boolean> meetings = new HashMap<>();

        //set the first meeting into the meeting map
        meetings.putIfAbsent(meeting, true);

        //for each meeting timing
        for (String meetingTiming : nextMeetingTimings.keySet()){

            Meeting newMeeting = new Meeting();
            String[] array = meetingTiming.split("_");
            LocalDateTime newMeetingStartTime = LocalDateTime.parse(array[0], formatter);
            LocalDateTime newMeetingEndTime = LocalDateTime.parse(array[1], formatter);

            //to check if the meeting has conflict or not
            Boolean meetingHasNoConflict = true;

            //if the event timing is same time / within the time of the event, then put as false, indicate need to reschedule
            //check if start time equal, start time within the timing, end time within the timing, end time equal end time
            for (Interval unavailableTime : unavailableTimings){
                if (unavailableTime.getStartDateTime().isEqual(newMeetingStartTime) ||
                        unavailableTime.getStartDateTime().isBefore(newMeetingEndTime) && unavailableTime.getStartDateTime().isAfter(newMeetingStartTime) ||
                        unavailableTime.getEndDateTime().isAfter(newMeetingStartTime) && unavailableTime.getEndDateTime().isBefore(newMeetingEndTime) ||
                        unavailableTime.getEndDateTime().isEqual(newMeetingEndTime)){

                    //if there is a clash in timing
                    meetingHasNoConflict = false;
                    break;
                }
            }

            //set new meeting parameters
            newMeeting.setMeetingTeamId(meeting.getMeetingTeamId());
            newMeeting.setMeetingName(meeting.getMeetingName());
            newMeeting.setUserCount(meeting.getUserCount());
            newMeeting.setHasNoConflicts(meetingHasNoConflict);
            newMeeting.setIsMeetingSet(true);

            //set FirstMeetingDateTime and LastMeetingDateTime
            newMeeting.setFirstMeetingDateTime(meeting.getFirstMeetingDateTime());
            newMeeting.setLastMeetingDateTime(meeting.getLastMeetingDateTime());

            //set meetingFrequency and Duration
            newMeeting.setMeetingFrequency(meeting.getMeetingFrequency());
            newMeeting.setMeetingDurationInSeconds(meeting.getMeetingDurationInSeconds());

            //set Meeting time
            newMeeting.setMeetingStartDateTime(newMeetingStartTime);
            newMeeting.setMeetingEndDateTime(newMeetingEndTime);

            //set meetingAvailabilities and hasUserVoted to null
            newMeeting.setMeetingAvailabilities(null);
            newMeeting.setHasUserVoted(null);

            //put into the map to be returned
            meetings.putIfAbsent(newMeeting, meetingHasNoConflict);

            //save the new meetings
            meetingRepository.save(newMeeting);
        }
        //returns the all the meetings
        return new ResponseEntity <Map<Meeting, Boolean>> (meetings, HttpStatus.OK); //need send notification
    }
}