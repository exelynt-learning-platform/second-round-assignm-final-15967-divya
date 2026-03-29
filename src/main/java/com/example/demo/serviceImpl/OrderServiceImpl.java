package com.example.demo.serviceImpl;

import org.springframework.stereotype.Service;

import com.example.demo.service.OrderService;

import java.util.Date;
import java.util.List;
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
import com.example.demo.service.CartService;

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

	@Override
	public Order placeOrder(String email, String shippingAddress) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		List<Cart> cartItems = cartRepository.findByUserId(user.getId());

		if (cartItems.isEmpty()) {
			throw new RuntimeException("Cart is empty");
		}

		double totalPrice = 0;

		// Create Order first
		Order order = new Order();
		order.setUserId(user.getId());
		order.setShippingAddress(shippingAddress);
		order.setPaymentStatus("PENDING");
		order.setStatus("CREATED");
		order.setCreatedDate(new Date());

		Order savedOrder = orderRepository.save(order);

		// Create Order Items
		for (Cart cart : cartItems) {

			Product product = productRepository.findById(cart.getProductId())
					.orElseThrow(() -> new RuntimeException("Product not found"));

			if (product.getStockQuantity() < cart.getQuantity()) {
				throw new RuntimeException("Insufficient stock for product: " + product.getName());
			}

			totalPrice += cart.getTotalPrice();

			// reduce stock
			product.setStockQuantity(product.getStockQuantity() - cart.getQuantity());
			productRepository.save(product);

			// Save OrderItem
			OrderItem item = new OrderItem();
			item.setOrderId(savedOrder.getId());
			item.setProductId(product.getId());
			item.setProductName(product.getName());
			item.setQuantity(cart.getQuantity());
			item.setPrice(product.getPrice());

			orderItemRepository.save(item);
		}

		savedOrder.setTotalPrice(totalPrice);
		orderRepository.save(savedOrder);

		// clear cart
		cartRepository.deleteAll(cartItems);

		return savedOrder;
	}

	@Override
	public List<Order> getMyOrders(String email) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		return orderRepository.findByUserId(user.getId());
	}

	@Override
	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}
}