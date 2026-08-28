package com.hisabkitab.backend.customer.application;


import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.customer.domain.CustomerStatus;
import com.hisabkitab.backend.customer.domain.CustomerType;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerRequest;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerResponse;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.customer.infrastructure.CustomerRepository;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.organization.infrastructure.OrganizationRepository;
import com.hisabkitab.backend.user.infrastructure.UserRepository;
import com.hisabkitab.backend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final OrganizationMemberRepository organizationMemberRepository;

    @Override
    @Transactional
    public ApiResponse<CustomerResponse> createCustomer(
            CustomerRequest request , Long organizationId) {

//        UserEntity currentUser = securityUtils.getCurrentUser();

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        validateCustomerReference(request);

        if (customerRepository
                .existsByOrganizationIdAndCustomerNameIgnoreCase(
                        organization.getId(),
                        request.getCustomerName())) {

            throw new RuntimeException(
                    "Customer with this name already exists."
            );
        }

        UserEntity linkedUser = null;
        OrganizationEntity linkedOrganization = null;

        if (request.getLinkedUserId() != null) {

            linkedUser = userRepository.findById(
                            request.getLinkedUserId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Linked user not found."));
        }

        if (request.getLinkedOrganizationId() != null) {

            linkedOrganization = organizationRepository.findById(
                            request.getLinkedOrganizationId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Linked organization not found."));
        }

        CustomerEntity customer = CustomerEntity.builder()
                .organization(organization)
                .customerType(request.getCustomerType())
                .customerName(request.getCustomerName())
                .linkedUser(linkedUser)
                .linkedOrganization(linkedOrganization)
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .status(CustomerStatus.ACTIVE)
                .build();

        customerRepository.save(customer);

        return ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer created successfully.")
                .data(toResponse(customer))
                .build();
    }

    @Override
    public ApiResponse<CustomerResponse> getCustomer(
            Long organizationId,
            Long customerId) {

        OrganizationEntity organization =
                getAccessibleOrganization(
                        organizationId);

        CustomerEntity customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                customerId,
                                organization.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found."));

        return ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer fetched successfully.")
                .data(toResponse(customer))
                .build();
    }

    @Override
    public ApiResponse<List<CustomerResponse>> getCustomers(Long organizationId) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        List<CustomerResponse> customers =
                customerRepository
                        .findAllByOrganizationId(
                                organization.getId())
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse.<List<CustomerResponse>>builder()
                .success(true)
                .message("Customers fetched successfully.")
                .data(customers)
                .build();
    }

    // 3. Important: don't allow changing customer type casually later
    //
    //Right now update allows:
    //
    //FAMILY → BUSINESS
    //BUSINESS → ORGANIZATION
    //USER → FAMILY
    //
    //But once this customer has orders, bills and payments, changing its identity/type can create accounting problems.
    @Override
    @Transactional
    public ApiResponse<CustomerResponse> updateCustomer(
            Long organizationId,
            Long customerId,
            CustomerRequest request) {

        OrganizationEntity organization =
                getAccessibleOrganization(
                       organizationId);

        CustomerEntity customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                customerId,
                                organization.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found."));

        validateCustomerReference(request);
        boolean duplicate =
                customerRepository
                        .existsByOrganizationIdAndCustomerNameIgnoreCaseAndIdNot(
                                organization.getId(),
                                request.getCustomerName(),
                                customerId
                        );

        if (duplicate) {
            throw new RuntimeException(
                    "Customer with this name already exists."
            );
        }

        // As of now this line is  not change customerType accidentally for removing ambiguity
//        customer.setCustomerType(request.getCustomerType());
        customer.setCustomerName(request.getCustomerName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());

        if (request.getLinkedUserId() != null) {

            UserEntity linkedUser =
                    userRepository.findById(
                                    request.getLinkedUserId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Linked user not found."));

            customer.setLinkedUser(linkedUser);

        } else {
            customer.setLinkedUser(null);
        }

        if (request.getLinkedOrganizationId() != null) {

            OrganizationEntity linkedOrganization =
                    organizationRepository.findById(
                                    request.getLinkedOrganizationId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Linked organization not found."));

            customer.setLinkedOrganization(
                    linkedOrganization);

        } else {
            customer.setLinkedOrganization(null);
        }

        customerRepository.save(customer);

        return ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer updated successfully.")
                .data(toResponse(customer))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<String> deactivateCustomer(Long organizationId,
            Long customerId) {

        OrganizationEntity organization =
                getAccessibleOrganization(
                       organizationId);

        CustomerEntity customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                customerId,
                                organization.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found."));

        if (customer.getStatus()
                == CustomerStatus.INACTIVE) {

            throw new RuntimeException(
                    "Customer is already inactive.");
        }

        customer.setStatus(CustomerStatus.INACTIVE);

        customerRepository.save(customer);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Customer deactivated successfully.")
                .data("Customer deactivated successfully.")
                .build();
    }

    // --------------------------------------------------
    // Reusable methods
    // --------------------------------------------------



    private void validateCustomerReference(
            CustomerRequest request) {

        if (request.getCustomerType()
                == CustomerType.USER) {

            if (request.getLinkedUserId() == null) {
                throw new RuntimeException(
                        "Linked user is required for USER customer.");
            }

            if (request.getLinkedOrganizationId() != null) {
                throw new RuntimeException(
                        "Organization cannot be linked to USER customer.");
            }
        }

        if (request.getCustomerType()
                == CustomerType.ORGANIZATION) {

            if (request.getLinkedOrganizationId() == null) {
                throw new RuntimeException(
                        "Linked organization is required for ORGANIZATION customer.");
            }

            if (request.getLinkedUserId() != null) {
                throw new RuntimeException(
                        "User cannot be linked to ORGANIZATION customer.");
            }
        }

        if (request.getCustomerType()
                == CustomerType.WALK_IN
                || request.getCustomerType()
                == CustomerType.FAMILY
                || request.getCustomerType()
                == CustomerType.BUSINESS) {

            if (request.getLinkedUserId() != null
                    || request.getLinkedOrganizationId() != null) {

                throw new RuntimeException(
                        "Linked user or organization is not allowed for this customer type.");
            }
        }
    }

    private CustomerResponse toResponse(
            CustomerEntity customer) {

        return CustomerResponse.builder()
                .id(customer.getId())
                .organizationId(
                        customer.getOrganization().getId())
                .customerType(customer.getCustomerType())
                .customerName(customer.getCustomerName())
                .category(customer.getCategory())
                .linkedUserId(
                        customer.getLinkedUser() != null
                                ? customer.getLinkedUser().getId()
                                : null)
                .linkedOrganizationId(
                        customer.getLinkedOrganization() != null
                                ? customer.getLinkedOrganization().getId()
                                : null)
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    private OrganizationEntity getAccessibleOrganization(
            Long organizationId) {

        UserEntity currentUser = securityUtils.getCurrentUser();

        OrganizationMemberEntity member =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not an active member of this organization."
                                ));

        return member.getOrganization();
    }
}
