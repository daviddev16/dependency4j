package io.github.dependency4j.example.v2;

import java.util.Optional;

public interface IProductRepository {

	Product registerProduct(Product product);
	
	Product registerProduct(String productName);
	
	Optional<Product> findProductByName(String name);
	
}
