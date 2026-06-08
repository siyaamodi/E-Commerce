package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.AddToCartRequest;
import com.example.ecommerce.dto.response.CartItemResponse;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.UnauthorizedException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    public void addToCart(String email, AddToCartRequest req) {

        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem item = cartRepo
                .findByUserEmailAndProduct_Id(email, req.getProductId())
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + req.getQuantity());
        } else {
            item = new CartItem();
            item.setUserEmail(email);
            item.setProduct(product);
            item.setQuantity(req.getQuantity());
        }

        cartRepo.save(item);
    }

    public List<CartItemResponse> getCart(String email) {
        return cartRepo.findByUserEmail(email)
                .stream()
                .map(c -> new CartItemResponse(
                        c.getId(),
                        c.getProduct().getId(),
                        c.getProduct().getName(),
                        c.getQuantity(),
                        c.getProduct().getPrice(),
                        c.getQuantity() * c.getProduct().getPrice()
                ))
                .toList();
    }

    public void removeItem(Long id, String email) {

        CartItem item = cartRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getUserEmail().equals(email)) {
            throw new UnauthorizedException("Unauthorized");
        }

        cartRepo.delete(item);
    }
@Transactional
    public void clearCart(String email) {
    cartRepo.deleteByUserEmail(email);
    }
}