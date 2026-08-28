package com.hisabkitab.backend.order.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.order.domain.OrderItemMeasurementEntity;
import com.hisabkitab.backend.order.domain.OrderStatus;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemMeasurementRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemMeasurementResponse;
import com.hisabkitab.backend.product.domain.ProductEntity;
import com.hisabkitab.backend.product.domain.ProductMeasurementEntity;
import com.hisabkitab.backend.product.infrastructure.ProductMeasurementRepository;
import com.hisabkitab.backend.product.infrastructure.ProductRepository;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemResponse;
import com.hisabkitab.backend.order.interfaces.dto.OrderResponse;
import com.hisabkitab.backend.buyer.domain.BuyerEntity;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.order.domain.OrderEntity;
import com.hisabkitab.backend.order.domain.OrderItemEntity;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.buyer.infrastructure.BuyerRepository;
import com.hisabkitab.backend.customer.infrastructure.CustomerRepository;
import com.hisabkitab.backend.order.infrastructure.OrderRepository;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final BuyerRepository buyerRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final SecurityUtils securityUtils;
    private final ProductRepository productRepository;
    private final ProductMeasurementRepository productMeasurementRepository;

    // =========================================================
    // CREATE ORDER
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<OrderResponse> createOrder(
            Long organizationId,
            Long customerId,
            OrderRequest request) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        BuyerEntity buyer = null;

        if (request.getBuyerId() != null) {

            buyer = getBuyer(
                    customer.getId(),
                    request.getBuyerId()
            );
        }

        LocalDateTime orderDate =
                request.getOrderDate() != null
                        ? request.getOrderDate()
                        : LocalDateTime.now();

        List<OrderItemEntity> items = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            BigDecimal amount =
                    itemRequest.getQuantity()
                            .multiply(itemRequest.getRate());

            OrderItemEntity item = OrderItemEntity.builder()
                    .quantity(itemRequest.getQuantity())
                    .rate(itemRequest.getRate())
                    .amount(amount)
                    .notes(itemRequest.getNotes())
                    .build();

            addProductAndMeasurements(
                    item,
                    itemRequest,
                    organization.getId()
            );

            items.add(item);

            totalAmount = totalAmount.add(amount);
        }

        OrderEntity order = OrderEntity.builder()
                .organization(organization)
                .customer(customer)
                .buyer(buyer)
                .orderDate(orderDate)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .notes(request.getNotes())
                .items(items)
                .build();

        /*
         * Maintain bidirectional relationship.
         */
        for (OrderItemEntity item : items) {
            item.setOrder(order);
        }

        orderRepository.save(order);

        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order created successfully.")
                .data(toResponse(order))
                .build();
    }

    // =========================================================
    // GET ALL ORDERS
    // =========================================================

    @Override
    public ApiResponse<List<OrderResponse>> getOrders(
            Long organizationId,
            Long customerId) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        List<OrderResponse> orders =
                orderRepository
                        .findAllByCustomerId(customer.getId())
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse.<List<OrderResponse>>builder()
                .success(true)
                .message("Orders fetched successfully.")
                .data(orders)
                .build();
    }

    // =========================================================
    // GET SINGLE ORDER
    // =========================================================

    @Override
    public ApiResponse<OrderResponse> getOrder(
            Long organizationId,
            Long customerId,
            Long orderId) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        OrderEntity order =
                getOrderEntity(
                        customer.getId(),
                        orderId
                );

        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order fetched successfully.")
                .data(toResponse(order))
                .build();
    }

    // =========================================================
    // UPDATE ORDER
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<OrderResponse> updateOrder(
            Long organizationId,
            Long customerId,
            Long orderId,
            OrderRequest request) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        OrderEntity order =
                getOrderEntity(
                        customer.getId(),
                        orderId
                );

        /*
         * Completed and cancelled orders should not be edited.
         */
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException(
                    "Completed order cannot be modified."
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException(
                    "Cancelled order cannot be modified."
            );
        }

        BuyerEntity buyer = null;

        if (request.getBuyerId() != null) {

            buyer = getBuyer(
                    customer.getId(),
                    request.getBuyerId()
            );
        }

        /*
         * Remove old items and create updated items.
         */
        order.getItems().clear();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            BigDecimal amount =
                    itemRequest.getQuantity()
                            .multiply(itemRequest.getRate());

            OrderItemEntity item = OrderItemEntity.builder()
                    .quantity(itemRequest.getQuantity())
                    .rate(itemRequest.getRate())
                    .amount(amount)
                    .notes(itemRequest.getNotes())
                    .build();

            addProductAndMeasurements(
                    item,
                    itemRequest,
                    organization.getId()
            );
