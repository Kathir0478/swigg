package com.swigg.cart;

public enum CartStatus {
    ACTIVE("Active - Items in cart"),
    ABANDONED("Abandoned - Cart not updated"),
    ORDERED("Ordered - Converted to order");

    private final String description;

    CartStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
