package com.library.management.services.impl;

import com.library.management.dto.ChangePasswordDTO;
import com.library.management.dto.LoginRequestDTO;
import com.library.management.dto.LoginedUserDTO;
import com.library.management.dto.RegisterRequestDTO;
import com.library.management.dto.UpdateProfileDTO;
import com.library.management.entities.Role;
import com.library.management.entities.User;
import com.library.management.repositories.RoleRepository;
import com.library.management.repositories.UserRepository;
import com.library.management.services.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void register(RegisterRequestDTO registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exist ");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
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

        Role readerRole = roleRepository.findById(1L).orElseThrow(() -> new RuntimeException("Reader role not found "));
        user.setRole(readerRole);
        userRepository.save(user);
    }

    @Override
    public User login(LoginRequestDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("Sai tên đăng nhập hoặc mật khẩu");
        }
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Override
    public void updateProfile(String username, UpdateProfileDTO dto) {
        User user = getByUsername(username);

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());
        if (dto.getDob() != null) user.setDob(dto.getDob());

        // Handle avatar upload
        MultipartFile avatarFile = dto.getAvatarFile();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = saveAvatar(avatarFile, username);
            user.setAvatar(avatarUrl);
        }

        userRepository.save(user);
    }

    @Override
    public void changePassword(String username, ChangePasswordDTO dto) {
        User user = getByUsername(username);

        if (!user.getPassword().equals(dto.getCurrentPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }
        user.setPassword(dto.getNewPassword());
        userRepository.save(user);
    }

    private String saveAvatar(MultipartFile file, String username) {
        try {
            // Save to static/uploads/avatars/
            String uploadDir = "src/main/resources/static/uploads/avatars";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String fileName = username + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/avatars/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu ảnh đại diện: " + e.getMessage());
        }
    }

    @Override
    public LoginedUserDTO getLoginUser(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findUserByUsernameAndPassword(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());
        return new LoginedUserDTO(user.getUsername(), user.getRole().getName(), user.getEmail());
    }
}
