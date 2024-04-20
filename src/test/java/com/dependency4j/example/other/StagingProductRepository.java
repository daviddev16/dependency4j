package com.dependency4j.example.other;

import com.dependency4j.Managed;
import com.dependency4j.Strategy;

@Managed(strategy = @Strategy({"Staging"}))
public class StagingProductRepository implements IProductRepository{ }
