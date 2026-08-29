package kr.yuns.dropthepitchserver.product.controller;

import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.common.security.SecurityUtil;
import kr.yuns.dropthepitchserver.product.data.dto.ProductRequestDto;
import kr.yuns.dropthepitchserver.product.data.dto.ProductResponseDto;
import kr.yuns.dropthepitchserver.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public GlobalResponse<Void> createProduct(@Valid @RequestBody ProductRequestDto productRequestDto) {
        return productService.createProduct(SecurityUtil.getUsername(), productRequestDto);
    }

    @GetMapping
    public GlobalResponse<ProductResponseDto> getProduct(@RequestParam Long id) {
        return productService.getProduct(id);
    }
}
