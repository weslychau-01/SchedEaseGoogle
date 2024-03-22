package com.cs206.Meeting;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.cs206.GoogleCalendarAPI.GoogleCalendarAPIService;
import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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

    public Event addEventsToGoogleCalendar(String meetingId) throws IOException, GeneralSecurityException, Exception {
        Event eventToAdd;
        Event addedEvent = new Event();
        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
        if (optionalMeeting.isPresent()) {
            Meeting meeting = optionalMeeting.get();
            Optional<Team> optionalTeam = teamRepository.findById(meeting.getMeetingTeamId());
            if (optionalTeam.isPresent()) {
                Team team = optionalTeam.get();
                String[] userIds = team.getTeamUserIds().toArray(new String[0]);
                for (String userId : userIds) {
                    eventToAdd = googleCalendarAPIService.buildEvent(meeting.getMeetingName(), meeting.getMeetingStartDateTime(), meeting.getMeetingEndDateTime());
                    addedEvent = googleCalendarAPIService.addEvent(userId, eventToAdd);
                }
            } else {return null;}
        } else {return null;}
        return addedEvent;
    }

    public void deleteEventsFromGoogleCalendar (String meetingId) throws IOException, GeneralSecurityException, Exception {
        Event eventToDelete;
        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
        if (optionalMeeting.isPresent()) {
            Meeting meeting = optionalMeeting.get();
            Optional<Team> optionalTeam = teamRepository.findById(meeting.getMeetingTeamId());
            if (optionalTeam.isPresent()) {
                Team team = optionalTeam.get();
                String[] userIds = team.getTeamUserIds().toArray(new String[0]);
                for (String userId : userIds) {
                    eventToDelete = googleCalendarAPIService.buildEvent(meeting.getMeetingName(), meeting.getMeetingStartDateTime(), meeting.getMeetingEndDateTime());
                    googleCalendarAPIService.deleteEvent(userId, eventToDelete);
                }
            } 
        } 
        return;
    }

    public List<Meeting> allEvents() {
        // TODO Auto-generated method stub
        return meetingRepository.findAll();
    }

    public List<Interval> findCommonAvailableTimes(Interval timeFrame, List<Interval> unavailableTimings,
            long meetingSeconds) {
        int startHour = timeFrame.getStartDateTime().getHour();
        int startMin = timeFrame.getStartDateTime().getMinute();
        int endHour = timeFrame.getEndDateTime().getHour();
        int endMin = timeFrame.getEndDateTime().getMinute();

        List<Interval> availableTimes = new ArrayList<>();

        /*find the available times from the mergedUnavailableTimings list, check if the duration for the available timing is
        more than equal to the meeting timing (in seconds), if yes, add it to the list of available timings
         */
        LocalDateTime start = timeFrame.getStartDateTime();
        // for each unavailableTiming, check between the end and the start to see if there are any common slots
        for (Interval interval : unavailableTimings) {
            LocalDateTime freeEnd = interval.getStartDateTime();
            // check if the available time is more than the meetingDuration, if
            if (start.isAfter(freeEnd)){
                if (start.isAfter(interval.getEndDateTime())){
                    continue;
                }
                else {
                    start = interval.getEndDateTime();
                    continue;
                }
            }
            long durationInSeconds = Duration.between(start, freeEnd).getSeconds();
            if (durationInSeconds >= meetingSeconds) {
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

        for (Interval interval : availableTimes){

//            System.out.println(interval);

            if (interval.getStartDateTime().getMinute() != 30 && interval.getStartDateTime().getMinute() != 0){
                interval.setStartDateTime(interval.getStartDateTime().plusMinutes(Math.abs(30 - interval.getStartDateTime().getMinute())));
            }

            if (interval.getEndDateTime().getMinute() != 30 && interval.getEndDateTime().getMinute() != 0){
                interval.setEndDateTime(interval.getEndDateTime().minusMinutes(Math.abs(30 - interval.getEndDateTime().getMinute())));
            }
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
                boolean b = (((slotStart.isAfter(LocalDateTime.of(slotStart.getYear(), slotStart.getMonth(), slotStart.getDayOfMonth(), startHour, startMin))) || (slotStart.isEqual(LocalDateTime.of(slotStart.getYear(), slotStart.getMonth(), slotStart.getDayOfMonth(), startHour, startMin)))) &&
                        ((slotEnd.isBefore(LocalDateTime.of(slotStart.getYear(), slotStart.getMonth(), slotStart.getDayOfMonth(), endHour, endMin))) || (slotEnd.isEqual(LocalDateTime.of(slotStart.getYear(), slotStart.getMonth(), slotStart.getDayOfMonth(), endHour, endMin)))));

                if (b) {
                    slots.add(new Interval(slotStart, slotEnd));
                }

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

//        for (String nextMeetingTimingString : nextMeetingTimings.keySet()){
//            System.out.println(nextMeetingTimingString);
//        }

        return nextMeetingTimings;
    }


    //takes in team details, firstDateTimeLimit & lastDateTimeLimit of meeting
    public List<Interval> getUnavailableTimings (Team team, LocalDateTime firstDateTimeLimit, LocalDateTime lastDateTimeLimit){

        //get the list of usedIds from the team
        Set<String> userIds = team.getTeamUserIds();
        List<Interval> unavailableTimings = new ArrayList<Interval>();

        LocalDateTime starttimeLimit = LocalDateTime.of(firstDateTimeLimit.getYear(), firstDateTimeLimit.getMonth(), firstDateTimeLimit.getDayOfMonth(), 0, 0, 0);
        LocalDateTime endtimeLimit = LocalDateTime.of(lastDateTimeLimit.getYear(), lastDateTimeLimit.getMonth(), lastDateTimeLimit.getDayOfMonth(), 23, 59, 59);

        String firstDateTimeLimitString = starttimeLimit.format(formatter);
        String lastDateTimeLimitString = endtimeLimit.format(formatter);

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

                    //if all day event, if not
                    if (event.getStart().getDateTime() == null){
                        startTimeString = event.getStart().getDate().toString() + "T00:00:00";
                        endTimeString = event.getEnd().getDate().toString() + "T23:59:59";
                    } else {
                        startTimeString = event.getStart().getDateTime().toString().substring(0, 19);
                        endTimeString = event.getEnd().getDateTime().toString().substring(0, 19);
                    }


                    Interval interval = new Interval(LocalDateTime.parse(startTimeString, formatter), LocalDateTime.parse(endTimeString, formatter));
                    unavailableTimings.add(interval);
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
                    System.out.println(userId);
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
