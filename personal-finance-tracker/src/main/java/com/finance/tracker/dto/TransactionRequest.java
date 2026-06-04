package com.finance.tracker.dto;

import com.finance.tracker.model.Transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * TransactionRequest DTO
 *
 * Data Transfer Object used to receive transaction data from the client (HTTP request body).
 * Validated using Bean Validation annotations before being processed by the service.
 *
 * Example JSON Request:
 * {
 *   "userId": "user123",
 *   "type": "INCOME",
 *   "category": "Salary",
 *   "amount": 75000.00,
 *   "description": "Monthly salary - June",
 *   "date": "2024-06-01T10:00:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Transaction type is required (INCOME or EXPENSE)")
    private TransactionType type;

    @NotBlank(message = "Category is required (e.g., Salary, Food, Rent)")
    private String category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be a positive number")
    private Double amount;

    private String description;

    private LocalDateTime date;
}
