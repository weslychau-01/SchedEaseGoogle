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
                availableTimes.add(new Interval(start, freeEnd));
            }
            start = interval.getEndDateTime();
        }

        //add the last available timing if there is, between the last unavailable endTime and the end of meeting parameters
        long durationInSeconds = Duration.between(start, timeFrame.getEndDateTime()).getSeconds();
        if (durationInSeconds >= meetingSeconds) {
            availableTimes.add(new Interval(start, timeFrame.getEndDateTime()));
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
        System.out.println(firstMeetingTiming);
        //meeting Time Limit
        Interval meetingLimit = new Interval(firstMeeting.getFirstMeetingDateTime(), firstMeeting.getLastMeetingDateTime());
        System.out.println(firstMeeting.getFirstMeetingDateTime());
        //start from the next possible meeting timing
        LocalDateTime nextMeetingStartDateTime = firstMeeting.getMeetingStartDateTime().plusWeeks(weekCount);
        LocalDateTime nextMeetingEndDateTime = firstMeeting.getMeetingEndDateTime().plusWeeks(weekCount);

        //check if the nextMeetingTiming is within the timeLimit
        while (nextMeetingEndDateTime.isBefore(meetingLimit.getEndDateTime()) ||
                (nextMeetingEndDateTime.isEqual(meetingLimit.getEndDateTime()))){
            Interval nextMeetingTime = new Interval(nextMeetingStartDateTime, nextMeetingEndDateTime);
            String nextMeetingTimeString = nextMeetingTime.convertToString();
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

        String firstDateTimeLimitString = firstDateTimeLimit.toString();
        String lastDateTimeLimitString = lastDateTimeLimit.toString();

        for (String userId : userIds){
            try {
                //return list of events within the time
                List<Event> unavailableEvents = googleCalendarAPIService.getEvents(userId, firstDateTimeLimitString, lastDateTimeLimitString);
                for (Event event : unavailableEvents){
                    String startTimeString = "";
                    String endTimeString = "";
                    //need to include all day event check if its busy, if not get the date
                    if (event.getTransparency().compareTo("Opaque") == 0){
//                        startTimeString = event.getStart().getDate().toString();
//                        startTimeString = startTimeString + "T00:00:00";
//                        endTimeString = event.getEnd().getDate().toString();
//                        startTimeString = startTimeString + "T00:00:00";
                    }

                    //timing event
                    startTimeString = event.getStart().getDateTime().toString().substring(0, 19);
                    endTimeString = event.getEnd().getDateTime().toString().substring(0, 19);

                    Interval interval = new Interval(LocalDateTime.parse(startTimeString, formatter), LocalDateTime.parse(endTimeString, formatter));
                    unavailableTimings.add(interval);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return unavailableTimings;

        //get a list of userIds from the team
//        List<User> users = new ArrayList<>();
//        for (String userId : userIds){
//            User user = new User();
//            Optional<User> optionalUser = userRepository.findById(userId);
//            if (optionalUser.isPresent()){
//                user = optionalUser.get();
//                //add users to users list
//                users.add(user);
//            }
//        }

        //get eventIds from the users, for each eventId, get the event, check the time within meeting date, add it to list
//        List<Interval> unavailableTimings = new ArrayList<Interval>();
//        List<String> eventIds = new ArrayList<>();
//        for (User user : users){
//
//            //from CALENDAR
//            //get the credentials, run the service giving the first date time and last date time and getting back
//            //the timing
//
//
//            //seperate method
//            //List<Event> events = user.getUserEvents();
////            for (Event event : events){
////                LocalDateTime eventStartDate = event.getEventStartDateTime();
////                LocalDateTime eventEndDate = event.getEventEndDateTime();
////
////                //check the eventStartDate & eventEndDate is within the meeting dates
////                if ((eventStartDate.isAfter(firstDateTimeLimit) || eventStartDate.isEqual(firstDateTimeLimit))
////                        && (eventStartDate.isBefore(lastDateTimeLimit) || eventStartDate.isEqual(lastDateTimeLimit))){
////                    unavailableTimings.add(new Interval(event.getEventStartDateTime(), event.getEventEndDateTime()));
////                }
////            }
//
//            List<String> userEventIds = user.getUserEventIds();
//            //for each user, get the list of userEventIds
//            for (String userEventId : userEventIds){
//                Event event = new Event();
//                Optional<Event> optionalEvent = eventRepository.findById(userEventId);
//                //get the event
//                if (optionalEvent.isPresent()){
//                    event = optionalEvent.get();
//                    //get the startDate and endDate for the event
//                    LocalDateTime eventStartDate = event.getEventStartDateTime();
//                    LocalDateTime eventEndDate = event.getEventEndDateTime();
//
//                    //check the eventStartDate & eventEndDate is within the meeting dates
//                    if ((eventStartDate.isAfter(firstDateTimeLimit) || eventStartDate.isEqual(firstDateTimeLimit))
//                            && (eventStartDate.isBefore(lastDateTimeLimit) || eventStartDate.isEqual(lastDateTimeLimit))){
//                        unavailableTimings.add(new Interval(event.getEventStartDateTime(), event.getEventEndDateTime()));
//                    }
//                }
//            }
//        }


    }
}
