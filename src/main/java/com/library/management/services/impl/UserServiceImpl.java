package com.library.management.services.impl;

import com.library.management.dto.ChangePasswordDTO;
import com.library.management.dto.LoginRequestDTO;
import com.library.management.dto.LoginedUserDTO;
import com.library.management.dto.RegisterRequestDTO;
import com.library.management.dto.UpdateProfileDTO;
import com.library.management.entities.Role;
import com.library.management.entities.User;
import com.library.management.exception.*;
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
import java.util.List;
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
            throw new UsernameAlreadyExistException("Username already exist ");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new GmailAlreadyExistException("Gmail already exist ");
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

        Role readerRole = roleRepository.findById(3L).orElseThrow(() -> new RuntimeException("Reader role not found "));
        user.setRole(readerRole);
        userRepository.save(user);
    }

    @Override
    public User login(LoginRequestDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UsernameNotExistException("Tài khoản không tồn tại"));

        if (!user.getEnabled()) {
            throw new WrongPasswordOrUserNameException("Tài khoản chưa được kích hoạt");
        }

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new WrongPasswordOrUserNameException("Sai tên đăng nhập hoặc mật khẩu");
        }
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    @Override
    public void updateProfile(String username, UpdateProfileDTO dto) {
        User user = getByUsername(username);


        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAddress() != null) user.setAddress(dto.getAddress());

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
            throw new WrongCurrentPasswordException("Mật khẩu hiện tại không đúng");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new WrongComfirmPasswordException("Mật khẩu xác nhận không khớp");
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
            throw new CanNotSaveAvartaException("Không thể lưu ảnh đại diện: " + e.getMessage());
        }
    }

    @Override
    public LoginedUserDTO getLoginUser(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findUserByUsernameAndPassword(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());
        return new LoginedUserDTO(user.getUsername(), user.getRole().getName(), user.getEmail());
    }

    @Override
    public List<User> findAllReader() {
        return userRepository.findAllReader();
    }

    @Override
    public List<User> searchReader(String keyword) {
        return userRepository.searchReader(keyword);
    }

    @Override
    public void update(User user) {
        userRepository.save(user);
    }

    @Override
    public User findById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public boolean searchUser(User oldUser, User newUser) {
        if(
                userRepository.searchUser(oldUser.getEmail()) != null&&
                userRepository.searchUser(oldUser.getEmail()).getId() == newUser.getId()&&
                userRepository.searchUser(oldUser.getPhone()) != null&&
                userRepository.searchUser(oldUser.getPhone()).getId() == newUser.getId()){
            return true;
        }
        return false;
    }
}
