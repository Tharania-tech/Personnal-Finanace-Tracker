package com.finance.tracker.controller;

import com.finance.tracker.dto.ApiResponse;
import com.finance.tracker.dto.FinancialSummary;
import com.finance.tracker.dto.TransactionRequest;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TransactionController - REST API Layer
 *
 * Base URL: /api/transactions
 *
 * All endpoints return ApiResponse<T> for consistent response format.
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  METHOD │  URL                                      │  DESCRIPTION   │
 * ├──────────────────────────────────────────────────────────────────────┤
 * │  POST   │  /api/transactions                        │  Add new txn   │
 * │  GET    │  /api/transactions                        │  Get all txns  │
 * │  GET    │  /api/transactions/{id}                   │  Get by ID     │
 * │  GET    │  /api/transactions/user/{userId}          │  Get by user   │
 * │  GET    │  /api/transactions/user/{userId}/income   │  Get income    │
 * │  GET    │  /api/transactions/user/{userId}/expense  │  Get expenses  │
 * │  GET    │  /api/transactions/user/{userId}/summary  │  Get summary   │
 * │  GET    │  /api/transactions/user/{userId}/category │  By category   │
 * │  GET    │  /api/transactions/user/{userId}/range    │  Date range    │
 * │  PUT    │  /api/transactions/{id}                   │  Update txn    │
 * │  DELETE │  /api/transactions/{id}                   │  Delete one    │
 * │  DELETE │  /api/transactions/user/{userId}          │  Delete all    │
 * └──────────────────────────────────────────────────────────────────────┘
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    // ─────────────────────────────────────────────────────────
    //  POST - Add Transaction
    // ─────────────────────────────────────────────────────────

    /**
     * POST /api/transactions
     *
     * Adds a new income or expense transaction.
     * @Valid triggers Bean Validation on TransactionRequest fields.
     *
     * Request Body:
     * {
     *   "userId": "user123",
     *   "type": "INCOME",
     *   "category": "Salary",
     *   "amount": 75000.00,
     *   "description": "Monthly salary - June",
     *   "date": "2024-06-01T10:00:00"
     * }
     *
     * Response (201 Created):
     * {
     *   "success": true,
     *   "message": "Transaction added successfully",
     *   "data": { "_id": "...", "userId": "user123", ... }
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> addTransaction(
            @Valid @RequestBody TransactionRequest request) {

        log.info("POST /api/transactions → type={}, userId={}", request.getType(), request.getUserId());
        Transaction saved = transactionService.addTransaction(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction added successfully", saved));
    }

    // ─────────────────────────────────────────────────────────
    //  GET - All Transactions (Admin)
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/transactions
     *
     * Returns all transactions in the database.
     * Useful for admin/debugging purposes.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        log.info("GET /api/transactions → fetching all");
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(
                ApiResponse.success("Fetched " + transactions.size() + " transactions", transactions));
    }

    // ─────────────────────────────────────────────────────────
    //  GET - Single Transaction by ID
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/transactions/{id}
     *
     * Returns a single transaction by its MongoDB ObjectId.
     *
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Transaction found",
     *   "data": { "_id": "64f1a...", "userId": "user123", ... }
     * }
     *
     * Response (404 Not Found) if ID doesn't exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> getTransactionById(@PathVariable String id) {
        log.info("GET /api/transactions/{}", id);
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction found", transaction));
    }

    // ─────────────────────────────────────────────────────────
    //  GET - By User
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/transactions/user/{userId}
     *
     * Returns all transactions for a specific user (sorted by date, latest first).
     *
     * Example: GET /api/transactions/user/user123
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionsByUser(
            @PathVariable String userId) {

        log.info("GET /api/transactions/user/{}", userId);
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Found " + transactions.size() + " transactions for user: " + userId,
                        transactions));
    }

    // ─────────────────────────────────────────────────────────
    //  GET - Income / Expense
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/transactions/user/{userId}/income
     *
     * Returns only INCOME transactions for a user.
     */
    @GetMapping("/user/{userId}/income")
    public ResponseEntity<ApiResponse<List<Transaction>>> getIncomeTransactions(
            @PathVariable String userId) {

        log.info("GET income transactions for user: {}", userId);
        List<Transaction> incomeList = transactionService.getIncomeTransactions(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Found " + incomeList.size() + " income transactions", incomeList));
    }

    /**
     * GET /api/transactions/user/{userId}/expense
     *
     * Returns only EXPENSE transactions for a user.
     */
    @GetMapping("/user/{userId}/expense")
    public ResponseEntity<ApiResponse<List<Transaction>>> getExpenseTransactions(
            @PathVariable String userId) {

        log.info("GET expense transactions for user: {}", userId);
        List<Transaction> expenseList = transactionService.getExpenseTransactions(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Found " + expenseList.size() + " expense transactions", expenseList));
    }

    // ─────────────────────────────────────────────────────────
    //  GET - Financial Summary
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/transactions/user/{userId}/summary
     *
     * Calculates and returns a financial summary for the user.
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "Financial summary for user: user123",
     *   "data": {
     *     "userId": "user123",
     *     "totalIncome": 150000.00,
     *     "totalExpense": 62500.00,
     *     "balance": 87500.00,
     *     "totalTransactions": 12,
     *     "status": "SURPLUS"
     *   }
     * }
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<ApiResponse<FinancialSummary>> getFinancialSummary(
            @PathVariable String userId) {

        log.info("GET financial summary for user: {}", userId);
        FinancialSummary summary = transactionService.getFinancialSummary(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Financial summary for user: " + userId, summary));
    }

    // ─────────────────────────────────────────────────────────
    //  GET - By Category
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/transactions/user/{userId}/category?name=Food
     *
     * Returns transactions for a user filtered by category.
     * Example: GET /api/transactions/user/user123/category?name=Salary
     */
    @GetMapping("/user/{userId}/category")
    public ResponseEntity<ApiResponse<List<Transaction>>> getByCategory(
            @PathVariable String userId,
            @RequestParam String name) {

        log.info("GET transactions for user: {} in category: {}", userId, name);
        List<Transaction> transactions = transactionService.getTransactionsByCategory(userId, name);
        return ResponseEntity.ok(
                ApiResponse.success("Found " + transactions.size() + " transactions in category: " + name,
                        transactions));
    }

    // ─────────────────────────────────────────────────────────
    //  GET - Date Range Filter
    // ─────────────────────────────────────────────────────────

    /**
     * GET /api/transactions/user/{userId}/range
     *     ?startDate=2024-06-01T00:00:00
     *     &endDate=2024-06-30T23:59:59
     *
     * Returns transactions for a user within a specified date range.
     */
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<ApiResponse<List<Transaction>>> getByDateRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("GET transactions for user: {} from {} to {}", userId, startDate, endDate);
        List<Transaction> transactions =
                transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success("Found " + transactions.size() + " transactions in date range",
                        transactions));
    }

    // ─────────────────────────────────────────────────────────
    //  PUT - Update Transaction
    // ─────────────────────────────────────────────────────────

    /**
     * PUT /api/transactions/{id}
     *
     * Updates an existing transaction.
     *
     * Request Body: same structure as POST
     * Response: updated Transaction document
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request) {

        log.info("PUT /api/transactions/{}", id);
        Transaction updated = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", updated));
    }

    // ─────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────

    /**
     * DELETE /api/transactions/{id}
     *
     * Deletes a single transaction by ID.
     * Returns 404 if not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTransaction(@PathVariable String id) {
        log.info("DELETE /api/transactions/{}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(
                ApiResponse.success("Transaction deleted successfully", "Deleted ID: " + id));
    }

    /**
     * DELETE /api/transactions/user/{userId}
     *
     * Deletes all transactions for a specific user.
     * MongoDB: db.transactions.deleteMany({ userId: "..." })
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteAllForUser(@PathVariable String userId) {
        log.info("DELETE all transactions for user: {}", userId);
        transactionService.deleteAllTransactionsForUser(userId);
        return ResponseEntity.ok(
                ApiResponse.success("All transactions deleted for user: " + userId, userId));
    }
}
