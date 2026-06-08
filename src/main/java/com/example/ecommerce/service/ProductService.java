package com.example.ecommerce.service;

import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.enums.ProductStatus;

public interface ProductService {

    // 🔥 CREATE
    ProductResponse addProduct(ProductRequest req, String sellerEmail);

    // 🔥 GET (search + filter + pagination + sorting)
    PageResponse<ProductResponse> getProducts(
            String name,
            Double maxPrice,
            int page,
            int size,
            String sortBy,
            String direction
    );

    // 🔥 UPDATE
    ProductResponse updateProduct(Long id, ProductRequest req, String email);

    // 🔥 DELETE
    void deleteProduct(Long id, String email);
    void updateProductStatus(Long id, ProductStatus status, String email);
}