package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.AddressRequest;
import com.example.ecommerce.dto.response.AddressResponse;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepo;

    @Transactional
    public AddressResponse addAddress(String userEmail, AddressRequest req) {
        if (req.isDefault()) {
            addressRepo.clearDefaultFlag(userEmail);
        }

        Address address = new Address();
        address.setUserEmail(userEmail);
        address.setFullName(req.getFullName());
        address.setPhone(req.getPhone());
        address.setStreet(req.getStreet());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPincode(req.getPincode());
        address.setDefault(req.isDefault());

        // If this is the first address, make it default automatically
        if (addressRepo.findByUserEmailOrderByIsDefaultDesc(userEmail).isEmpty()) {
            address.setDefault(true);
        }

        addressRepo.save(address);
        return mapToResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, String userEmail, AddressRequest req) {
        Address address = addressRepo.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (req.isDefault() && !address.isDefault()) {
            addressRepo.clearDefaultFlag(userEmail);
        }

        address.setFullName(req.getFullName());
        address.setPhone(req.getPhone());
        address.setStreet(req.getStreet());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPincode(req.getPincode());
        address.setDefault(req.isDefault());

        addressRepo.save(address);
        return mapToResponse(address);
    }

    @Transactional
    public void deleteAddress(Long id, String userEmail) {
        Address address = addressRepo.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        addressRepo.delete(address);
    }

    @Transactional
    public void setDefaultAddress(Long id, String userEmail) {
        Address address = addressRepo.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        addressRepo.clearDefaultFlag(userEmail);
        address.setDefault(true);
        addressRepo.save(address);
    }

    public List<AddressResponse> getUserAddresses(String userEmail) {
        return addressRepo.findByUserEmailOrderByIsDefaultDesc(userEmail)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AddressResponse getDefaultAddress(String userEmail) {
        return addressRepo.findByUserEmailOrderByIsDefaultDesc(userEmail)
                .stream()
                .filter(Address::isDefault)
                .findFirst()
                .map(this::mapToResponse)
                .orElse(null);
    }

    private AddressResponse mapToResponse(Address a) {
        return new AddressResponse(
                a.getId(),
                a.getFullName(),
                a.getPhone(),
                a.getStreet(),
                a.getCity(),
                a.getState(),
                a.getPincode(),
                a.isDefault()
        );
    }
    public Address getDefaultAddressEntity(String userEmail) {
        return addressRepo.findByUserEmailOrderByIsDefaultDesc(userEmail)
                .stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElse(null);
    }
}