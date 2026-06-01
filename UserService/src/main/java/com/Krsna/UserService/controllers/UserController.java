package com.Krsna.UserService.controllers;

import com.Krsna.UserService.entities.User;
import com.Krsna.UserService.services.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.saveUser(user));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUser() {
        return ResponseEntity.ok(userService.getAllUser());
    }

    int retry = 1;
    @GetMapping("/{userId}")
    @Retry(name = "GetUserByUserId", fallbackMethod = "getUserFallback")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        retry++;
        log.info("Retry Count : "+retry);
//        Here we hit this api with a userId that did not exist it hit DB 3 times then returned user not found !!
        return ResponseEntity.ok(userService.getUser(userId));
    }

    public ResponseEntity<User> getUserFallback(String userId, Exception ex){
        log.info(ex.getMessage());
        User user = User.builder().userId(userId)
                    .email("This is Dummy Email")
                    .name("This is dummy name")
                    .about("This is dummy about")
                    .ratings(Collections.emptyList())
                    .build();
        return new ResponseEntity(user, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted with id " + userId);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(user, userId));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<User>> findByNameContaining(@PathVariable String name) {
        return ResponseEntity.ok(userService.findByNameContaining(name));
    }
}
