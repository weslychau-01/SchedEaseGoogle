package com.cs206.Interval;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Interval {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public long getDurationInSeconds(LocalDateTime startDateTime, LocalDateTime endDateTime){
        long seconds = Duration.between(startDateTime, endDateTime).getSeconds();
        return seconds;
    }

    public String convertToString(){
        return "" + startDateTime + "_" + endDateTime;
    }
}
