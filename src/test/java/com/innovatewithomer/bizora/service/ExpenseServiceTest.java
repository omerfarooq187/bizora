package com.innovatewithomer.bizora.service;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Expense;
import com.innovatewithomer.bizora.repository.ExpenseRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseServiceTest {

    private ExpenseService expenseService;

    private ExpenseRepository expenseRepository;

    private Connection keepAliveConnection;

    @BeforeEach
    void setUp() throws Exception {

        DatabaseManager.setJdbcUrl(
                "jdbc:sqlite:file:bizora_test_"
                        + System.nanoTime()
                        + "?mode=memory&cache=shared"
        );

        keepAliveConnection =
                DatabaseManager.getConnection();

        try (Statement statement =
                     keepAliveConnection.createStatement()) {

            statement.execute("""
                CREATE TABLE expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    description TEXT,
                    amount REAL NOT NULL,
                    expense_date TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """);
        }

        expenseRepository =
                new ExpenseRepository();

        expenseService =
                new ExpenseService(
                        expenseRepository
                );
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldCreateExpense() {

        Expense expense =
                new Expense(
                        "Electricity",
                        "Monthly electricity bill",
                        15000,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        Expense created =
                expenseService.createExpense(
                        expense
                );

        assertNotNull(
                created.getId()
        );

        assertEquals(
                "Electricity",
                created.getCategory()
        );

        assertEquals(
                15000,
                created.getAmount()
        );

        Expense saved =
                expenseRepository.findById(
                        created.getId()
                );

        assertNotNull(saved);

        assertEquals(
                created.getId(),
                saved.getId()
        );
    }

    @Test
    void shouldGetExpense() {

        Expense expense =
                new Expense(
                        "Rent",
                        "Shop rent",
                        50000,
                        LocalDate.of(
                                2026,
                                8,
                                1
                        )
                );

        expenseService.createExpense(
                expense
        );

        Expense found =
                expenseService.getExpense(
                        expense.getId()
                );

        assertNotNull(found);

        assertEquals(
                expense.getId(),
                found.getId()
        );

        assertEquals(
                "Rent",
                found.getCategory()
        );

        assertEquals(
                "Shop rent",
                found.getDescription()
        );

        assertEquals(
                50000,
                found.getAmount()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        8,
                        1
                ),
                found.getExpenseDate()
        );
    }

    @Test
    void shouldGetAllExpenses() {

        expenseService.createExpense(
                new Expense(
                        "Rent",
                        "Shop rent",
                        50000,
                        LocalDate.of(
                                2026,
                                8,
                                1
                        )
                )
        );

        expenseService.createExpense(
                new Expense(
                        "Electricity",
                        "Electricity bill",
                        15000,
                        LocalDate.of(
                                2026,
                                8,
                                5
                        )
                )
        );

        expenseService.createExpense(
                new Expense(
                        "Internet",
                        "Internet bill",
                        5000,
                        LocalDate.of(
                                2026,
                                8,
                                10
                        )
                )
        );

        List<Expense> expenses =
                expenseService.getAllExpenses();

        assertEquals(
                3,
                expenses.size()
        );
    }

    @Test
    void shouldDeleteExpense() {

        Expense expense =
                new Expense(
                        "Transport",
                        "Delivery fuel",
                        3000,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        expenseService.createExpense(
                expense
        );

        Long expenseId =
                expense.getId();

        assertNotNull(
                expenseRepository.findById(
                        expenseId
                )
        );

        expenseService.deleteExpense(
                expenseId
        );

        assertNull(
                expenseRepository.findById(
                        expenseId
                )
        );
    }

    @Test
    void shouldRejectNullExpense() {

        assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.createExpense(
                        null
                )
        );
    }

    @Test
    void shouldRejectBlankCategory() {

        Expense expense =
                new Expense(
                        "   ",
                        "Some expense",
                        1000,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> expenseService.createExpense(
                                expense
                        )
                );

        assertEquals(
                "Expense category is required.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullCategory() {

        Expense expense =
                new Expense(
                        null,
                        "Some expense",
                        1000,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.createExpense(
                        expense
                )
        );
    }

    @Test
    void shouldRejectZeroAmount() {

        Expense expense =
                new Expense(
                        "Other",
                        "Invalid expense",
                        0,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.createExpense(
                        expense
                ));
    }

    @Test
    void shouldRejectNegativeAmount() {

        Expense expense =
                new Expense(
                        "Other",
                        "Invalid expense",
                        -1000,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.createExpense(
                        expense
                ));
    }

    @Test
    void shouldRejectNullExpenseDate() {

        Expense expense =
                new Expense(
                        "Other",
                        "Missing date",
                        1000,
                        null
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.createExpense(
                        expense
                ));
    }

    @Test
    void shouldRejectNullCreatedAt() {

        Expense expense =
                new Expense(
                        "Other",
                        "Missing created timestamp",
                        1000,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        expense.setCreatedAt(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> expenseService.createExpense(
                                expense
                        )
                );

        assertEquals(
                "Created date is required.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullExpenseId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.getExpense(null)
        );
    }

    @Test
    void shouldRejectNonExistingExpense() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> expenseService.getExpense(
                                999999L
                        )
                );

        assertEquals(
                "Expense not found: 999999",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectDeleteWithNullId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> expenseService.deleteExpense(
                        null
                )
        );
    }

    @Test
    void shouldRejectDeleteOfNonExistingExpense() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> expenseService.deleteExpense(
                                999999L
                        )
                );

        assertEquals(
                "Expense not found: 999999",
                exception.getMessage()
        );
    }

    @Test
    void shouldAllowNullDescription() {

        Expense expense =
                new Expense(
                        "Other",
                        null,
                        2000,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        Expense created =
                expenseService.createExpense(
                        expense
                );

        Expense found =
                expenseService.getExpense(
                        created.getId()
                );

        assertNotNull(found);

        assertNull(
                found.getDescription()
        );
    }

    @Test
    void shouldPreserveCreatedAt() {

        Expense expense =
                new Expense(
                        "Maintenance",
                        "Repair",
                        8500,
                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        8,
                        20,
                        15,
                        30,
                        45
                );

        expense.setCreatedAt(
                createdAt
        );

        expenseService.createExpense(
                expense
        );

        Expense found =
                expenseService.getExpense(
                        expense.getId()
                );

        assertEquals(
                createdAt,
                found.getCreatedAt()
        );
    }
}