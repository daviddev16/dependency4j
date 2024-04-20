package io.github.dependency4j.example.v2;

import java.util.Optional;

import io.github.dependency4j.Managed;

@Managed(name = "InMemoryProductRepository")
public class ProductRepositoryImpl extends InMemoryRepository<Product> implements IProductRepository { 

	public static boolean alreadyCreated = false;

	public ProductRepositoryImpl() {
		if (alreadyCreated) {
			throw new IllegalStateException("ProductRepositoryImpl was duplicated");
		}
		alreadyCreated = true;
	}
	
	@Override
	public Product registerProduct(String productName) {
		Product product = new Product();
		product.setName(productName);
		return registerProduct(product);
	}

	@Override
	public Product registerProduct(Product product) {
		return create(product);
	}
	
	@Override
	public Optional<Product> findProductByName(String name) {
		return stream()
				.filter(product -> product.getName().equals(name))
				.findFirst();
	}
	
} 
