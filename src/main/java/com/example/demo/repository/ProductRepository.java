package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	
	boolean existsByNameAndUserAndIsDeletedFalse(String name, User user);	

	Page<Product> findByUser(User userid, Pageable pageable);

	
	Page<Product> findByIsDeleted(boolean isDeleted, Pageable pageable);

	boolean existsByNameAndUserAndIdNotAndIsDeletedFalse(String name, User user, Integer id);
	
	Optional<Product> findByIdAndIsDeletedFalse(Integer id);
	}