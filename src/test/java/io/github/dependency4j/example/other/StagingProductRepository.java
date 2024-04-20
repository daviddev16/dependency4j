package io.github.dependency4j.example.other;

import io.github.dependency4j.Managed;
import io.github.dependency4j.Strategy;

@Managed(strategy = @Strategy({"Staging"}))
public class StagingProductRepository implements IProductRepository{ }
