package com.innovatewithomer.bizora.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Expense {

    private Long id;
    private String category;
    private String description;
    private double amount;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;

    public Expense() {
    }

    public Expense(
            String category,
            String description,
            double amount,
            LocalDate expenseDate
    ) {
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}