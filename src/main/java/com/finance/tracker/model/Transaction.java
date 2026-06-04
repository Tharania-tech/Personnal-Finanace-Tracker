package com.finance.tracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Transaction - MongoDB Document Entity
 *
 * Stored in the "transactions" collection.
 * Each document represents a single financial transaction (income or expense).
 *
 * MongoDB Document Example:
 * {
 *   "_id": "64f1a2b3c4d5e6f7a8b9c0d1",
 *   "userId": "user123",
 *   "type": "INCOME",
 *   "category": "Salary",
 *   "amount": 75000.00,
 *   "description": "Monthly salary - June",
 *   "date": "2024-06-01T10:00:00",
 *   "createdAt": "2024-06-01T10:05:23"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")  // Maps to MongoDB collection "transactions"
public class Transaction {

    @Id  // Maps to MongoDB's _id field (auto-generated ObjectId)
    private String id;

    @Indexed  // Creates an index on userId for faster queries
    @NotBlank(message = "User ID must not be blank")
    @Field("userId")
    private String userId;

    @NotNull(message = "Transaction type is required")
    @Field("type")
    private TransactionType type;  // INCOME or EXPENSE

    @NotBlank(message = "Category must not be blank")
    @Field("category")
    private String category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Field("amount")
    private Double amount;

    @Field("description")
    private String description;

    @Field("date")
    private LocalDateTime date;

    @Field("createdAt")
    private LocalDateTime createdAt;

    /**
     * Enum representing the type of transaction.
     * Stored as a String in MongoDB for readability.
     */
    public enum TransactionType {
        INCOME,
        EXPENSE
    }
}
