package com.cloud.kiidlibrary.controller;

import com.cloud.kiidlibrary.dal.UserDAL;
import com.cloud.kiidlibrary.dal.UserRepository;
import com.cloud.kiidlibrary.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/userDal")
public class UserDalController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;
    //define Data Access Layer object
    private final UserDAL userDAL;

    public UserDalController(UserRepository userRepository, UserDAL userDAL) {
        this.userRepository = userRepository;
        this.userDAL = userDAL;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<User> getAllUsers() {
        LOG.info("Getting all users.");
        return userRepository.findAll();
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public Optional<User> getUser(@PathVariable String userId) {
        LOG.info("Getting user with ID: {}.", userId);
        return userRepository.findById(userId);
    }


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public User addNewUsers(@RequestBody User user) {
        LOG.info("Saving user.");
        return userRepository.save(user);
    }


    //change method implementation to use DAL and hence MongoTemplate
    @RequestMapping(value = "/settings/{userId}", method = RequestMethod.GET)
    public Object getAllUserSettings(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user != null) {
            return userDAL.getAllUserSettings(userId);
        } else {
            return "User not found.";
        }
    }

    //change method implementation to use DAL and hence MongoTemplate
    @RequestMapping(value = "/settings/{userId}/{key}", method = RequestMethod.GET)
    public String getUserSetting(
            @PathVariable String userId, @PathVariable String key) {
        return userDAL.getUserSetting(userId, key);
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