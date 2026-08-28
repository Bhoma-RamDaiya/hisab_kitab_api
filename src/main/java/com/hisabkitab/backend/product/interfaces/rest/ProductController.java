package com.hisabkitab.backend.product.interfaces.rest;

import com.hisabkitab.backend.product.application.ProductService;
import com.hisabkitab.backend.product.interfaces.dto.CreateProductRequest;
import com.hisabkitab.backend.product.interfaces.dto.ProductResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations/{organizationId}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateProductRequest request) {

        return ResponseEntity.ok(
                productService.createProduct(
                        organizationId,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
            @PathVariable Long organizationId) {

        return ResponseEntity.ok(
                productService.getProducts(
                        organizationId
                )
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable Long organizationId,
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                productService.getProduct(
                        organizationId,
                        productId
                )
        );
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long organizationId,
            @PathVariable Long productId,
            @Valid @RequestBody CreateProductRequest request) {

        return ResponseEntity.ok(
                productService.updateProduct(
                        organizationId,
                        productId,
                        request
                )
        );
    }
}