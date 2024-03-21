package com.cs206.Meeting;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.cs206.GoogleCalendarAPI.GoogleCalendarAPIService;
import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

//import com.cs206.Event.EventRepository;
import com.cs206.Interval.*;
import com.cs206.Team.Team;
import com.cs206.Team.TeamRepository;
import com.cs206.User.User;
import com.cs206.User.UserRepository;

@Service
public class MeetingService {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    @Autowired
    private MeetingRepository meetingRepository;

//    @Autowired
//    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private GoogleCalendarAPIService googleCalendarAPIService;

    public List<Meeting> allEvents() {
        // TODO Auto-generated method stub
        return meetingRepository.findAll();
    }

    public List<Interval> findCommonAvailableTimes(Interval timeFrame, List<Interval> unavailableTimings,
            long meetingSeconds) {

        List<Interval> availableTimes = new ArrayList<>();

        /*find the available times from the mergedUnavailableTimings list, check if the duration for the available timing is
        more than equal to the meeting timing (in seconds), if yes, add it to the list of available timings
         */
        LocalDateTime start = timeFrame.getStartDateTime();
        // for each unavailableTiming, check between the end and the start to see if there are any common slots
        for (Interval interval : unavailableTimings) {
            LocalDateTime freeEnd = interval.getStartDateTime();
            // check if the available time is more than the meetingDuration, if
            long durationInSeconds = Duration.between(start, freeEnd).getSeconds();
            if (durationInSeconds >= meetingSeconds) {
                start.format(formatter);
                freeEnd.format(formatter);
                availableTimes.add(new Interval(start, freeEnd));
            }
            start = interval.getEndDateTime();
        }

        //add the last available timing if there is, between the last unavailable endTime and the end of meeting parameters
        long durationInSeconds = Duration.between(start, timeFrame.getEndDateTime()).getSeconds();
        if (durationInSeconds >= meetingSeconds) {
            start.format(formatter);
            LocalDateTime endTime = timeFrame.getEndDateTime();
            endTime.format(formatter);
            availableTimes.add(new Interval(start, endTime));
        }



        //create slots list for potential meeting timings
        List<Interval> slots = new ArrayList<>();
//        System.out.println(availableTimes.get(0));
        /*from each available time generate slots of the stated meeting time in 30 min blocks eg. for 2 - 4, 1 hr
        slots, 30 min blocks, generates 2 - 3, 230 - 330, 3 - 4
         */
        for (Interval interval : availableTimes){
            LocalDateTime slotStart = interval.getStartDateTime();
            LocalDateTime slotEnd = slotStart.plusSeconds(meetingSeconds);

            while (slotEnd.isBefore(interval.getEndDateTime()) || slotEnd.equals(interval.getEndDateTime())) {
                slots.add(new Interval(slotStart, slotEnd));
                slotStart = slotStart.plusMinutes(30);
                slotEnd = slotEnd.plusMinutes(30);
            }
        }
        return slots;
    }

    //how to algo it?
    public Map<String, Boolean> getConsecutiveMeetingTimings(Meeting firstMeeting, Integer weekCount){
        Map<String, Boolean> nextMeetingTimings = new TreeMap<>();

        //first meeting timing
        Interval firstMeetingTiming = new Interval(firstMeeting.getMeetingStartDateTime(), firstMeeting.getMeetingEndDateTime());

        //meeting Time Limit
        Interval meetingLimit = new Interval(firstMeeting.getFirstMeetingDateTime(), firstMeeting.getLastMeetingDateTime());
        //start from the next possible meeting timing
        LocalDateTime nextMeetingStartDateTime = firstMeeting.getMeetingStartDateTime().plusWeeks(weekCount);
        LocalDateTime nextMeetingEndDateTime = firstMeeting.getMeetingEndDateTime().plusWeeks(weekCount);

        //check if the nextMeetingTiming is within the timeLimit
        while (nextMeetingEndDateTime.isBefore(meetingLimit.getEndDateTime()) ||
                (nextMeetingEndDateTime.isEqual(meetingLimit.getEndDateTime()))){
//            Interval nextMeetingTime = new Interval(nextMeetingStartDateTime, nextMeetingEndDateTime);
//            String nextMeetingTimeString = nextMeetingTime.convertToString();

            String nextMeetingTimeString = nextMeetingStartDateTime.format(formatter) + "_" + nextMeetingEndDateTime.format(formatter);

            nextMeetingTimings.putIfAbsent(nextMeetingTimeString, true);

            nextMeetingStartDateTime = nextMeetingStartDateTime.plusWeeks(weekCount);
            nextMeetingEndDateTime = nextMeetingEndDateTime.plusWeeks(weekCount);
        }

        for (String nextMeetingTimingString : nextMeetingTimings.keySet()){
            System.out.println(nextMeetingTimingString);
        }

        return nextMeetingTimings;
    }


