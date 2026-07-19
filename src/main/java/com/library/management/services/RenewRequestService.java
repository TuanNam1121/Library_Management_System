package com.library.management.services;

import com.library.management.dto.RenewRequestDTO;

public interface RenewRequestService {
    void createRequest(Long borrowDetailId, String username, RenewRequestDTO renewRequestDTO);
    void approve(Long renewRequestId, String librarianUsername, String librarianNote);
    void reject(Long renewRequestId, String librarianUsername, String librarianNote);
}
