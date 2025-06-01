package com.reliaquest.api.model;

public record Employee(
        String id,
        String name,
        int salary,
        int age,
        String title,
        String email
) {
}
