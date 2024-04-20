package com.dependency4j;

import com.dependency4j.example.other.IProductRepository;
import com.dependency4j.example.other.StagingProductRepository;
import com.dependency4j.example.service.MyProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Main {


    @Test
    @DisplayName("Demonstration unit test")
    void demonstrateSequenceDiagramTest() {

        /* 1. Creating and installing the DependencyManager */
        DependencyManager dependencyManager = DependencyManager.builder()
                .strategy("Staging")
                .install("com.dependency4j.example")
                .getDependencyManager();

        /* 2. Fetching the IProductRepository implementation and testing */
        IProductRepository productRepository = dependencyManager.query(IProductRepository.class);
        Assertions.assertNotNull(productRepository);
        Assertions.assertEquals(StagingProductRepository.class, productRepository.getClass());

        /* 3. Installing the user's single instance */
        MyProductService myProductService = new MyProductService();

        /* -!- MyProductService contains a dependency object of IProductRepository, we should test it later  -!- */
        dependencyManager.installSingleInstance(myProductService);

        /* 4. Testing if the IProductRepository dependency was injected in MyProductService instance */
        IProductRepository productRepositoryFromMyService = myProductService.getProductRepository();
        Assertions.assertNotNull(productRepositoryFromMyService);
        Assertions.assertEquals(StagingProductRepository.class, productRepositoryFromMyService.getClass());

    }

}
