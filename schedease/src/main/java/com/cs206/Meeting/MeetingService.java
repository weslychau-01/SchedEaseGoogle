package com.cs206.Meeting;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cs206.Interval.*;

@Service
public class MeetingService {
    @Autowired
    private MeetingRepository meetingRepository;

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

}
