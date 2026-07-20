package com.library.management.services.impl;

import com.library.management.entities.Book;
import com.library.management.enums.BorrowItemStatus;
import com.library.management.enums.BorrowStatus;
import com.library.management.repositories.BorrowDetailRepository;
import com.library.management.repositories.BorrowRequestRepository;
import com.library.management.repositories.FineRepository;
import com.library.management.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final BorrowDetailRepository borrowDetailRepository;
    private final FineRepository fineRepository;
    private final com.library.management.repositories.BookRepository bookRepository;

    @Override
    public Map<String, Object> getSimpleStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalRequests = borrowRequestRepository.count();
        long pendingRequests = borrowRequestRepository.findAllByStatusOrderByRequestDateDesc(BorrowStatus.PENDING).size();
        long activeBorrows = borrowDetailRepository.countByStatus(BorrowItemStatus.BORROWING);
        long overdueBorrows = borrowDetailRepository.countByStatusAndDueDateBefore(BorrowItemStatus.BORROWING, LocalDateTime.now());

        double totalFines = fineRepository.findAll().stream()
                .mapToDouble(f -> f.getAmount() != null ? f.getAmount() : 0.0)
                .sum();

        stats.put("totalRequests", totalRequests);
        stats.put("pendingRequests", pendingRequests);
        stats.put("activeBorrows", activeBorrows);
        stats.put("overdueBorrows", overdueBorrows);
        stats.put("totalFines", totalFines);

        List<Object[]> topBookIds = borrowDetailRepository.findTopBookIds(PageRequest.of(0, 5));
        List<Map<String, Object>> topBooks = new ArrayList<>();
        for (Object[] row : topBookIds) {
            Long bookId = (Long) row[0];
            Long count = (Long) row[1];
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book != null) {
                Map<String, Object> m = new HashMap<>();
                m.put("title", book.getTitle());
                m.put("author", book.getAuthor() != null ? book.getAuthor().getName() : "Không rõ");
                m.put("category", book.getCategory() != null ? book.getCategory().getName() : "Không rõ");
                m.put("count", count);
                topBooks.add(m);
            }
        }
        stats.put("topBooks", topBooks);

        return stats;
    }
}
