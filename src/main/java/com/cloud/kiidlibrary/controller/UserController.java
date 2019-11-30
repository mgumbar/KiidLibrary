package com.cloud.kiidlibrary.controller;

import com.cloud.kiidlibrary.dal.UserRepository;
import com.cloud.kiidlibrary.exceptions.NotFoundException;
import com.cloud.kiidlibrary.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @RequestMapping(method = RequestMethod.GET)
    public List<User> getAllUsers() {
        LOG.info("Getting all users.");
        return userRepository.findAll();
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public Optional<User> getUser(@PathVariable String userId) {
        LOG.info("Getting user with ID: {}.", userId);
        Optional<User> userO = userRepository.findById(userId);
        if (!userO.isPresent()) throw new NotFoundException("User with Id not found" + userId);
        return userO;
    }


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public User addNewUsers(@Valid @RequestBody User user) {
        LOG.info("Saving user.");
        return userRepository.save(user);
    }


    @RequestMapping(value = "/settings/{userId}", method = RequestMethod.GET)
    public Object getAllUserSettings(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user != null) {
            return user.get().getUserSettings();
        } else {
            return "User not found.";
        }
    }

    @RequestMapping(value = "/settings/{userId}/{key}", method = RequestMethod.GET)
    public String getUserSetting(@PathVariable String userId, @PathVariable String key) {
        Optional<User> user = userRepository.findById(userId);
        if (user != null) {
            return user.get().getUserSettings().get(key);
        } else {
            return "User not found.";
        }
    }


    @RequestMapping(value = "/settings/{userId}/{key}/{value}", method = RequestMethod.GET)
    public String addUserSetting(@PathVariable String userId, @PathVariable String key, @PathVariable String value) {
        Optional<User> userO = userRepository.findById(userId);
        if (userO != null) {
            User user = userO.get();
            user.getUserSettings().put(key, value);
            userRepository.save(user);
            return "Key added";
        } else {
            return "User not found.";
        }
    }

}