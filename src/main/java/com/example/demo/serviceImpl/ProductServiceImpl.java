package com.example.demo.serviceImpl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.DTO.ProductRequestDTO;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.common.PaginationUtil;
import com.example.demo.config.CartConfig;
import com.example.demo.constants.AppConstants;
import com.example.demo.enums.ProductSortField;
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
	private UserRepository userRepository;

	@Autowired
	private CartConfig cartConfig;

	@Override
	@CacheEvict(value = "productsCache", allEntries = true)
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Product create(ProductRequestDTO dto, String email) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ProductException(AppConstants.USER_NOT_FOUND));

		boolean exists = productRepository.existsByNameAndUserAndIsDeletedFalse(dto.getName(), user);

		if (exists) {
			throw new ProductException(AppConstants.PRODUCT_ALREADY_EXISTS);
		}

		Product product = mapToEntity(dto);
		product.setUser(user);

		try {
			return productRepository.save(product);
		} catch (DataIntegrityViolationException e) {
			throw new ProductException(AppConstants.DATABASE_ERROR);
		}
	}

	@Cacheable(value = "productsCache", key = "{#email, #page, #size, #sortBy, #sortDir}")
	public Page<Product> getProductsByOwner(String email, int page, int size, String sortBy, String sortDir) {

		Pageable pageable = PaginationUtil.createPageable(
		        page,
		        size,
		        sortBy,
		        sortDir,
		        cartConfig.getDefaultPage(),
		        cartConfig.getDefaultSize(),
		        ProductSortField::from
		);
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ProductException(AppConstants.USER_NOT_FOUND));

		Page<Product> products = productRepository.findByUser(user, pageable);

	
		return products;
	}

	@Override
	public Product getById(int id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ProductException(AppConstants.PRODUCT_NOT_FOUND + id));
	}

	@Override
	@CacheEvict(value = "productsCache", allEntries = true)
	public Product update(Integer id, Product updatedProduct) {

		String email = SecurityUtil.getCurrentUserEmail();

		User currentUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ProductException(AppConstants.USER_NOT_FOUND));

		Product existingProduct = productRepository.findByIdAndIsDeletedFalse(id)
				.orElseThrow(() -> new ProductException("Product not found or deleted"));

		validateProductOwnership(existingProduct, currentUser);

		boolean exists = productRepository.existsByNameAndUserAndIdNotAndIsDeletedFalse(updatedProduct.getName(),
				currentUser, id);

		if (exists) {
			throw new ProductException(AppConstants.PRODUCT_ALREADY_EXISTS);
		}

		validateProductFields(updatedProduct);
		existingProduct.setName(updatedProduct.getName());
		existingProduct.setPrice(updatedProduct.getPrice());
		existingProduct.setDescription(updatedProduct.getDescription());
		existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
		existingProduct.setImageUrl(updatedProduct.getImageUrl());

		return productRepository.save(existingProduct);
	}

	@CacheEvict(value = "productsCache", allEntries = true)
	public boolean delete(Integer id) {

		String email = SecurityUtil.getCurrentUserEmail();

		User currentUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ProductException(AppConstants.USER_NOT_FOUND));

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ProductException(AppConstants.PRODUCT_NOT_FOUND + id));

		validateProductOwnership(product, currentUser);

		product.setDeleted(true);
		productRepository.save(product);

		log.info("Product soft-deleted successfully. Id: {}", id);

		return true;
	}

	private void validateProductOwnership(Product product, User currentUser) {

		if (product == null || product.getUser() == null || currentUser == null) {
			throw new ProductException(AppConstants.UNAUTHORIZED_PRODUCT_ACCESS);
		}

		if (!Objects.equals(product.getUser().getId(), currentUser.getId())) {
			throw new ProductException(AppConstants.UNAUTHORIZED_PRODUCT_ACCESS);
		}
	}

	public Page<Product> getAllProducts(int page, int size, String sortBy, String sortDir) {

		Pageable pageable = PaginationUtil.createPageable(
		        page,
		        size,
		        sortBy,
		        sortDir,
		        cartConfig.getDefaultPage(),
		        cartConfig.getDefaultSize(),
		        ProductSortField::from
		);
		Page<Product> products = productRepository.findByIsDeleted(false, pageable);

		if (products.isEmpty()) {
			throw new ProductException(AppConstants.NO_PRODUCTS_FOUND);
		}

		return products;
	}

	private void validateProductFields(Product updatedProduct) {

		Integer stock = updatedProduct.getStockQuantity();

		// Null check
		if (stock == null) {
			throw new ProductException(AppConstants.INVALID_QUANTITY);
		}

		// Min validation
		if (stock < cartConfig.getMinQuantity()) {
			throw new ProductException(AppConstants.INVALID_QUANTITY);
		}

		// Max validation
		if (stock > cartConfig.getMaxQuantity()) {
			throw new ProductException(AppConstants.MAX_QUANTITY_EXCEEDED + cartConfig.getMaxQuantity());
		}

		// Image URL validation
		String imageUrl = updatedProduct.getImageUrl();

		if (imageUrl != null && imageUrl.isBlank()) {
			throw new ProductException(AppConstants.INVALID_PRODUCT);
		}
	}

	private Product mapToEntity(ProductRequestDTO dto) {

		Product product = new Product();
		product.setName(dto.getName());
		product.setPrice(dto.getPrice());
		product.setDescription(dto.getDescription());
		product.setStockQuantity(dto.getStockQuantity());
		product.setImageUrl(dto.getImageUrl());

		return product;
	}

	
}