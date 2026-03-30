package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;

import jakarta.transaction.Transactional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	
	boolean existsByNameAndUser(String name, User user);	

	Page<Product> findByUser(User userid, Pageable pageable);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE product SET isdeleted = 1 WHERE id = ?1", nativeQuery = true)
	int softDeleteProduct( Integer id);
	
	Page<Product> findByIsdeleted(int isDeleted, Pageable pageable);
}