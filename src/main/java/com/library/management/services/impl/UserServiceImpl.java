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
import com.library.management.services.FileStorageService;
import com.library.management.services.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FileStorageService fileStorageService;

    @Override
    public void register(RegisterRequestDTO registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new GmailAlreadyExistException("Gmail đã tồn tại");
        }
        if (!(registerRequest.getPassword().equals(registerRequest.getConfirmPassword()))) {
            throw new ComfirmPasswordNotMatchException("Confirm password không đúng");
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

        MultipartFile avatarFile = dto.getAvatarFile();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            updateAvatar(user, avatarFile);
            return;
        }

        userRepository.save(user);
    }

    private void updateAvatar(User user, MultipartFile avatarFile) {
        String oldAvatarUrl = user.getAvatar();
        String newAvatarUrl;

        try {
            newAvatarUrl = fileStorageService.saveFile(avatarFile, "avatars");
        } catch (RuntimeException ex) {
            throw new CanNotSaveAvartaException(ex.getMessage());
        }

        user.setAvatar(newAvatarUrl);
        try {
            userRepository.saveAndFlush(user);
        } catch (RuntimeException ex) {
            fileStorageService.deleteFile(newAvatarUrl);
            throw ex;
        }

        if (oldAvatarUrl != null
                && !oldAvatarUrl.isBlank()
                && !oldAvatarUrl.equals(newAvatarUrl)) {
            fileStorageService.deleteFile(oldAvatarUrl);
        }
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
        if((userRepository.searchUser(oldUser.getEmail()) == null ||
                userRepository.searchUser(oldUser.getEmail()).getId() == newUser.getId())&&
                (userRepository.searchUser(oldUser.getPhone()) == null) ||
                userRepository.searchUser(oldUser.getPhone()).getId() == newUser.getId()){
            return true;
        }

        return false;
    }
}
