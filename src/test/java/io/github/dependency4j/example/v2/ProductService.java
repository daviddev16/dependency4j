package io.github.dependency4j.example.v2;


import io.github.dependency4j.Managed;
import io.github.dependency4j.Pull;
import io.github.dependency4j.util.StrUtil;

@Managed
public class ProductService {

	private IProductRepository productRepository;

	public static boolean alreadyCreated = false;

	@Pull
	public ProductService(IProductRepository productRepository) {
		if (alreadyCreated)
			throw new IllegalStateException("ProductService was duplicated");
		alreadyCreated = true;
		this.productRepository = productRepository;
	}
	
	public Product registerProduct(String productName) {
		return productRepository.registerProduct(productName);
	}
	
	public Product findProductByName(String name) {
		
		if (StrUtil.isNullOrBlank(name))
			throw new ServiceException("The name field was not filled.");
		
		return productRepository
				.findProductByName(name)
				.orElseThrow(() -> new NotFoundException("No product with name \"%s\" was found.".formatted(name)));
	}

}
