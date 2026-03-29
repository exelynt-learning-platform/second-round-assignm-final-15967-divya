package com.example.demo.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.exception.ProductException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	UserRepository userRepository;

	@CacheEvict(value = "productsCache", allEntries = true)
	public Product create(Product product, String email) {

		if (productRepository.existsByNameAndPriceAndStockQuantity(product.getName(), product.getPrice(),
				product.getStockQuantity())) {

			throw new RuntimeException("Product already exists");
		}

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		product.setAddedbyuserid(user.getId());

		return productRepository.save(product);
	}

	@Cacheable(value = "productsCache", key = "#email + '-' + #page + '-' + #size")
	public Page<Product> getProductsByOwner(String email, int page, int size) {

		System.out.println("🔥 DB HIT - Method Executed");
		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		if (user == null) {
		    throw new ProductException("User not found");
		}
		Page<Product> products = productRepository.findAllProductsByOwnerid(user.getId(), pageable);

		if (products.isEmpty()) {
			throw new RuntimeException("No products found for this user");
		}

		return products;
	}

	@Override
	public Product getById(int id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ProductException("Product not found with id: " + id));
	}

	@Override
	@CacheEvict(value = "productsCache", allEntries = true)
	public Product update(Integer id, Product updatedProduct) {

		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new ProductException("Product not found with id: " + id));

		if (existingProduct.getIsdeleted() == 1) {
			throw new ProductException("Cannot update deleted product");
		}

		existingProduct.setName(updatedProduct.getName());
		existingProduct.setPrice(updatedProduct.getPrice());
		existingProduct.setDescription(updatedProduct.getDescription());

		return productRepository.save(existingProduct);
	}

	@Override
	@CacheEvict(value = "productsCache", allEntries = true)
	public boolean delete(Integer id) {

	    int updatedRows = productRepository.softDeleteProduct(id);

	    if (updatedRows == 0) {
	        throw new ProductException("Product not found with id: " + id);
	    }

	    return true;
	}

	
	public Page<Product> getAllProducts(int page, int size) {

	    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

	    Page<Product> products = productRepository.findByIsdeleted(0, pageable);

	    if (products.isEmpty()) {
	        throw new RuntimeException("No products found");
	    }

	    return products;
	}

}