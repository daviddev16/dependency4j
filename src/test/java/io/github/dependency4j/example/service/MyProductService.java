package io.github.dependency4j.example.service;

import io.github.dependency4j.Pull;
import io.github.dependency4j.example.other.IProductRepository;

public class MyProductService {

    private @Pull IProductRepository productRepository;

    public MyProductService() {}

    public IProductRepository getProductRepository() {
        return productRepository;
    }

}
