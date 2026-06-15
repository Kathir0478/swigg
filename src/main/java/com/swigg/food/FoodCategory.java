package com.swigg.food;

public enum FoodCategory {
    VEG("Vegetarian"),
    NON_VEG("Non-Vegetarian"),
    STARTER("Starter"),
    BEVERAGE("Beverage"),
    DESSERT("Dessert"),
    SALAD("Salad"),
    SOUP("Soup"),
    MAIN_COURSE("Main Course"),
    SIDE_DISH("Side Dish"),
    APPETIZER("Appetizer");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
