package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entity.Order;
import com.example.demo.Entity.Product;
import com.example.demo.constants.AppConstants;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/saveProduct")
    public ResponseEntity<Product> create(@RequestBody Product product) {

        String email = SecurityUtil.getCurrentUserEmail();
        log.debug("Creating product for user: {}", email);

        Product savedProduct = productService.create(product, email);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @GetMapping("/getProductsByOwner")
    public ResponseEntity<Page<Product>> getProductsByOwner(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        String email = SecurityUtil.getCurrentUserEmail();

        log.info("Fetching products for user: {}", email);

        Page<Product> products = productService.getProductsByOwner(email, page, size);

        if (products == null || products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/getProductById/{id}")
    public ResponseEntity<Product> getById(@PathVariable("id") Integer id) {

        log.debug("Fetching product by id: {}", id);

        Product product = productService.getById(id);

        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable("id") Integer id,
                                          @RequestBody Product product) {

        log.debug("Updating product id: {}", id);

        Product updated = productService.update(id, product);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/deleteProductById/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id) {

        log.debug("Deleting product id: {}", id);

        boolean deleted = productService.delete(id);

        if (deleted) {
            return ResponseEntity.ok(AppConstants.PRODUCT_DELETED);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(AppConstants.PRODUCT_NOT_FOUND);
    }

    @GetMapping("/getAllOrders")
    public ResponseEntity<List<Order>> allOrders() {

        List<Order> orders = orderService.getAllOrders();

        if (orders == null || orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(orders);
    }
}