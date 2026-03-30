package com.example.demo.serviceImpl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.Order;
import com.example.demo.Entity.OrderItem;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.constants.AppConstants;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exception.OrderException;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductValidationService;

import jakarta.transaction.Transactional;

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

	@Autowired
	private ProductValidationService productValidationService;

	@Transactional
	@Override
	public Order placeOrder(String email, String shippingAddress) {

		validateShippingAddress(shippingAddress);

		User user = getUserByEmail(email);

		List<Cart> cartItems = cartRepository.findByUserId(user.getId());

		if (cartItems == null || cartItems.isEmpty()) {
			throw new OrderException(AppConstants.CART_EMPTY);
		}

		// ✅ SECURITY CHECK: validate ownership
		validateCartOwnership(cartItems, user);

		Order order = createOrder(user, shippingAddress);
		Order savedOrder = orderRepository.save(order);

		double totalPrice = processCartItems(cartItems, savedOrder);

		savedOrder.setTotalPrice(totalPrice);
		orderRepository.save(savedOrder);

		cartRepository.deleteAll(cartItems);

		return savedOrder;
	}

	private void validateCartOwnership(List<Cart> cartItems, User user) {

		for (Cart cart : cartItems) {

			if (cart.getUser() == null || !cart.getUser().getId().equals(user.getId())) {
				throw new OrderException(AppConstants.UNAUTHORIZED_PRODUCT_ACCESS);
			}

			if (cart.getProduct() == null || cart.getProduct().getId() == null) {
				throw new OrderException(AppConstants.PRODUCT_NOT_FOUND);
			}

			Product product = productRepository.findById(cart.getProduct().getId())
					.orElseThrow(() -> new OrderException(AppConstants.PRODUCT_NOT_FOUND));

			if (product.isDeleted()) {
				throw new OrderException("Product is no longer available");
			}

		}
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

	// ================= HELPER METHODS =================
	private double processCartItems(List<Cart> cartItems, Order order) {

	    double totalPrice = 0;

	    for (Cart cart : cartItems) {

	        // ✅ Validate user ownership
	        if (cart.getUser() == null ||
	            !cart.getUser().getId().equals(order.getUser().getId())) {

	            throw new OrderException(AppConstants.UNAUTHORIZED_PRODUCT_ACCESS);
	        }

	        // ✅ Null check for product in cart
	        if (cart.getProduct() == null || cart.getProduct().getId() == null) {
	            throw new OrderException(AppConstants.PRODUCT_NOT_FOUND);
	        }

	        // ✅ Fetch latest product from DB
	        Product product = productRepository.findById(cart.getProduct().getId())
	                .orElseThrow(() -> new OrderException(AppConstants.PRODUCT_NOT_FOUND));

	        // ❗ Check if product is deleted
	        if (product.isDeleted()) {
	            throw new OrderException("Product is no longer available");
	        }

	        // ✅ Stock validation
	        productValidationService.validateStock(product, cart.getQuantity());

	        // ✅ Calculate total
	        totalPrice += cart.getTotalPrice();

	        // ✅ Reduce stock
	        reduceStock(product, cart.getQuantity());

	        // ✅ Save order item
	        saveOrderItem(order, cart, product);
	    }

	    return totalPrice;
	}

	private void validateShippingAddress(String address) {
		if (address == null || address.trim().length() < 10 || address.length() > 200) {
			throw new OrderException(AppConstants.INVALID_ADDRESS);
		}
	}

	private User getUserByEmail(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new OrderException(AppConstants.USER_NOT_FOUND));
	}

	private Product getProductById(Integer productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> new OrderException(AppConstants.PRODUCT_NOT_FOUND + productId));
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
		order.setPaymentStatus(PaymentStatus.PENDING);
//        order.setStatus(AppConstants.ORDER_CREATED);
		order.setCreatedDate(new Date());

		return order;
	}
}