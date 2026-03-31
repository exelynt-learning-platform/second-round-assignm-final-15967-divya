package com.example.demo.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @Size(max = 500, message = "Description too long")
    private String description;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 1, message = "Minimum quantity is 1")
    @Max(value = 1000, message = "Maximum quantity is 1000")
    private Integer stockQuantity;

    @Pattern(
        regexp = "^(https?://.*)?$",
        message = "Invalid image URL"
    )
    private String imageUrl;
}