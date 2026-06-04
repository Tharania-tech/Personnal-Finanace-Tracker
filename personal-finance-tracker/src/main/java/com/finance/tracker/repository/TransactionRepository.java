package com.finance.tracker.repository;

import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.Transaction.TransactionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TransactionRepository
 *
 * Extends MongoRepository<Transaction, String> where:
 *   - Transaction = the document entity
 *   - String      = the type of the @Id field (MongoDB ObjectId as String)
 *
 * Spring Data MongoDB auto-implements these methods at runtime.
 * No SQL queries needed — method names are converted to MongoDB queries.
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    /**
     * Find all transactions belonging to a specific user.
     * MongoDB Query: db.transactions.find({ userId: "user123" })
     */
    List<Transaction> findByUserId(String userId);

    /**
     * Find all transactions for a user filtered by type (INCOME or EXPENSE).
     * MongoDB Query: db.transactions.find({ userId: "user123", type: "INCOME" })
     */
    List<Transaction> findByUserIdAndType(String userId, TransactionType type);

    /**
     * Find all transactions for a user within a date range.
     * MongoDB Query: db.transactions.find({ userId: "...", date: { $gte: start, $lte: end } })
     */
    List<Transaction> findByUserIdAndDateBetween(String userId,
                                                  LocalDateTime startDate,
                                                  LocalDateTime endDate);

    /**
     * Find all transactions for a user in a specific category.
     * MongoDB Query: db.transactions.find({ userId: "...", category: "Food" })
     */
    List<Transaction> findByUserIdAndCategory(String userId, String category);

    /**
     * Find all transactions for a user sorted by date descending (latest first).
     * MongoDB Query: db.transactions.find({ userId: "..." }).sort({ date: -1 })
     */
    List<Transaction> findByUserIdOrderByDateDesc(String userId);

    /**
     * Custom query using @Query annotation with MongoDB JSON syntax.
     * Finds all transactions above a given amount for a user.
     *
     * ?0 = first parameter (userId), ?1 = second parameter (amount)
     */
    @Query("{ 'userId': ?0, 'amount': { $gte: ?1 } }")
    List<Transaction> findByUserIdAndAmountGreaterThanEqual(String userId, Double amount);

    /**
     * Count total number of transactions for a user.
     * MongoDB Query: db.transactions.countDocuments({ userId: "..." })
     */
    long countByUserId(String userId);

    /**
     * Delete all transactions for a specific user.
     * MongoDB Query: db.transactions.deleteMany({ userId: "..." })
     */
    void deleteByUserId(String userId);
}
