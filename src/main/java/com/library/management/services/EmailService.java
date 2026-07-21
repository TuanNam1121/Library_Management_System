package com.library.management.services;

import com.library.management.entities.BorrowDetail;

public interface EmailService {
    void sendOverdueReminder(BorrowDetail detail);
}
