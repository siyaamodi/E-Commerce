package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.ProductStatus;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.enums.SellerStatus;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.UnauthorizedException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.util.PaginationUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "price", "name");

    private ProductResponse map(Product p) {
        String sellerName = p.getSeller() != null ? p.getSeller().getName() : "Unknown";
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getCategory(),
                p.getImageUrl(),
                p.getBrand(),
                p.getStatus().name(),
                sellerName,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    // 🔥 CREATE
    @Override
    @Transactional
    public ProductResponse addProduct(ProductRequest req, String sellerEmail) {
        User seller = userRepo.findByEmail(sellerEmail)
                .orElseThrow(() -> new BadRequestException("Seller not found"));
        if (seller.getRole() != Role.SELLER && seller.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("User is not a seller");
        }
        if (seller.getRole() == Role.SELLER && seller.getSellerStatus() != SellerStatus.APPROVED) {
            throw new UnauthorizedException("Your seller account is not approved by admin");
        }

        if (req.getPrice() <= 0) {
            throw new BadRequestException("Price must be positive");
        }
        if (req.getStock() < 0) {
            throw new BadRequestException("Stock cannot be negative");
        }

        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.updateStatusBasedOnStock();
        product.setCategory(req.getCategory());
        product.setImageUrl(req.getImageUrl());
        product.setBrand(req.getBrand());
        product.setStatus(ProductStatus.ACTIVE);
        product.setSeller(seller);               // ✅ relation, not email string
        product.setCreatedAt(LocalDateTime.now());

        productRepo.save(product);
        return map(product);
    }

    // 🔥 GET (PUBLIC)
    @Override
    public PageResponse<ProductResponse> getProducts(
            String name,
            Double maxPrice,
            int page,
            int size,
            String sortBy,
            String direction) {

        PaginationUtils.validate(page, size, sortBy, ALLOWED_SORT_FIELDS, direction);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepo.findAll(spec, pageable);

        List<ProductResponse> list = productPage.getContent()
                .stream()
                .map(this::map)
                .toList();

        return new PageResponse<>(
                list,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    // 🔥 UPDATE
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest req, String email) {
        User seller = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (seller.getRole() == Role.SELLER && seller.getSellerStatus() != SellerStatus.APPROVED) {
            throw new UnauthorizedException("Your seller account is not active");
        }

        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // ✅ ownership check using seller relation
        if (!p.getSeller().getEmail().equals(email)) {
            throw new UnauthorizedException("Not your product");
        }

        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        p.updateStatusBasedOnStock();
        p.setCategory(req.getCategory());
        p.setImageUrl(req.getImageUrl());
        p.setBrand(req.getBrand());
        // status update is not allowed via this DTO (optional, you could add later)

        productRepo.save(p);
        return map(p);
    }

    // 🔥 DELETE
    @Override
    @Transactional
    public void deleteProduct(Long id, String email) {
        User seller = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (seller.getRole() == Role.SELLER && seller.getSellerStatus() != SellerStatus.APPROVED) {
            throw new UnauthorizedException("Your seller account is not active");
        }

        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!p.getSeller().getEmail().equals(email)) {
            throw new UnauthorizedException("Not your product");
        }

        productRepo.delete(p);
    }
    @Override
    @Transactional
    public void updateProductStatus(Long id, ProductStatus newStatus, String email) {
        User seller = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (seller.getRole() == Role.SELLER && seller.getSellerStatus() != SellerStatus.APPROVED) {
            throw new UnauthorizedException("Your seller account is not active");
        }
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!p.getSeller().getEmail().equals(email)) {
            throw new UnauthorizedException("Not your product");
        }
        p.setStatus(newStatus);
        productRepo.save(p);
    }
}