package com.innovatewithomer.bizora.repository;

import com.innovatewithomer.bizora.infrastructure.DatabaseManager;
import com.innovatewithomer.bizora.model.Expense;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseRepositoryTest {

    private ExpenseRepository repository;
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

        repository =
                new ExpenseRepository();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }

        DatabaseManager.resetJdbcUrl();
    }

    @Test
    void shouldSaveExpense() {

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

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Expense saved =
                    repository.save(
                            connection,
                            expense
                    );

            assertNotNull(
                    saved.getId()
            );

            assertEquals(
                    "Electricity",
                    saved.getCategory()
            );

            assertEquals(
                    "Monthly electricity bill",
                    saved.getDescription()
            );

            assertEquals(
                    15000,
                    saved.getAmount()
            );

            assertEquals(
                    LocalDate.of(
                            2026,
                            8,
                            20
                    ),
                    saved.getExpenseDate()
            );

            assertNotNull(
                    saved.getCreatedAt()
            );

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void shouldFindExpenseById() {

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

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            Expense saved =
                    repository.save(
                            connection,
                            expense
                    );

            Expense found =
                    repository.findById(
                            connection,
                            saved.getId()
                    );

            assertNotNull(found);

            assertEquals(
                    saved.getId(),
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

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void shouldReturnNullForNonExistingExpense() {

        Expense expense =
                repository.findById(
                        999999L
                );

        assertNull(expense);
    }

    @Test
    void shouldFindAllExpenses() {

        Expense first =
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

        Expense second =
                new Expense(
                        "Electricity",
                        "Electricity bill",
                        15000,
                        LocalDate.of(
                                2026,
                                8,
                                5
                        )
                );

        Expense third =
                new Expense(
                        "Internet",
                        "Internet bill",
                        5000,
                        LocalDate.of(
                                2026,
                                8,
                                10
                        )
                );

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            repository.save(
                    connection,
                    first
            );

            repository.save(
                    connection,
                    second
            );

            repository.save(
                    connection,
                    third
            );

        } catch (Exception e) {
            fail(e);
        }

        List<Expense> expenses =
                repository.findAll();

        assertEquals(
                3,
                expenses.size()
        );

        /*
         * Repository orders by id DESC.
         */
        assertEquals(
                third.getId(),
                expenses.get(0).getId()
        );

        assertEquals(
                second.getId(),
                expenses.get(1).getId()
        );

        assertEquals(
                first.getId(),
                expenses.get(2).getId()
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

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            repository.save(
                    connection,
                    expense
            );

            assertNotNull(
                    repository.findById(
                            connection,
                            expense.getId()
                    )
            );

            repository.delete(
                    connection,
                    expense.getId()
            );

            assertNull(
                    repository.findById(
                            connection,
                            expense.getId()
                    )
            );

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void shouldMapExpenseFields() {

        LocalDate expenseDate =
                LocalDate.of(
                        2026,
                        8,
                        15
                );

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        8,
                        15,
                        14,
                        30,
                        45
                );

        Expense expense =
                new Expense(
                        "Maintenance",
                        "Air conditioner repair",
                        8500,
                        expenseDate
                );

        expense.setCreatedAt(
                createdAt
        );

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            repository.save(
                    connection,
                    expense
            );

            Expense found =
                    repository.findById(
                            connection,
                            expense.getId()
                    );

            assertNotNull(found);

            assertEquals(
                    expense.getId(),
                    found.getId()
            );

            assertEquals(
                    "Maintenance",
                    found.getCategory()
            );

            assertEquals(
                    "Air conditioner repair",
                    found.getDescription()
            );

            assertEquals(
                    8500,
                    found.getAmount()
            );

            assertEquals(
                    expenseDate,
                    found.getExpenseDate()
            );

            assertEquals(
                    createdAt,
                    found.getCreatedAt()
            );

        } catch (Exception e) {
            fail(e);
        }
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

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            repository.save(
                    connection,
                    expense
            );

            Expense found =
                    repository.findById(
                            connection,
                            expense.getId()
                    );

            assertNotNull(found);

            assertNull(
                    found.getDescription()
            );

        } catch (Exception e) {
            fail(e);
        }
    }
}