package io.github.dependency4j;

import io.github.dependency4j.example.v2.InMemorySetup;
import io.github.dependency4j.example.v2.Product;
import io.github.dependency4j.example.v2.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Checks the uniqueness of singleton instances")
public class UniquenessInstanceTest {

    @Test
    @DisplayName("Check for duplicated instance test [Issue: #2]")
    void checkForDuplicatedInstanceTest() {

        /* it should fails on dependency4j <= 1.0.0 */

        DependencyManager dependencyManager = DependencyManager.builder()
                .installPackage("io.github.dependency4j.example.v2")
                .getDependencyManager();

        InMemorySetup inMemorySetup = dependencyManager.query(InMemorySetup.class);
        inMemorySetup.createAllProducts();

        ProductService productService = dependencyManager.query(ProductService.class);

        Assertions.assertEquals(inMemorySetup.getProductService(), productService);

        Product productA = productService.findProductByName("ProductA");
        Assertions.assertNotNull(productA);
    }

}
