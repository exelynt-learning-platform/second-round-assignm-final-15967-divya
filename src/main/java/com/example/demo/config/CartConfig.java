package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CartConfig {

	@Value("${address.min.length}")
	private int minAddressLength;

	@Value("${address.max.length}")
	private int maxAddressLength;

	public int getMinAddressLength() {
		return minAddressLength;
	}

	public int getMaxAddressLength() {
		return maxAddressLength;
	}
}