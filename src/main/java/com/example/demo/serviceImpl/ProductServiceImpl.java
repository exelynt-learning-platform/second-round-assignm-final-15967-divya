package com.example.demo.serviceImpl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.DTO.ProductRequestDTO;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.common.PaginationUtil;
import com.example.demo.config.PaginationConfig;
import com.example.demo.config.ValidationConfig;
import com.example.demo.constants.AppConstants;
import com.example.demo.enums.ProductSortField;
import com.example.demo.exception.ProductException;
import com.example.demo.exception.ProductNotFoundException;
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
	private PaginationConfig paginationConfig;
	@Autowired
	private ValidationConfig validationConfig;

	@Override
	@CacheEvict(value = "productsCache", allEntries = true)
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public Product create(ProductRequestDTO dto) {
		String email = SecurityUtil.getCurrentUserEmail();

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

	@Override
	@Cacheable(value = "productsCache", key = "T(com.example.demo.security.SecurityUtil).getCurrentUserEmail() + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDir")
	public Page<Product> getProductsByOwner(int page, int size, String sortBy, String sortDir) {

		String email = SecurityUtil.getCurrentUserEmail();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ProductException(AppConstants.USER_NOT_FOUND));

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, sortDir,
				paginationConfig.getDefaultPage(), paginationConfig.getDefaultSize(), ProductSortField::from);

		return productRepository.findByUser(user, pageable);
	}

	@Override
	public Product getById(int id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ProductException(AppConstants.PRODUCT_NOT_FOUND + id));
		if (product.isDeleted()) {
			throw new ProductNotFoundException(AppConstants.PRODUCT_ALREADY_DELETED);
		}
		return product;
	}

	@Override
	@CacheEvict(value = "productsCache", allEntries = true)
	public Product update(Integer id, Product product) {

		String email = SecurityUtil.getCurrentUserEmail();

		User currentUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new ProductException(AppConstants.USER_NOT_FOUND));

		Product existingProduct = productRepository.findByIdAndIsDeletedFalse(id)
				.orElseThrow(() -> new ProductException("Product not found or deleted"));

		validateProductOwnership(existingProduct, currentUser);

		boolean exists = productRepository.existsByNameAndUserAndIdNotAndIsDeletedFalse(product.getName(), currentUser,
				id);

		if (exists) {
			throw new ProductException(AppConstants.PRODUCT_ALREADY_EXISTS);
		}

		validateProductFields(product);
		existingProduct.setName(product.getName());
		existingProduct.setPrice(product.getPrice());
		existingProduct.setDescription(product.getDescription());
		existingProduct.setStockQuantity(product.getStockQuantity());
		existingProduct.setImageUrl(product.getImageUrl());

		return productRepository.save(existingProduct);
	}

	@Override
	@Transactional
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

		if (product == null || product.getUser() == null) {
			throw new ProductException(AppConstants.UNAUTHORIZED_PRODUCT_ACCESS);
		}
		if (!Objects.equals(product.getUser().getId(), currentUser.getId())) {
			throw new ProductException(AppConstants.UNAUTHORIZED_PRODUCT_ACCESS);
		}
	}

	@Override
	public Page<Product> getAllProducts(int page, int size, String sortBy, String sortDir) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, sortDir,
				paginationConfig.getDefaultPage(), paginationConfig.getDefaultSize(), ProductSortField::from);
		Page<Product> products = productRepository.findByIsDeleted(false, pageable);

		return products;
	}

	private void validateProductFields(Product updatedProduct) {

		Integer stock = updatedProduct.getStockQuantity();

		// Null check
		if (stock == null) {
			throw new ProductException(AppConstants.INVALID_QUANTITY);
		}

		// Min validation
		if (stock < validationConfig.getMinQuantity()) {
			throw new ProductException(AppConstants.INVALID_QUANTITY);
		}

		// Max validation
		if (stock > validationConfig.getMaxQuantity()) {
			throw new ProductException(AppConstants.MAX_QUANTITY_EXCEEDED + validationConfig.getMaxQuantity());
		}

		// Image URL validation
		String imageUrl = updatedProduct.getImageUrl();

		if (imageUrl != null && imageUrl.trim().isEmpty()) {
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