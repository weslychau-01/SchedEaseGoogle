package com.cs206.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> allUsers(){
        return userRepository.findAll();
    }

    public User save (User user) {
        // TODO Auto-generated method stub
        return userRepository.save(user);
    }

    // public User getUserByUserId (String userId) {
    //     // TODO Auto-generated method stub
    //     return userRepository.findUserByUserId(userId);
    // }
}
