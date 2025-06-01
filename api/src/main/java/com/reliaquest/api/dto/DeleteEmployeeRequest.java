package com.reliaquest.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DeleteEmployeeRequest(
        @NotBlank
        String name
) {
}
