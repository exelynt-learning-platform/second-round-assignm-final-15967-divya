package com.example.demo.serviceImpl;
import java.util.Optional;  
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
import com.example.demo.exception.OrderException;
import com.example.demo.exception.ProductException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.ProductService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	UserRepository userRepository;

	@CacheEvict(value = "productsCache", allEntries = true)
	public Product create(Product product, String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

	    if (productRepository.existsByNameAndUser(product.getName(), user)) {
	        throw new ProductException("Product already exists for this user");
	    }

		product.setUser(user);
		return productRepository.save(product);
	}

	@Cacheable(value = "productsCache", key = "#email + '-' + #page + '-' + #size")
	public Page<Product> getProductsByOwner(String email, int page, int size) {

	    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new ProductException("User not found"));

	    Page<Product> products = productRepository.findByUser(user, pageable);

	    if (products.isEmpty()) {
	        throw new ProductException("No products found for this user");
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

		if (existingProduct.isDeleted()) {
			throw new OrderException("Product is no longer available");
		}


		existingProduct.setName(updatedProduct.getName());
		existingProduct.setPrice(updatedProduct.getPrice());
		existingProduct.setDescription(updatedProduct.getDescription());

		return productRepository.save(existingProduct);
	}

	@Override
	@CacheEvict(value = "productsCache", allEntries = true)
	public boolean delete(Integer id) {

	    String email = SecurityUtil.getCurrentUserEmail();
	    Optional<User> currentUser = userRepository.findByEmail(email);

	    Optional<Product> optionalProduct = productRepository.findById(id);

	    if (optionalProduct.isEmpty()) {
	        log.warn("Product not found with id: {}", id);
	        return false;
	    }

	    Product product = optionalProduct.get();

	    if (!product.getUser().getId().equals(currentUser.get().getId())) {
	        log.warn("Unauthorized delete attempt by user: {} for product id: {}", email, id);
	        throw new RuntimeException("You are not allowed to delete this product");
	    }

	    productRepository.delete(product);

	    log.info("Product soft deleted successfully. Id: {}", id);

	    return true;
	}

	
	public Page<Product> getAllProducts(int page, int size) {

	    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

	    Page<Product> products = productRepository.findByIsDeleted(false, pageable);

	    if (products.isEmpty()) {
	        throw new RuntimeException("No products found");
	    }

	    return products;
	}

}