package com.example.ecommerce.dto.request;
import com.example.ecommerce.enums.Category;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotBlank(message = "Brand is required")
    private String brand;

    @Positive
    private Double price;

    @PositiveOrZero
    private Integer stock;

    @NotNull
    private Category category;
}
