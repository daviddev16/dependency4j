package io.github.dependency4j.example.v2;

import io.github.dependency4j.Managed;
import io.github.dependency4j.Pull;

@Managed
public class InMemorySetup {

	private @Pull ProductService productService;

	public void createAllProducts() {
		productService.registerProduct("ProductA");
	}

	public ProductService getProductService() {
		return productService;
	}
}