    //takes in team details, firstDateTimeLimit & lastDateTimeLimit of meeting
    public List<Interval> getUnavailableTimings (Team team, LocalDateTime firstDateTimeLimit, LocalDateTime lastDateTimeLimit){

        //get the list of usedIds from the team
        Set<String> userIds = team.getTeamUserIds();
        List<Interval> unavailableTimings = new ArrayList<Interval>();

        String firstDateTimeLimitString = firstDateTimeLimit.format(formatter);
        String lastDateTimeLimitString = lastDateTimeLimit.format(formatter);

        for (String userId : userIds){
            try {
                //return list of events within the time
                List<Event> unavailableEvents = googleCalendarAPIService.getEvents(userId, firstDateTimeLimitString, lastDateTimeLimitString);
                for (Event event : unavailableEvents){
                    String startTimeString = "";
                    String endTimeString = "";
                    //need to include all day event check if its busy, if not get the date
//                    if (event.getTransparency().compareTo("Opaque") == 0){
////                        startTimeString = event.getStart().getDate().toString();
////                        startTimeString = startTimeString + "T00:00:00";
////                        endTimeString = event.getEnd().getDate().toString();
////                        startTimeString = startTimeString + "T00:00:00";
//                    }

                    System.out.println(event.getStart());
                    System.out.println(event.getEnd());

                    //timing event
                    startTimeString = event.getStart().getDateTime().toString().substring(0, 19);
                    endTimeString = event.getEnd().getDateTime().toString().substring(0, 19);

                    Interval interval = new Interval(LocalDateTime.parse(startTimeString, formatter), LocalDateTime.parse(endTimeString, formatter));
                    unavailableTimings.add(interval);
                    System.out.println(interval);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return unavailableTimings;
    }

    public void addEventsToUserCalendarFromList(List<String> meetingIds){
        for (String meetingId : meetingIds){
            Event eventToAdd;
            Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
            if (optionalMeeting.isPresent()) {
                Meeting meeting = optionalMeeting.get();
                Optional<Team> optionalTeam = teamRepository.findById(meeting.getMeetingTeamId());
                if (optionalTeam.isPresent()) {
                    Team team = optionalTeam.get();
                    String[] userIds = team.getTeamUserIds().toArray(new String[0]);
                    for (String userId : userIds) {
                        eventToAdd = googleCalendarAPIService.buildEvent(meeting.getMeetingName(), meeting.getMeetingStartDateTime(), meeting.getMeetingEndDateTime());
                        try {
                            googleCalendarAPIService.addEvent(userId, eventToAdd);
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    public void addEventToUserCalendar(String meetingId){
        Event eventToAdd;
        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
        if (optionalMeeting.isPresent()) {
            Meeting meeting = optionalMeeting.get();
            Optional<Team> optionalTeam = teamRepository.findById(meeting.getMeetingTeamId());
            if (optionalTeam.isPresent()) {
                Team team = optionalTeam.get();
                String[] userIds = team.getTeamUserIds().toArray(new String[0]);
                for (String userId : userIds) {
                    eventToAdd = googleCalendarAPIService.buildEvent(meeting.getMeetingName(), meeting.getMeetingStartDateTime(), meeting.getMeetingEndDateTime());
                    try {
                        googleCalendarAPIService.addEvent(userId, eventToAdd);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }
    }

}
