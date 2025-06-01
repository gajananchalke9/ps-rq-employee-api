package com.reliaquest.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateEmployeeRequest(
        @NotBlank
        String name,

        @Min(1)
        int salary,

        @Min(16)
        int age,

        @NotBlank
        String title
) {
}
