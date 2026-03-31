package com.example.demo.enums;

import java.util.Arrays;

import com.example.demo.exception.ProductException;

public enum ProductSortField {

	ID("id"), NAME("name"), DESCRIPTION("description"), PRICE("price"), STOCK("stockQuantity"), IMAGE_URL("imageUrl");

	private final String field;

	ProductSortField(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}
	public static String from(String value) {
	    return Arrays.stream(values())
	            .filter(f -> f.field.equalsIgnoreCase(value))
	            .map(ProductSortField::getField)
	            .findFirst()
	            .orElseThrow(() -> new ProductException("Invalid sort field: " + value));
	}
}