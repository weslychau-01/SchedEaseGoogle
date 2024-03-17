package com.cs206.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.cs206.Interval.Interval;
import com.cs206.Meeting.Meeting;
import com.cs206.Meeting.MeetingRepository;
import com.cs206.Meeting.MeetingService;
import com.cs206.Team.Team;
import com.cs206.Team.TeamRepository;
import com.cs206.Team.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamService teamService;



    public List<User> allUsers() {
        return userRepository.findAll();
    }

    public void saveMeetingForTeamUsers(Set<String> userIds, String meetingId) {
        //for each user for them and add the new meeting Ids and save the meeting
        for (String userId : userIds) {
            Optional<User> optionalUser = userRepository.findById(userId);
            User user = new User();
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            }

            Set<String> userMeetingIds = user.getUserMeetingIds();
            userMeetingIds.add(meetingId);
            user.setUserMeetingIds(userMeetingIds);
            userRepository.save(user);

        }

    }

    public void saveAllMeetingsForTeamUsers(Set<String> userIds, List<String> meetingIds) {
        //for each user for them and add all the new meetingIds and save the meeting
        for (String userId : userIds) {
            Optional<User> optionalUser = userRepository.findById(userId);
            User user = new User();
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            }

            Set<String> userMeetingIds = user.getUserMeetingIds();
            userMeetingIds.addAll(meetingIds);
            user.setUserMeetingIds(userMeetingIds);
            userRepository.save(user);

        }

    }

    //update availabilities for other meetings upon confirmation of a single meeting
    public void updateAvailabilitiesForAllPendingMeetings(Set<String> userIds, Interval meetingTiming) {
        for (String userId : userIds) {
            Optional<User> optionalUser = userRepository.findById(userId);
            User user = new User();
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            }

            Set<String> meetingIds = user.getUserMeetingIds();
            if (meetingIds.isEmpty()){
                return;
            }
            System.out.println(meetingTiming.getStartDateTime());
            for (String meetingId : meetingIds) {
                Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
                Meeting meeting = new Meeting();
                if (optionalMeeting.isPresent()) {
                    System.out.println("Meeting present");
                    meeting = optionalMeeting.get();
                }

                if (meeting.getIsMeetingSet()) {
                    continue;
                } else {
                    System.out.println(meeting.getId());
                    Map<String, Integer> meetingAvailabilities = meeting.getMeetingAvailabilities();
                    Map<String, Integer> newMeetingAvailabilities = new TreeMap<>();
                    for (String meetingAvailability : meetingAvailabilities.keySet()) {
                        String[] array = meetingAvailability.split("_");
                        LocalDateTime availableTimingStartDateTime = LocalDateTime.parse(array[0], formatter);
                        LocalDateTime availableTimingEndDateTime = LocalDateTime.parse(array[1], formatter);

                        if (!(meetingTiming.getStartDateTime().isEqual(availableTimingStartDateTime) ||
                                meetingTiming.getStartDateTime().isBefore(availableTimingEndDateTime) && meetingTiming.getStartDateTime().isAfter(availableTimingStartDateTime) ||
                                meetingTiming.getEndDateTime().isAfter(availableTimingStartDateTime) && meetingTiming.getEndDateTime().isBefore(availableTimingEndDateTime) ||
                                meetingTiming.getEndDateTime().isEqual(availableTimingEndDateTime))) {


                            System.out.println(availableTimingStartDateTime);


                            newMeetingAvailabilities.putIfAbsent(meetingAvailability, meetingAvailabilities.get(meetingAvailability));
                        }
                    }

                    for (String meetingAvailability : newMeetingAvailabilities.keySet()){
                        System.out.println(meetingAvailability);
                    }

                    meeting.setMeetingAvailabilities(newMeetingAvailabilities);
                }
                meetingRepository.save(meeting);
            }
        }
    }

    //update availabilities for upon confirmation of multi-frequency meetings
    public void updateAvailabilitiesForAllPendingMeetingsWithMultipleMeetings(Set<String> userIds, List<Interval> meetingTimings){
        for (String userId : userIds) {
            Optional<User> optionalUser = userRepository.findById(userId);
            User user = new User();
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            }

            Set<String> meetingIds = user.getUserMeetingIds();
            for (String meetingId : meetingIds) {
                Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
                Meeting meeting = new Meeting();
                if (optionalMeeting.isPresent()) {
                    meeting = optionalMeeting.get();
                }

                if (meeting.getIsMeetingSet()) {
                    continue;
                } else {
                    Map<String, Integer> meetingAvailabilities = meeting.getMeetingAvailabilities();
                    Map<String, Integer> newMeetingAvailabilities = meeting.getMeetingAvailabilities();
                    for (String meetingAvailability : meetingAvailabilities.keySet()) {
                        String[] array = meetingAvailability.split("_");
                        LocalDateTime availableTimingStartDateTime = LocalDateTime.parse(array[0], formatter);
                        LocalDateTime availableTimingEndDateTime = LocalDateTime.parse(array[1], formatter);

                        for (Interval meetingTiming : meetingTimings){
                            if (!(meetingTiming.getStartDateTime().isEqual(availableTimingStartDateTime) ||
                                    meetingTiming.getStartDateTime().isBefore(availableTimingEndDateTime) && meetingTiming.getStartDateTime().isAfter(availableTimingStartDateTime) ||
                                    meetingTiming.getEndDateTime().isAfter(availableTimingStartDateTime) && meetingTiming.getEndDateTime().isBefore(availableTimingEndDateTime) ||
                                    meetingTiming.getEndDateTime().isEqual(availableTimingEndDateTime))) {
                                newMeetingAvailabilities.putIfAbsent(meetingAvailability, meetingAvailabilities.get(meetingAvailability));
                            }
                        }
                    }
                    meeting.setMeetingAvailabilities(newMeetingAvailabilities);
                }
                meetingRepository.save(meeting);
            }
        }
    }
}
