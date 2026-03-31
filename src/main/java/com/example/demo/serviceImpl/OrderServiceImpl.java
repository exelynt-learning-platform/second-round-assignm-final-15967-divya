package com.example.demo.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.Order;
import com.example.demo.Entity.OrderItem;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.common.PaginationUtil;
import com.example.demo.config.CartConfig;
import com.example.demo.config.PaginationConfig;
import com.example.demo.constants.AppConstants;
import com.example.demo.enums.OrderSortField;
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
	@Autowired
	private CartConfig cartConfig;

	@Autowired
	private PaginationConfig paginationConfig;

	@Transactional
	@Override
	public Order placeOrder(String email, String shippingAddress) {

		validateShippingAddress(shippingAddress);

		User user = getUserByEmail(email);

		List<Cart> cartItems = getCartItems(user);

		Order order = createOrder(user, shippingAddress);
		Order savedOrder = orderRepository.save(order);

		double totalPrice = processCartItems(cartItems, savedOrder);

		savedOrder.setTotalPrice(totalPrice);
		orderRepository.save(savedOrder);

		cartRepository.deleteAll(cartItems);

		return savedOrder;
	}

	@Override
	public List<Order> getMyOrders(String email) {
		if (email == null || email.isBlank()) {
			throw new OrderException(AppConstants.EMPTY_EMAIL);
		}

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new OrderException(AppConstants.USER_NOT_FOUND));

		List<Order> orders = orderRepository.findByUserId(user.getId());

		if (orders == null) {
			return new ArrayList<>();
		}
		return orders;
	}

	@Override
	public Page<Order> getAllOrders(int page, int size, String sortBy, String sortDir) {

		Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, sortDir, paginationConfig.getDefaultPage(),
				paginationConfig.getDefaultSize(), OrderSortField::from);
		Page<Order> orders = orderRepository.findAll(pageable);

		return orders;
	}

	// ================= HELPER METHODS =================
	private double processCartItems(List<Cart> cartItems, Order order) {

		double totalPrice = 0;

		for (Cart cart : cartItems) {
			validateCartOwnership(cart, order);

			Product product = getValidProduct(cart);

			productValidationService.validateStock(product, cart.getQuantity());

			totalPrice += calculateItemTotal(cart);

			updateStock(product, cart.getQuantity());

			createOrderItem(order, cart, product);
		}

		return totalPrice;
	}

	private void validateCartOwnership(Cart cart, Order order) {
		if (cart.getUser() == null || !Objects.equals(cart.getUser().getId(), order.getUser().getId())) {
			throw new OrderException(AppConstants.UNAUTHORIZED_PRODUCT_ACCESS);
		}
	}

	private Product getValidProduct(Cart cart) {
		Product product = productRepository.findById(cart.getProduct().getId())
				.orElseThrow(() -> new OrderException(AppConstants.PRODUCT_NOT_FOUND));

		if (product.isDeleted()) {
			throw new OrderException(AppConstants.PRODUCT_NOT_FOUND);
		}

		return product;
	}

	private double calculateItemTotal(Cart cart) {
		return cart.getTotalPrice();
	}

	private void updateStock(Product product, int quantity) {
		product.setStockQuantity(product.getStockQuantity() - quantity);
		productRepository.save(product);
	}

	private void createOrderItem(Order order, Cart cart, Product product) {
		OrderItem item = new OrderItem();
		item.setOrder(order);
		item.setProduct(product);
		item.setProductName(product.getName());
		item.setQuantity(cart.getQuantity());
		item.setPrice(product.getPrice());

		orderItemRepository.save(item);
	}

	private List<Cart> getCartItems(User user) {
		List<Cart> cartItems = cartRepository.findByUserId(user.getId());

		if (cartItems == null || cartItems.isEmpty()) {
			throw new OrderException(AppConstants.CART_EMPTY);
		}

		return cartItems;
	}

	private void validateShippingAddress(String shippingAddress) {

		if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
			throw new OrderException(AppConstants.ADDRESS_EMPTY);
		}

		int length = shippingAddress.trim().length();

		if (length < cartConfig.getMinAddressLength()) {
			throw new OrderException(AppConstants.ADDRESS_TOO_SHORT);
		}

		if (length > cartConfig.getMaxAddressLength()) {
			throw new OrderException(AppConstants.ADDRESS_TOO_LONG);
		}
	}

	private User getUserByEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new OrderException(AppConstants.EMPTY_EMAIL);
		}

		return userRepository.findByEmail(email).orElseThrow(() -> new OrderException(AppConstants.USER_NOT_FOUND));
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