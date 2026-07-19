package com.library.management.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RenewRequestDTO {
    @NotNull(message = "Vui Lòng chọn ngày trả mới")
    @Future(message = "Ngày trả mới phải ở tương lai")
    private LocalDateTime requestDueDate;
    private String readerNote;
}
