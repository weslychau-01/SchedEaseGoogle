package com.cs206.User;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/getUsers")
    public ResponseEntity<?> getAllUsers() {
        List<User> allUsers = userService.allUsers();
        if (allUsers == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found");
        }
        return new ResponseEntity<List<User>>(userService.allUsers(), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/addUsers")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        List<String> eventId = new ArrayList<>();
        // User user = new User();
        // user.setUserName("John");
        // user.setUserEmail("john@gmail.com");
        // user.setUserPassword("john123");
        user.setUserEventIds(eventId);
        user.setUserMeetingIds(new ArrayList<String>());
        userService.save(user);
        return new ResponseEntity<>("User Saved", HttpStatus.OK);
    }
}