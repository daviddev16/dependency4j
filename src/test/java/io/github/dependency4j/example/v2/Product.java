package io.github.dependency4j.example.v2;

public class Product extends BaseEntity {
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Product [name=" + getName() + ", id=" + getId() + "]";
	}
	
}
