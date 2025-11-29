package com.server.eventi.dto;

public record RestaurantSuggestion(
        String name,
        String cuisine,
        String priceLevel,
        String supports,
        String location) {
}