order.getItems().add(item);


            totalAmount = totalAmount.add(amount);
        }

        order.setBuyer(buyer);

        if (request.getOrderDate() != null) {
            order.setOrderDate(request.getOrderDate());
        }

        order.setTotalAmount(totalAmount);
        order.setNotes(request.getNotes());

        orderRepository.save(order);

        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order updated successfully.")
                .data(toResponse(order))
                .build();
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<String> updateOrderStatus(
            Long organizationId,
            Long customerId,
            Long orderId,
            OrderStatus status) {

        OrganizationEntity organization =
                getAccessibleOrganization(organizationId);

        CustomerEntity customer =
                getCustomer(organization.getId(), customerId);

        OrderEntity order =
                getOrderEntity(
                        customer.getId(),
                        orderId
                );

        validateStatusTransition(
                order.getStatus(),
                status
        );

        order.setStatus(status);

        if (status == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }

        orderRepository.save(order);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Order status updated successfully.")
                .data("Order status updated to " + status + ".")
                .build();
    }

    // =========================================================
    // REUSABLE METHODS
    // =========================================================

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

    private BuyerEntity getBuyer(
            Long customerId,
            Long buyerId) {

        return buyerRepository
                .findByIdAndCustomerId(
                        buyerId,
                        customerId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Buyer does not belong to this customer."
                        ));
    }

    private OrderEntity getOrderEntity(
            Long customerId,
            Long orderId) {

        return orderRepository
                .findByIdAndCustomerId(
                        orderId,
                        customerId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found."
                        ));
    }

    private void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        if (currentStatus == newStatus) {
            throw new RuntimeException(
                    "Order already has status " + currentStatus + "."
            );
        }

        if (currentStatus == OrderStatus.COMPLETED) {
            throw new RuntimeException(
                    "Completed order cannot be changed."
            );
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new RuntimeException(
                    "Cancelled order cannot be changed."
            );
        }

        /*
         * PENDING
         *   ↓
         * IN_PROGRESS
         *   ↓
         * COMPLETED
         *
         * PENDING can also be cancelled.
         * IN_PROGRESS can also be cancelled.
         */

        if (currentStatus == OrderStatus.PENDING) {

            if (newStatus != OrderStatus.IN_PROGRESS
                    && newStatus != OrderStatus.CANCELLED) {

                throw new RuntimeException(
                        "Invalid order status transition."
                );
            }
        }

        if (currentStatus == OrderStatus.IN_PROGRESS) {

            if (newStatus != OrderStatus.COMPLETED
                    && newStatus != OrderStatus.CANCELLED) {

                throw new RuntimeException(
                        "Invalid order status transition."
                );
            }
        }
    }

    private OrderResponse toResponse(OrderEntity order) {

        List<OrderItemResponse> itemResponses =
                order.getItems()
                        .stream()
                        .map(item -> {

                            List<OrderItemMeasurementResponse> measurements =
                                    item.getMeasurements()
                                            .stream()
                                            .map(measurement ->
                                                    OrderItemMeasurementResponse.builder()
                                                            .measurementId(
                                                                    measurement.getMeasurement().getId()
                                                            )
                                                            .measurementName(
                                                                    measurement.getMeasurementName()
                                                            )
                                                            .unit(
                                                                    measurement.getUnit()
                                                            )
                                                            .value(
                                                                    measurement.getValue()
                                                            )
                                                            .build()
                                            )
                                            .toList();

                            return OrderItemResponse.builder()
                                    .id(item.getId())
                                    .productName(item.getProduct().getName())
                                    .productId(item.getProduct().getId())
                                    .quantity(item.getQuantity())
                                    .rate(item.getRate())
                                    .amount(item.getAmount())
                                    .notes(item.getNotes())
                                    .measurements(measurements)
                                    .build();
                        })
                        .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .organizationId(
                        order.getOrganization().getId()
                )
                .customerId(
                        order.getCustomer().getId()
                )
                .buyerId(
                        order.getBuyer() != null
                                ? order.getBuyer().getId()
                                : null
                )
                .buyerName(
                        order.getBuyer() != null
                                ? order.getBuyer().getName()
                                : null
                )
                .orderDate(order.getOrderDate())
                .completedAt(order.getCompletedAt())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private void addProductAndMeasurements(
            OrderItemEntity orderItem,
            OrderItemRequest request,
            Long organizationId) {

        ProductEntity product =
                productRepository
                        .findByIdAndOrganizationId(
                                request.getProductId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found or does not belong to this organization."
                                ));

        orderItem.setProduct(product);

        List<ProductMeasurementEntity> activeMeasurements =
                product.getMeasurements()
                        .stream()
                        .filter(ProductMeasurementEntity::getActive)
                        .toList();

        Map<Long, ProductMeasurementEntity> configuredMeasurements =
                activeMeasurements.stream()
                        .collect(Collectors.toMap(
                                pm -> pm.getMeasurement().getId(),
                                pm -> pm
                        ));

        Set<Long> requestedMeasurementIds =
                request.getMeasurements()
                        .stream()
                        .map(OrderItemMeasurementRequest::getMeasurementId)
                        .collect(Collectors.toSet());

        /*
         * Make sure the request does not contain a measurement
         * that isn't configured for this product.
         */
        for (Long measurementId : requestedMeasurementIds) {

            if (!configuredMeasurements.containsKey(measurementId)) {

                throw new RuntimeException(
                        "Measurement ID " + measurementId +
                                " is not configured for product " +
                                product.getName() + "."
                );
            }
        }

        /*
         * Make sure every required measurement is provided.
         */
        for (ProductMeasurementEntity productMeasurement :
                activeMeasurements) {

            if (Boolean.TRUE.equals(productMeasurement.getRequired())
                    && !requestedMeasurementIds.contains(
                    productMeasurement.getMeasurement().getId())) {

                throw new RuntimeException(
                        "Required measurement '" +
                                productMeasurement.getMeasurement().getName() +
                                "' is missing."
                );
            }
        }

        /*
         * Create historical measurement values for this order item.
         */
        for (OrderItemMeasurementRequest measurementRequest :
                request.getMeasurements()) {

            ProductMeasurementEntity configuredMeasurement =
                    configuredMeasurements.get(
                            measurementRequest.getMeasurementId()
                    );

            OrderItemMeasurementEntity orderMeasurement =
                    OrderItemMeasurementEntity.builder()
                            .orderItem(orderItem)
                            .measurement(
                                    configuredMeasurement.getMeasurement()
                            )
                            .measurementName(
                                    configuredMeasurement
                                            .getMeasurement()
                                            .getName()
                            )
                            .unit(
                                    configuredMeasurement
                                            .getMeasurement()
                                            .getUnit()
                            )
                            .value(measurementRequest.getValue())
                            .build();

            orderItem.getMeasurements()
                    .add(orderMeasurement);
        }
    }
}