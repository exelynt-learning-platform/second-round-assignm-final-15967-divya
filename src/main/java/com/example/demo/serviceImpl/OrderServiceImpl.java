package com.example.demo.serviceImpl;

import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.Order;
import com.example.demo.Entity.OrderItem;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.exception.OrderException;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    @Override
    public Order placeOrder(String email, String shippingAddress) {

        validateShippingAddress(shippingAddress);

        User user = getUserByEmail(email);

        List<Cart> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new OrderException("Cart is empty");
        }

        Order order = createOrder(user, shippingAddress);
        Order savedOrder = orderRepository.save(order);

        double totalPrice = 0;

        for (Cart cart : cartItems) {

            Product product = getProductById(cart.getProduct().getId());

            validateStock(product, cart.getQuantity());

            totalPrice += cart.getTotalPrice();

            reduceStock(product, cart.getQuantity());

            saveOrderItem(savedOrder, cart, product);
        }

        savedOrder.setTotalPrice(totalPrice);
        orderRepository.save(savedOrder);

        cartRepository.deleteAll(cartItems);

        return savedOrder;
    }


    private void validateShippingAddress(String address) {
        if (address == null || address.trim().length() < 10 || address.length() > 200) {
            throw new OrderException("Invalid shipping address (10–200 characters required)");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new OrderException("User not found"));
    }

    private Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new OrderException("Product not found with id: " + productId));
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new OrderException("Insufficient stock for product: " + product.getName());
        }
    }

    private void reduceStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    private void saveOrderItem(Order order, Cart cart, Product product) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setQuantity(cart.getQuantity());
        item.setPrice(product.getPrice());

        orderItemRepository.save(item);
    }

    private Order createOrder(User user, String shippingAddress) {
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPaymentStatus("PENDING");
        order.setStatus("CREATED");
        order.setCreatedDate(new Date());
        return order;
    }


    @Override
    public List<Order> getMyOrders(String email) {
        User user = getUserByEmail(email);
        return orderRepository.findByUserId(user.getId());
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}