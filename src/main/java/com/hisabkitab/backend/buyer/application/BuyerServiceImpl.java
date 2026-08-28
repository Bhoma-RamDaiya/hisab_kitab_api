package com.hisabkitab.backend.buyer.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.buyer.interfaces.BuyerStatus;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.buyer.interfaces.BuyerRequest;
import com.hisabkitab.backend.buyer.interfaces.BuyerResponse;
import com.hisabkitab.backend.buyer.domain.BuyerEntity;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.buyer.infrastructure.BuyerRepository;
import com.hisabkitab.backend.customer.infrastructure.CustomerRepository;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.user.infrastructure.UserRepository;
import com.hisabkitab.backend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuyerServiceImpl implements BuyerService {

    private final BuyerRepository buyerRepository;
    private final CustomerRepository customerRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ApiResponse<BuyerResponse> createBuyer(
            Long organizationId,
            Long customerId,
            BuyerRequest request) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        if (buyerRepository
                .existsByCustomerIdAndNameIgnoreCase(
                        customerId,
                        request.getName())) {

            throw new RuntimeException(
                    "Buyer with this name already exists for this customer."
            );
        }

        UserEntity user = null;

        if (request.getUserId() != null) {

            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found."
                            ));
        }

        BuyerEntity buyer = BuyerEntity.builder()
                .customer(customer)
                .user(user)
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .relation(request.getRelation())
                .status(BuyerStatus.ACTIVE)
                .build();

        buyerRepository.save(buyer);

        return ApiResponse.<BuyerResponse>builder()
                .success(true)
                .message("Buyer created successfully.")
                .data(toResponse(buyer))
                .build();
    }

    @Override
    public ApiResponse<List<BuyerResponse>> getBuyers(
            Long organizationId,
            Long customerId) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        List<BuyerResponse> buyers =
                buyerRepository
                        .findAllByCustomerId(customer.getId())
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse.<List<BuyerResponse>>builder()
                .success(true)
                .message("Buyers fetched successfully.")
                .data(buyers)
                .build();
    }

    @Override
    public ApiResponse<BuyerResponse> getBuyer(
            Long organizationId,
            Long customerId,
            Long buyerId) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        BuyerEntity buyer =
                buyerRepository
                        .findByIdAndCustomerId(
                                buyerId,
                                customer.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Buyer not found."
                                ));

        return ApiResponse.<BuyerResponse>builder()
                .success(true)
                .message("Buyer fetched successfully.")
                .data(toResponse(buyer))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<BuyerResponse> updateBuyer(
            Long organizationId,
            Long customerId,
            Long buyerId,
            BuyerRequest request) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        BuyerEntity buyer =
                buyerRepository
                        .findByIdAndCustomerId(
                                buyerId,
                                customer.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Buyer not found."
                                ));

        boolean duplicate =
                buyerRepository
                        .existsByCustomerIdAndNameIgnoreCaseAndIdNot(
                                customerId,
                                request.getName(),
                                buyerId
                        );

        if (duplicate) {
            throw new RuntimeException(
                    "Buyer with this name already exists for this customer."
            );
        }

        UserEntity user = null;

        if (request.getUserId() != null) {

            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found."
                            ));
        }

        buyer.setName(request.getName());
        buyer.setPhoneNumber(request.getPhoneNumber());
        buyer.setRelation(request.getRelation());
        buyer.setUser(user);

        buyerRepository.save(buyer);

        return ApiResponse.<BuyerResponse>builder()
                .success(true)
                .message("Buyer updated successfully.")
                .data(toResponse(buyer))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<String> deactivateBuyer(
            Long organizationId,
            Long customerId,
            Long buyerId) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        BuyerEntity buyer =
                buyerRepository
                        .findByIdAndCustomerId(
                                buyerId,
                                customer.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Buyer not found."
                                ));

        if (buyer.getStatus() == BuyerStatus.INACTIVE) {

            throw new RuntimeException(
                    "Buyer is already inactive."
            );
        }

        buyer.setStatus(BuyerStatus.INACTIVE);

        buyerRepository.save(buyer);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Buyer deactivated successfully.")
                .data("Buyer deactivated successfully.")
                .build();
    }

    // --------------------------------------------------
    // Reusable methods
    // --------------------------------------------------

    private OrganizationEntity getAccessibleOrganization(
            Long organizationId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();

        OrganizationMemberEntity member =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not an active member of this organization."
                                ));

        return member.getOrganization();
    }

    private CustomerEntity getCustomer(
            Long organizationId,
            Long customerId) {

        return customerRepository
                .findByIdAndOrganizationId(
                        customerId,
                        organizationId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found."
                        ));
    }

    private BuyerResponse toResponse(
            BuyerEntity buyer) {

        return BuyerResponse.builder()
                .id(buyer.getId())
                .customerId(
                        buyer.getCustomer().getId()
                )
                .userId(
                        buyer.getUser() != null
                                ? buyer.getUser().getId()
                                : null
                )
                .name(buyer.getName())
                .phoneNumber(buyer.getPhoneNumber())
                .relation(buyer.getRelation())
                .status(buyer.getStatus())
                .createdAt(buyer.getCreatedAt())
                .updatedAt(buyer.getUpdatedAt())
                .build();
    }
}