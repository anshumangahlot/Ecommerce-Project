package com.project.service;

import com.project.entity.User;
import java.util.List;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User registerUser(User user){
        if(userRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        return userRepository.save(user);
    }

    public User getUserById(Long id){
        return userRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("User not found with id: " + id)
        );
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
}
