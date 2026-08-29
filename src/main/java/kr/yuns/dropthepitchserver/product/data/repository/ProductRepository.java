package kr.yuns.dropthepitchserver.product.data.repository;

import kr.yuns.dropthepitchserver.product.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
