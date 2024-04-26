package io.github.dependency4j.example.other;

import io.github.dependency4j.Managed;

@Managed(dynamic = true)
public class DynamicComponent {

    public String value() { return "DynamicComponent"; }

}
