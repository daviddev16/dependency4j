package com.dependency4j.example.service;

import com.dependency4j.Pull;
import com.dependency4j.example.other.IProductRepository;

public class MyProductService {

    private @Pull IProductRepository productRepository;

    public MyProductService() {}

    public IProductRepository getProductRepository() {
        return productRepository;
    }

}
