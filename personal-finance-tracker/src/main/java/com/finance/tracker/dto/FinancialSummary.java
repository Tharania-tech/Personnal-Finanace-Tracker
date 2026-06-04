package com.finance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FinancialSummary DTO
 *
 * Returned as the response for the /summary endpoint.
 * Contains aggregated financial data for a specific user.
 *
 * Example JSON Response:
 * {
 *   "userId": "user123",
 *   "totalIncome": 150000.00,
 *   "totalExpense": 62500.00,
 *   "balance": 87500.00,
 *   "totalTransactions": 12,
 *   "status": "SURPLUS"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummary {

    private String userId;

    /** Sum of all INCOME transactions */
    private Double totalIncome;

    /** Sum of all EXPENSE transactions */
    private Double totalExpense;

    /** Net balance: totalIncome - totalExpense */
    private Double balance;

    /** Total number of transactions (income + expense) */
    private Long totalTransactions;

    /**
     * Financial status based on balance:
     *   SURPLUS  → balance > 0
     *   DEFICIT  → balance < 0
     *   BALANCED → balance == 0
     */
    private String status;
}
