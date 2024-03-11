package com.cs206.Meeting;


import java.time.LocalDateTime;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "meeting")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Meeting {
    @Id
    private String id;
    private String meetingName;
    private String meetingTeamId;
    private int userCount;
    private LocalDateTime firstMeetingDateTime;
    private LocalDateTime lastMeetingDateTime;
    private LocalDateTime meetingStartDateTime;
    private LocalDateTime meetingEndDateTime;
    private long meetingDurationInSeconds;
    private String meetingFrequency;
    private Map<String, Integer> meetingAvailabilities;
    private Map<String, Boolean> hasUserVoted;
    private Boolean hasNoConflicts;
    private Boolean isMeetingSet;
}