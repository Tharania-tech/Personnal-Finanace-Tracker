package com.finance.tracker;

import com.finance.tracker.dto.FinancialSummary;
import com.finance.tracker.dto.TransactionRequest;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.Transaction.TransactionType;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService.
 * Uses Mockito to mock the repository layer.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction incomeTransaction;
    private Transaction expenseTransaction;

    @BeforeEach
    void setUp() {
        incomeTransaction = Transaction.builder()
                .id("txn001")
                .userId("user123")
                .type(TransactionType.INCOME)
                .category("Salary")
                .amount(75000.00)
                .description("Monthly salary")
                .date(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        expenseTransaction = Transaction.builder()
                .id("txn002")
                .userId("user123")
                .type(TransactionType.EXPENSE)
                .category("Food")
                .amount(5000.00)
                .description("Grocery shopping")
                .date(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testAddTransaction_ShouldSaveAndReturnTransaction() {
        TransactionRequest request = new TransactionRequest(
                "user123", TransactionType.INCOME, "Salary",
                75000.00, "Monthly salary", LocalDateTime.now()
        );

        when(transactionRepository.save(any(Transaction.class))).thenReturn(incomeTransaction);

        Transaction result = transactionService.addTransaction(request);

        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        assertEquals(TransactionType.INCOME, result.getType());
        assertEquals(75000.00, result.getAmount());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testGetTransactionById_ShouldReturnTransaction() {
        when(transactionRepository.findById("txn001")).thenReturn(Optional.of(incomeTransaction));

        Transaction result = transactionService.getTransactionById("txn001");

        assertNotNull(result);
        assertEquals("txn001", result.getId());
    }

    @Test
    void testGetFinancialSummary_ShouldCalculateCorrectValues() {
        List<Transaction> transactions = Arrays.asList(incomeTransaction, expenseTransaction);
        when(transactionRepository.findByUserId("user123")).thenReturn(transactions);
        when(transactionRepository.countByUserId("user123")).thenReturn(2L);

        FinancialSummary summary = transactionService.getFinancialSummary("user123");

        assertEquals(75000.00, summary.getTotalIncome());
        assertEquals(5000.00, summary.getTotalExpense());
        assertEquals(70000.00, summary.getBalance());
        assertEquals("SURPLUS", summary.getStatus());
        assertEquals(2L, summary.getTotalTransactions());
    }

    @Test
    void testGetFinancialSummary_ShouldReturnDeficitStatus() {
        Transaction bigExpense = Transaction.builder()
                .userId("user123")
                .type(TransactionType.EXPENSE)
                .amount(100000.00)
                .build();

        List<Transaction> transactions = Arrays.asList(incomeTransaction, bigExpense);
        when(transactionRepository.findByUserId("user123")).thenReturn(transactions);
        when(transactionRepository.countByUserId("user123")).thenReturn(2L);

        FinancialSummary summary = transactionService.getFinancialSummary("user123");

        assertTrue(summary.getBalance() < 0);
        assertEquals("DEFICIT", summary.getStatus());
    }

    @Test
    void testDeleteTransaction_ShouldCallDeleteOnce() {
        when(transactionRepository.findById("txn001")).thenReturn(Optional.of(incomeTransaction));

        transactionService.deleteTransaction("txn001");

        verify(transactionRepository, times(1)).delete(incomeTransaction);
    }
}
