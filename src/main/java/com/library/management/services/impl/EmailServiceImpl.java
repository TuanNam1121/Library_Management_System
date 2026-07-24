package com.library.management.services.impl;

import com.library.management.entities.BorrowDetail;
import com.library.management.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOverdueReminder(BorrowDetail detail) {
        String readerEmail = detail.getBorrowRequest().getReader().getEmail();
        String readerName = detail.getBorrowRequest().getReader().getFullName();
        String bookTitle = detail.getBook().getTitle();

        if (readerEmail == null || readerEmail.isBlank()) {
            throw new RuntimeException("Độc giả " + readerName + " chưa có email, không thể gửi thông báo.");
        }

        String textContent = "Xin chào " + readerName + ",\n\n"
                + "Đây là thông báo nhắc nhở từ thư viện. Cuốn sách bạn đang mượn đã quá hạn trả.\n\n"
                + "Thông tin sách mượn:\n"
                + "- Tên sách: " + bookTitle + "\n"
                + "- ISBN: " + detail.getBook().getIsbn() + "\n"
                + "- Mã yêu cầu: #" + detail.getBorrowRequest().getId() + "\n"
                + "- Ngày hạn: " + detail.getDueDate() + "\n\n"
                + "Việc trả sách quá hạn sẽ bị tính phí phạt theo quy định của thư viện.\n"
                + "Vui lòng mang sách đến quầy thủ thư để hoàn tất thủ tục trả sách sớm nhất có thể.\n\n"
                + "Trân trọng,\nLibraryHub";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(readerEmail);
            message.setSubject("Nhắc nhở: Sách \"" + bookTitle + "\" đã quá hạn trả!");
            message.setText(textContent);

            mailSender.send(message);
            log.info("Đã gửi email nhắc nhở quá hạn đến {} cho sách '{}'", readerEmail, bookTitle);
        } catch (Exception e) {
            log.error("Lỗi gửi email đến {}: {}", readerEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email đến " + readerEmail + ": " + e.getMessage());
        }
    }
}
