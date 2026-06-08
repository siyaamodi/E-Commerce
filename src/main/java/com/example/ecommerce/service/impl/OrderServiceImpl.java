package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.response.OrderItemResponse;
import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.enums.*;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.UnauthorizedException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.AddressService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.util.PaginationUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final EntityManager entityManager;
    private final UserRepository userRepo;
    private final AddressService addressService;   // 🔥 ADDED

    // ===================== PLACE ORDER (WITH RETRY) =====================
    @Override
    @Transactional
    public OrderResponse placeOrder(String email, PaymentMethod paymentMethod){
        return placeOrderWithRetry(email,paymentMethod,3 );
    }

    private OrderResponse placeOrderWithRetry(String email, PaymentMethod paymentMethod, int retriesLeft) {
        try {
            List<CartItem> cartItems = cartRepo.findByUserEmail(email);
            if (cartItems.isEmpty()) {
                throw new BadRequestException("Cart is empty");
            }

            // 🔥 FETCH DEFAULT ADDRESS
            Address defaultAddress = addressService.getDefaultAddressEntity(email);
            if (defaultAddress == null) {
                throw new BadRequestException("Please add a shipping address before placing order");
            }

            Order order = new Order();
            order.setUserEmail(email);
            order.setCreatedAt(LocalDateTime.now());
            order.setPaymentMethod(paymentMethod);   // store the method

            if (paymentMethod == PaymentMethod.COD) {
                order.setStatus(OrderStatus.CONFIRMED);
                order.setPaymentStatus(PaymentStatus.PENDING);
            } else { // ONLINE
                order.setStatus(OrderStatus.AWAITING_PAYMENT);
                order.setPaymentStatus(PaymentStatus.PENDING);
                order.setPaymentExpiry(LocalDateTime.now().plusMinutes(15));
            }
// then set delivery info, items, etc.

            // 🔥 SNAPSHOT DELIVERY INFO
            order.setDeliveryFullName(defaultAddress.getFullName());
            order.setDeliveryPhone(defaultAddress.getPhone());
            order.setDeliveryAddressLine(defaultAddress.getStreet() + ", " + defaultAddress.getCity() + ", " + defaultAddress.getState() + " - " + defaultAddress.getPincode());

            double total = 0;
            List<OrderItem> orderItems = new ArrayList<>();
            List<Product> updatedProducts = new ArrayList<>();

            for (CartItem c : cartItems) {
                Product product = c.getProduct();
                if (product == null) {
                    throw new BadRequestException("Product not found in cart");
                }
                if (product.getStock() < c.getQuantity()) {
                    throw new BadRequestException("Stock not available for " + product.getName());
                }

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProduct(product);
                item.setQuantity(c.getQuantity());
                item.setPrice(product.getPrice());
                orderItems.add(item);

                total += product.getPrice() * c.getQuantity();

                product.setStock(product.getStock() - c.getQuantity());
                product.updateStatusBasedOnStock();
                updatedProducts.add(product);
            }

            order.setItems(orderItems);
            order.setTotalAmount(total);

            orderRepo.save(order);
            productRepo.saveAll(updatedProducts);
            cartRepo.deleteByUserEmail(email);

            return mapToResponse(order);

        } catch (OptimisticLockException e) {
            if (retriesLeft <= 0) {
                throw new BadRequestException("Stock update conflict, please retry");
            }
            entityManager.clear();
            return placeOrderWithRetry(email, paymentMethod, retriesLeft - 1);
        }
    }

    // ===================== GET ORDERS =====================
    @Override
    public PageResponse<OrderResponse> getMyOrders(String email, OrderStatus status,
                                                   LocalDateTime fromDate, LocalDateTime toDate,
                                                   int page, int size, String sortBy, String direction) {

        PaginationUtils.validate(page, size, sortBy,
                List.of("createdAt", "totalAmount", "status"), direction);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userEmail"), email));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> orderPage = orderRepo.findAll(spec, pageable);
        List<OrderResponse> list = orderPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(list, orderPage.getNumber(), orderPage.getSize(),
                orderPage.getTotalElements(), orderPage.getTotalPages(), orderPage.isLast());
    }

    // ===================== CANCEL =====================
    @Override
    @Transactional
    public void cancelOrder(Long id, String email) {
        Order order = getOrderOrThrow(id);
        if (!order.getUserEmail().equals(email)) {
            throw new UnauthorizedException("Unauthorized");
        }
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Cannot cancel paid order");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel delivered order");
        }
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel at this stage");
        }

        // restore stock
        List<Product> products = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                products.add(product);
            }
        }
        productRepo.saveAll(products);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
    }

    // ===================== UPDATE STATUS =====================
    @Override
    public void updateStatus(Long id, OrderStatus newStatus) {
        Order order = getOrderOrThrow(id);
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        orderRepo.save(order);
    }

    // ===================== PAYMENT =====================
    @Override
    @Transactional
    public void payOrder(Long id, String email) {
        Order order = getOrderOrThrow(id);

        // 🔥 Only ONLINE orders can be paid
        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new BadRequestException("Payment not required for COD orders");
        }

        if (!order.getUserEmail().equals(email)) {
            throw new UnauthorizedException("Unauthorized");
        }
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Already paid");
        }
        // Allow payment for AWAITING_PAYMENT or CREATED (backward compatibility)
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT && order.getStatus() != OrderStatus.CREATED) {
            throw new BadRequestException("Payment only allowed for orders awaiting payment");
        }
        if (!simulatePayment()) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepo.save(order);
            throw new BadRequestException("Payment failed");
        }
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order);
    }

    @Override
    @Transactional
    public void retryPayment(Long id, String email) {
        Order order = getOrderOrThrow(id);
        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new BadRequestException("Retry only for online payments");
        }
        if (!order.getUserEmail().equals(email)) {
            throw new UnauthorizedException("Unauthorized");
        }
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Already paid");
        }
        if (order.getPaymentStatus() != PaymentStatus.FAILED && order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Retry not allowed");
        }
        if (!simulatePayment()) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepo.save(order);
            throw new BadRequestException("Payment failed again");
        }
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order);
    }

    @Override
    @Transactional
    public void refundOrder(Long id, String email) {
        Order order = getOrderOrThrow(id);
        if (!order.getUserEmail().equals(email)) {
            throw new UnauthorizedException("Unauthorized");
        }
        if (order.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Cannot refund unpaid order");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Refund allowed only after delivery");
        }
        if (!simulateRefund()) {
            throw new BadRequestException("Refund failed, try again");
        }
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setStatus(OrderStatus.CANCELLED);

        List<Product> products = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                products.add(product);
            }
        }
        productRepo.saveAll(products);
        orderRepo.save(order);
    }

    // ===================== DELIVERY PARTNER METHODS =====================
    @Override
    @Transactional
    public void assignOrderToDeliveryPartner(Long orderId, String partnerEmail) {
        Order order = getOrderOrThrow(orderId);
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PAID) {
            throw new BadRequestException("Order cannot be assigned for delivery at current status");
        }
        User partner = userRepo.findByEmail(partnerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));
        if (partner.getRole() != Role.DELIVERY_PARTNER) {
            throw new BadRequestException("User is not a delivery partner");
        }
        if (partner.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Delivery partner is not active");
        }
        order.setDeliveryPartnerEmail(partnerEmail);
        order.setAssignedAt(LocalDateTime.now());
        orderRepo.save(order);
    }

    @Override
    @Transactional
    public void pickupOrder(Long orderId, String partnerEmail) {
        Order order = getOrderOrThrow(orderId);
        validatePartnerAccess(order, partnerEmail);
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PAID) {
            throw new BadRequestException("Order cannot be picked up at current status");
        }
        if (order.getDeliveryPartnerEmail() == null) {
            throw new BadRequestException("No delivery partner assigned to this order");
        }
        order.setStatus(OrderStatus.SHIPPED);
        order.setPickedUpAt(LocalDateTime.now());
        orderRepo.save(order);
    }

    @Override
    @Transactional
    public void deliverOrder(Long orderId, String partnerEmail) {
        Order order = getOrderOrThrow(orderId);
        validatePartnerAccess(order, partnerEmail);
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BadRequestException("Order must be picked up before delivery");
        }
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepo.save(order);
    }

    @Override
    public Map<String, Object> getDeliveryInfoForUser(Long orderId, String userEmail) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getUserEmail().equals(userEmail)) {
            throw new UnauthorizedException("You can only view your own orders");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("orderStatus", order.getStatus());
        info.put("deliveryStatus", getDeliveryStatusText(order));
        if (order.getDeliveryPartnerEmail() != null) {
            User partner = userRepo.findByEmail(order.getDeliveryPartnerEmail()).orElse(null);
            if (partner != null) {
                info.put("partnerName", partner.getName() != null ? partner.getName() : partner.getEmail());
                info.put("partnerPhone", partner.getPhoneNumber() != null ? partner.getPhoneNumber() : "Not available");
            }
        }
        return info;
    }

    @Override
    public List<OrderResponse> getAssignedOrders(String partnerEmail) {
        List<Order> orders = orderRepo.findByDeliveryPartnerEmail(partnerEmail);
        return orders.stream().map(this::mapToResponse).toList();
    }

    // ===================== HELPERS =====================
    private Order getOrderOrThrow(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private boolean simulatePayment() {
        return Math.random() > 0.2;
    }

    private boolean simulateRefund() {
        return Math.random() > 0.1;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        switch (current) {
            case CREATED -> {
                if (next != OrderStatus.CONFIRMED && next != OrderStatus.CANCELLED)
                    throw new BadRequestException("Invalid status transition");
            }
            case CONFIRMED -> {
                if (next != OrderStatus.SHIPPED && next != OrderStatus.CANCELLED)
                    throw new BadRequestException("Invalid status transition");
            }
            case SHIPPED -> {
                if (next != OrderStatus.DELIVERED)
                    throw new BadRequestException("Invalid status transition");
            }
            case DELIVERED, CANCELLED ->
                    throw new BadRequestException("Order already completed");
        }
    }

    private void validatePartnerAccess(Order order, String partnerEmail) {
        if (order.getDeliveryPartnerEmail() == null || !order.getDeliveryPartnerEmail().equals(partnerEmail)) {
            throw new UnauthorizedException("You are not assigned to this order");
        }
    }

    private String getDeliveryStatusText(Order order) {
        if (order.getDeliveredAt() != null) return "DELIVERED on " + order.getDeliveredAt();
        if (order.getPickedUpAt() != null) return "OUT FOR DELIVERY (picked up at " + order.getPickedUpAt() + ")";
        if (order.getAssignedAt() != null) return "ASSIGNED to partner";
        return "WAITING FOR ASSIGNMENT";
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream().map(this::mapItem).toList();
        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                items,
                order.getDeliveryFullName(),
                order.getDeliveryPhone(),
                order.getDeliveryAddressLine()
        );
    }

    private OrderItemResponse mapItem(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProduct() != null ? item.getProduct().getName() : "Deleted Product",
                item.getQuantity(),
                item.getPrice()
        );
    }
}