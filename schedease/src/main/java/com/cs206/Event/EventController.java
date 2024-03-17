//package com.cs206.Event;
//
//import java.nio.file.Path;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/event")
//public class EventController {
//    @Autowired
//    private EventService eventService;
//    @Autowired
//    private EventRepository eventRepository;
//
//    @GetMapping("/getEvents")
//    public ResponseEntity<List<Event>> getAllEvents(){
//        return new ResponseEntity<List<Event>>(eventService.allEvents(), HttpStatus.OK);
//    }
//
//    @ResponseStatus(HttpStatus.CREATED)
//    @PostMapping("/addEvent")
//    public Event addEvent(@RequestBody Event event){
//        return eventService.save(event);
//    }
//
//    @GetMapping("/{eventId}/getEventById")
//    public ResponseEntity<Event> getEventById(@PathVariable(value = "eventId")String eventId){
//        Optional<Event> optionalEvent = eventRepository.findById(eventId);
//        Event event = new Event();
//        if (optionalEvent.isPresent()){
//            event = optionalEvent.get();
//        }
//
//        return new ResponseEntity<Event>(event, HttpStatus.OK);
//    }
//
//    @PostMapping("/{eventName}/{userId}/{eventStartDateTime}/{eventStartEndTime}/createEvent")
//    public ResponseEntity<Event> createEvent(@PathVariable(value = "eventName") String eventName,
//                                            @PathVariable(value = "userId") String userId,
//                                            @PathVariable(value = "eventStartDateTime") LocalDateTime eventStartDateTime,
//                                            @PathVariable(value = "eventEndDateTime") LocalDateTime eventEndDateTime){
//        Event event = new Event();
//        event.setEventName(eventName);
//        event.setEventUserId(userId);
//        event.setEventStartDateTime(eventStartDateTime);
//        event.setEventEndDateTime(eventEndDateTime);
//        return new ResponseEntity<Event>(event, HttpStatus.OK);
//    }
//}
