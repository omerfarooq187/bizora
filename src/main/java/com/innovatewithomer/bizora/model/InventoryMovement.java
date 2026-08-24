package com.innovatewithomer.bizora.model;

import java.time.LocalDateTime;

public class InventoryMovement {

    private Long id;

    private Long productId;

    private InventoryMovementType movementType;

    private double quantity;

    private String referenceType;

    private Long referenceId;

    private String note;

    private LocalDateTime createdAt;

    public InventoryMovement() {
    }

    public InventoryMovement(
            Long productId,
            InventoryMovementType movementType,
            double quantity,
            String referenceType,
            Long referenceId,
            String note
    ) {

        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public InventoryMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(
            InventoryMovementType movementType
    ) {
        this.movementType = movementType;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}