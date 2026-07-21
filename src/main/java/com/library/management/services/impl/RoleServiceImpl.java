package com.library.management.services.impl;

import com.library.management.entities.Role;
import com.library.management.repositories.RoleRepository;
import com.library.management.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    @Override
    public Role findById(Long id) {
        return roleRepository.findRoleById(id);
    }
}
