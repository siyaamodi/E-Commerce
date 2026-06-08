package com.example.ecommerce.controller;

import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.dto.response.SellerApplicationResponse;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.service.AdminService;
import com.example.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
private final OrderService orderService;
    @PutMapping("/block/{email}")
    public ResponseEntity<ApiResponse<String>> blockUser(@PathVariable String email) {
        adminService.blockUserPermanently(email);
        return ResponseEntity.ok(new ApiResponse<>("User blocked permanently", null));
    }

    @PutMapping("/block-temp/{email}")
    public ResponseEntity<ApiResponse<String>> blockUserTemporarily(
            @PathVariable String email,
            @RequestParam int hours) {
        adminService.blockUserTemporarily(email, hours);
        return ResponseEntity.ok(new ApiResponse<>("User blocked for " + hours + " hours", null));
    }

    @PutMapping("/unblock/{email}")
    public ResponseEntity<ApiResponse<String>> unblockUser(@PathVariable String email) {
        adminService.unblockUser(email);
        return ResponseEntity.ok(new ApiResponse<>("User unblocked", null));
    }

    @PutMapping("/approve-seller/{email}")
    public ResponseEntity<ApiResponse<String>> approveSeller(@PathVariable String email) {
        adminService.approveSeller(email);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Seller approved successfully", null));
    }

    @PutMapping("/reject-seller/{email}")
    public ResponseEntity<ApiResponse<String>> rejectSeller(@PathVariable String email) {
        adminService.rejectSeller(email);
        return ResponseEntity.ok(new ApiResponse<>("Seller rejected", null));
    }

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<User>>> getAllSellers() {
        return ResponseEntity.ok(new ApiResponse<>("Sellers fetched", adminService.getAllSellers()));
    }
    @PostMapping("/assign-order/{orderId}/to/{partnerEmail}")
    public ResponseEntity<ApiResponse<String>> assignOrderToDeliveryPartner(
            @PathVariable Long orderId,
            @PathVariable String partnerEmail) {
        orderService.assignOrderToDeliveryPartner(orderId, partnerEmail);
        return ResponseEntity.ok(new ApiResponse<>("Order assigned to delivery partner", null));
    }
    @PutMapping("/set-role/{email}")
    public ResponseEntity<ApiResponse<String>> setRole(
            @PathVariable String email,
            @RequestParam Role role) {
        adminService.setUserRole(email, role);
        return ResponseEntity.ok(new ApiResponse<>("Role updated", null));
    }
    @GetMapping("/seller-applications")
    public ResponseEntity<ApiResponse<List<SellerApplicationResponse>>> getPendingSellerApplications() {
        return ResponseEntity.ok(new ApiResponse<>("Pending applications fetched",
                adminService.getPendingSellerApplications()));
    }
}