package com.cs206.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import com.Encryption.*;

import javax.crypto.SecretKey;

import com.cs206.GoogleCalendarAPI.GoogleCalendarAPIService;
import com.cs206.Meeting.Meeting;
import com.cs206.Meeting.MeetingRepository;
import com.cs206.Team.Team;
import com.cs206.Team.TeamRepository;
import com.google.api.services.calendar.model.Event;
import com.sun.source.tree.Tree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.Encryption.EncryptionUtil;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoogleCalendarAPIService googleCalendarAPIService;

    @GetMapping("/getUsers")
    public ResponseEntity<?> getAllUsers() {
        List<User> allUsers = userService.allUsers();
        if (allUsers == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found");
        }
        return new ResponseEntity<List<User>>(userService.allUsers(), HttpStatus.OK);
    }

    @GetMapping("/{userId}/getUser")
    public ResponseEntity<?> getUser(@PathVariable(value = "userId") String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        User user = new User();
        if (optionalUser.isPresent()){
            user = optionalUser.get();
        }
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/addUser")
    public ResponseEntity<String> createUser(@RequestBody User user) throws Exception {
        List<String> eventId = new ArrayList<>();
        // User user = new User();
        // user.setUserName("John");
        // user.setUserEmail("john@gmail.com");
        // user.setUserPassword("john123");
//        user.setUserEventIds(new ArrayList<>());
        user.setUserMeetingIds(new TreeSet<>());
        userRepository.save(user);
        SecretKey secretKey = EncryptionUtil.generateSecretKey();
        user.setSerialisedKey(EncryptionUtil.serialiseSecretString(secretKey));
        user.setUserEventIds(eventId);
        user.setUserMeetingIds(new ArrayList<String>());
        userService.save(user);
        return new ResponseEntity<>("User Saved", HttpStatus.OK);
    }

    @PostMapping("/{userName}/{userEmail}/{userPassword}/signUp")
    public ResponseEntity<?> signUp(@PathVariable(value = "userName") String userName, @PathVariable(value = "userEmail")String userEmail,
                                    @PathVariable(value = "userPassword") String userPassword) throws Exception{
        User user = new User();

        user.setUserName(userName);
        user.setUserEmail(userEmail);
        user.setUserPassword(userPassword);
        user.setUserMeetingIds(new TreeSet<>());
        user.setTeamIds(new TreeSet<>());
        user.setEncryptedAccessToken(null);
        user.setEncryptedRefreshToken(null);
        SecretKey secretKey = EncryptionUtil.generateSecretKey();
        user.setSerialisedKey(EncryptionUtil.serialiseSecretString(secretKey));
        userRepository.save(user);

        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @PutMapping("{userId}/connectGoogleCalendar")
    public ResponseEntity<?> googleCalendarLogin(@PathVariable(value = "userId") String userId){
        try {
            googleCalendarAPIService.getCredentials(userId);
        } catch (Exception e){
            e.printStackTrace();
        }

        return new ResponseEntity<String>("Google Calendar Connected", HttpStatus.OK);
    }

    @PostMapping("{userEmail}/{userPassword}/login")
    public ResponseEntity<?> login(@PathVariable(value = "userEmail")String userEmail,
                                   @PathVariable(value = "userPassword") String userPassword){
        Optional<User> optionalUser = userRepository.findByUserEmail(userEmail);
        User user = new User();
        if (optionalUser.isPresent()){
            user = optionalUser.get();
        }

        if (user.getUserPassword().compareTo(userPassword) == 0){
            return new ResponseEntity<Boolean> (true, HttpStatus.OK);
        }

        return new ResponseEntity<Boolean>(false, HttpStatus.OK);
    }


}


