package kr.yuns.dropthepitchserver.product.service;

import kr.yuns.dropthepitchserver.common.response.GlobalResponse;
import kr.yuns.dropthepitchserver.product.data.dto.ProductRequestDto;
import kr.yuns.dropthepitchserver.product.data.dto.ProductResponseDto;
import kr.yuns.dropthepitchserver.product.data.entity.Product;
import kr.yuns.dropthepitchserver.product.data.exception.ProductNotFoundException;
import kr.yuns.dropthepitchserver.product.data.repository.ProductRepository;
import kr.yuns.dropthepitchserver.user.data.entity.User;
import kr.yuns.dropthepitchserver.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final AuthService authService;

    private Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
    }

    @Transactional
    public GlobalResponse<Void> createProduct(String email, ProductRequestDto productRequestDto) {
        User user = authService.getUserEntity(email);

        productRepository.save(
                Product.builder()
                        .name(productRequestDto.getName())
                        .description(productRequestDto.getDescription())
                        .user(user)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return GlobalResponse.ok();
    }

    @Transactional(readOnly = true)
    public GlobalResponse<ProductResponseDto> getProduct(Long id) {
        Product product = getProductEntity(id);

        return GlobalResponse.ok(
                ProductResponseDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .createdAt(product.getCreatedAt())
                        .build()
        );
    }
}
