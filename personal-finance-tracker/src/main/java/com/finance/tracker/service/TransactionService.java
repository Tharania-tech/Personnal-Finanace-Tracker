package com.finance.tracker.service;

import com.finance.tracker.dto.FinancialSummary;
import com.finance.tracker.dto.TransactionRequest;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.Transaction.TransactionType;
import com.finance.tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TransactionService - Business Logic Layer
 *
 * This service contains all core business logic:
 *   1. Create new transactions (income or expense)
 *   2. Retrieve transactions (by user, by type, by category)
 *   3. Calculate financial summary (total income, expense, balance)
 *   4. Update and delete transactions
 *
 * Flow:  Controller → Service → Repository → MongoDB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    // ─────────────────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────────────────

    /**
     * Adds a new transaction (INCOME or EXPENSE) to MongoDB.
     *
     * Data saved to MongoDB:
     * {
     *   "_id"         : auto-generated ObjectId,
     *   "userId"      : from request,
     *   "type"        : "INCOME" or "EXPENSE",
     *   "category"    : from request,
     *   "amount"      : from request,
     *   "description" : from request,
     *   "date"        : from request (or current time if null),
     *   "createdAt"   : server timestamp at save time
     * }
     */
    public Transaction addTransaction(TransactionRequest request) {
        log.info("Adding {} transaction for user: {}", request.getType(), request.getUserId());

        Transaction transaction = Transaction.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate() != null ? request.getDate() : LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction saved with ID: {}", saved.getId());
        return saved;
    }

    // ─────────────────────────────────────────────────────────
    //  READ - Single Transaction
    // ─────────────────────────────────────────────────────────

    /**
     * Retrieves a single transaction by its MongoDB ObjectId.
     *
     * Throws ResourceNotFoundException (→ 404) if not found.
     */
    public Transaction getTransactionById(String id) {
        log.info("Fetching transaction with ID: {}", id);
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }

    // ─────────────────────────────────────────────────────────
    //  READ - By User
    // ─────────────────────────────────────────────────────────

    /**
     * Retrieves all transactions for a user, sorted by date (latest first).
     * MongoDB: db.transactions.find({ userId: "..." }).sort({ date: -1 })
     */
    public List<Transaction> getTransactionsByUser(String userId) {
        log.info("Fetching all transactions for user: {}", userId);
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    /**
     * Retrieves only INCOME transactions for a user.
     * MongoDB: db.transactions.find({ userId: "...", type: "INCOME" })
     */
    public List<Transaction> getIncomeTransactions(String userId) {
        log.info("Fetching INCOME transactions for user: {}", userId);
        return transactionRepository.findByUserIdAndType(userId, TransactionType.INCOME);
    }

    /**
     * Retrieves only EXPENSE transactions for a user.
     * MongoDB: db.transactions.find({ userId: "...", type: "EXPENSE" })
     */
    public List<Transaction> getExpenseTransactions(String userId) {
        log.info("Fetching EXPENSE transactions for user: {}", userId);
        return transactionRepository.findByUserIdAndType(userId, TransactionType.EXPENSE);
    }

    /**
     * Retrieves transactions for a user filtered by category.
     * MongoDB: db.transactions.find({ userId: "...", category: "Food" })
     */
    public List<Transaction> getTransactionsByCategory(String userId, String category) {
        log.info("Fetching '{}' transactions for user: {}", category, userId);
        return transactionRepository.findByUserIdAndCategory(userId, category);
    }

    /**
     * Retrieves all transactions for a user within a date range.
     */
    public List<Transaction> getTransactionsByDateRange(String userId,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate) {
        log.info("Fetching transactions for user {} between {} and {}", userId, startDate, endDate);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
        return transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    // ─────────────────────────────────────────────────────────
    //  READ - All (Admin)
    // ─────────────────────────────────────────────────────────

    /**
     * Retrieves all transactions in the database (admin use).
     * MongoDB: db.transactions.find({})
     */
    public List<Transaction> getAllTransactions() {
        log.info("Fetching ALL transactions");
        return transactionRepository.findAll();
    }

    // ─────────────────────────────────────────────────────────
    //  FINANCIAL SUMMARY CALCULATION
    // ─────────────────────────────────────────────────────────

    /**
     * Calculates a financial summary for a given user.
     *
     * Core business logic:
     *   totalIncome  = SUM of all INCOME transaction amounts
     *   totalExpense = SUM of all EXPENSE transaction amounts
     *   balance      = totalIncome - totalExpense
     *   status       = "SURPLUS" | "DEFICIT" | "BALANCED"
     *
     * Uses Java Streams for aggregation (no MongoDB aggregation pipeline needed).
     *
     * Returns FinancialSummary:
     * {
     *   "userId"            : "user123",
     *   "totalIncome"       : 150000.00,
     *   "totalExpense"      : 62500.00,
     *   "balance"           : 87500.00,
     *   "totalTransactions" : 12,
     *   "status"            : "SURPLUS"
     * }
     */
    public FinancialSummary getFinancialSummary(String userId) {
        log.info("Calculating financial summary for user: {}", userId);

        List<Transaction> allTransactions = transactionRepository.findByUserId(userId);

        // Aggregate income: filter INCOME, sum amounts
        double totalIncome = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Aggregate expenses: filter EXPENSE, sum amounts
        double totalExpense = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Net balance
        double balance = totalIncome - totalExpense;

        // Determine financial status
        String status;
        if (balance > 0) {
            status = "SURPLUS";
        } else if (balance < 0) {
            status = "DEFICIT";
        } else {
            status = "BALANCED";
        }

        // Count total transactions
        long totalTransactions = transactionRepository.countByUserId(userId);

        log.info("Summary → Income: {}, Expense: {}, Balance: {}, Status: {}",
                totalIncome, totalExpense, balance, status);

        return FinancialSummary.builder()
                .userId(userId)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .totalTransactions(totalTransactions)
                .status(status)
                .build();
    }

    // ─────────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────────

    /**
     * Updates an existing transaction by ID.
     * Fetches the existing document, applies changes, and saves back.
     * MongoDB: replaces the entire document (Spring Data MongoDB save behaviour).
     */
    public Transaction updateTransaction(String id, TransactionRequest request) {
        log.info("Updating transaction with ID: {}", id);

        Transaction existing = getTransactionById(id);

        existing.setType(request.getType());
        existing.setCategory(request.getCategory());
        existing.setAmount(request.getAmount());
        existing.setDescription(request.getDescription());

        if (request.getDate() != null) {
            existing.setDate(request.getDate());
        }

        Transaction updated = transactionRepository.save(existing);
        log.info("Transaction updated: {}", updated.getId());
        return updated;
    }

    // ─────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────

    /**
     * Deletes a single transaction by MongoDB ObjectId.
     * Throws ResourceNotFoundException if the transaction doesn't exist.
     */
    public void deleteTransaction(String id) {
        log.info("Deleting transaction with ID: {}", id);
        Transaction transaction = getTransactionById(id);
        transactionRepository.delete(transaction);
        log.info("Transaction deleted: {}", id);
    }

    /**
     * Deletes all transactions for a specific user.
     * MongoDB: db.transactions.deleteMany({ userId: "..." })
     */
    public void deleteAllTransactionsForUser(String userId) {
        log.info("Deleting all transactions for user: {}", userId);
        transactionRepository.deleteByUserId(userId);
    }
}
