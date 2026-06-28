package com.library.management.services.impl;

import com.library.management.dto.LoginRequestDTO;
import com.library.management.dto.LoginedUserDTO;
import com.library.management.dto.RegisterRequestDTO;
import com.library.management.entities.Role;
import com.library.management.entities.User;
import com.library.management.repositories.RoleRepository;
import com.library.management.repositories.UserRepository;
import com.library.management.services.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void register(RegisterRequestDTO registerRequest) {
        if(userRepository.existsByUsername(registerRequest.getUsername())){
            throw new RuntimeException("Username already exist ");
        }
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new RuntimeException("Gmail already exist ");
        }
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setFullName(registerRequest.getFullName());
        user.setPhone(registerRequest.getPhone());
        user.setAddress(registerRequest.getAddress());
        user.setDob(registerRequest.getDob());

        user.setEnabled(true);

        Role readerRole = roleRepository.findById(1L).orElseThrow(()-> new RuntimeException("Reader role not found "));

        user.setRole(readerRole);
        System.out.println("Before save");
        userRepository.save(user);
        System.out.println("After save");

    }

    @Override
    public boolean login(LoginRequestDTO dto) {

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getPassword().equals(dto.getPassword());
    }

    @Override
    public LoginedUserDTO getLoginUser(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findUserByUsernameAndPassword(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());
        return new LoginedUserDTO(user.getUsername(), user.getRole().getName(), user.getEmail());
    }
}
