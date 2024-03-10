package com.cs206.Meeting;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MeetingNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public MeetingNotFoundException(String meetingId){
        super("Could not find meeting" + meetingId);
    }
}

