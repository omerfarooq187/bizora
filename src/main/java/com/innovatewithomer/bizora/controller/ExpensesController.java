package com.innovatewithomer.bizora.controller;

import com.innovatewithomer.bizora.config.AppContext;
import com.innovatewithomer.bizora.model.Expense;
import com.innovatewithomer.bizora.service.ExpenseService;
import com.innovatewithomer.bizora.util.CurrencyFormatter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ExpensesController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final ExpenseService expenseService = AppContext.expenseService();

    private final ObservableList<Expense> expenseList = FXCollections.observableArrayList();
    private FilteredList<Expense> filteredExpenses;

    @FXML private TableView<Expense>  expensesTable;
    @FXML private TableColumn<Expense, Long>   idColumn;
    @FXML private TableColumn<Expense, String> categoryColumn;
    @FXML private TableColumn<Expense, String> descriptionColumn;
    @FXML private TableColumn<Expense, Double> amountColumn;
    @FXML private TableColumn<Expense, String> dateColumn;
    @FXML private TableColumn<Expense, Void>   actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label totalExpensesLabel;
    @FXML private Label expenseCountLabel;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        configureTable();
        configureSearch();
        loadExpenses();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : CurrencyFormatter.format(v));
            }
        });

        dateColumn.setCellValueFactory(cellData -> {
            Expense e = cellData.getValue();
            if (e == null || e.getExpenseDate() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(e.getExpenseDate().format(DATE_FMT));
        });

        configureActionsColumn();
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(deleteBtn);
            {
                deleteBtn.getStyleClass().add("danger-button");
                deleteBtn.setOnAction(e -> {
                    Expense exp = getTableRow() != null ? getTableRow().getItem() : null;
                    if (exp == null && getIndex() >= 0 && getTableView() != null && getIndex() < getTableView().getItems().size()) {
                        exp = getTableView().getItems().get(getIndex());
                    }
                    if (exp != null) {
                        handleDelete(exp);
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void configureSearch() {
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        categoryFilter.valueProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void loadExpenses() {
        List<Expense> all = expenseService.getAllExpenses();
        expenseList.setAll(all != null ? all : List.of());

        if (filteredExpenses == null) {
            filteredExpenses = new FilteredList<>(expenseList, e -> true);
            expensesTable.setItems(filteredExpenses);
        }

        // Populate category filter
        ObservableList<String> categories = FXCollections.observableArrayList("All Categories");
        if (all != null) {
            all.stream()
               .map(Expense::getCategory)
               .filter(cat -> cat != null && !cat.isBlank())
               .distinct()
               .sorted()
               .forEach(categories::add);
        }
        String selected = categoryFilter.getValue();
        categoryFilter.setItems(categories);
        categoryFilter.setValue(selected != null && categories.contains(selected) ? selected : "All Categories");

        applyFilter();
        updateSummary();
    }

    private void applyFilter() {
        if (filteredExpenses == null) {
            return;
        }
        String text = searchField.getText();
        String q   = text != null ? text.trim().toLowerCase() : "";
        String cat = categoryFilter.getValue();
        filteredExpenses.setPredicate(e -> {
            if (e == null) return false;
            boolean matchSearch = q.isEmpty()
                || (e.getCategory()    != null && e.getCategory().toLowerCase().contains(q))
                || (e.getDescription() != null && e.getDescription().toLowerCase().contains(q));

            boolean matchCat = cat == null || cat.equals("All Categories")
                || (e.getCategory() != null && cat.equalsIgnoreCase(e.getCategory()));

            return matchSearch && matchCat;
        });
        updateSummary();
    }

    private void updateSummary() {
        if (filteredExpenses == null) {
            return;
        }
        double total = filteredExpenses.stream().filter(java.util.Objects::nonNull).mapToDouble(Expense::getAmount).sum();
        totalExpensesLabel.setText(CurrencyFormatter.format(total));
        expenseCountLabel.setText(String.valueOf(filteredExpenses.size()));
        statusLabel.setText(filteredExpenses.size() + " expense(s) shown.");
    }

    @FXML
    private void handleAddExpense() {
        openDialog();
    }

    private void openDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/innovatewithomer/bizora/fxml/add-expense-dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Expense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadExpenses();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open expense dialog.", e);
        }
    }

    private void handleDelete(Expense expense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Expense");
        alert.setHeaderText("Delete this expense?");
        alert.setContentText("Category: " + expense.getCategory()
                + "\nAmount: " + CurrencyFormatter.format(expense.getAmount()));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            expenseService.deleteExpense(expense.getId());
            loadExpenses();
        }
    }

    @FXML
    private void handleSearch() {
        applyFilter();
    }
}
