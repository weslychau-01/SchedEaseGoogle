//package com.cs206.Event;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.cglib.core.Local;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.time.LocalDateTime;
//
//@Document(collection = "events")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class Event {
//    @Id
//    private String id;
//    private String eventName;
//    private String eventUserId;
//    private LocalDateTime eventStartDateTime;
//    private LocalDateTime eventEndDateTime;
//}