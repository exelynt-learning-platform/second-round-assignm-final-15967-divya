package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Order;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exception.OrderException;
import com.example.demo.repository.OrderRepository;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class PaymentService {

	@Autowired
	private OrderRepository orderRepository;

	@Value("${app.frontend.success-url}")
	private String successUrl;

	@Value("${app.frontend.cancel-url}")
	private String cancelUrl;

	  @Value("${payment.currency}")
	    private String currency;
	
	public String createPaymentSession(Integer orderId) throws Exception {

		Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderException("Order not found"));

		if (order.getTotalPrice() <= 0) {
			throw new OrderException("Invalid order amount");
		}

		SessionCreateParams params = SessionCreateParams.builder().setMode(SessionCreateParams.Mode.PAYMENT)
				.setSuccessUrl(successUrl + orderId).setCancelUrl(
						cancelUrl + orderId)
				.addLineItem(
						SessionCreateParams.LineItem.builder().setQuantity(1L)
								.setPriceData(
										SessionCreateParams.LineItem.PriceData.builder().setCurrency(currency)
												.setUnitAmount((long) (order.getTotalPrice() * 100))
												.setProductData(SessionCreateParams.LineItem.PriceData.ProductData
														.builder().setName("Order Payment").build())
												.build())
								.build())
				.build();

		Session session = Session.create(params);
		return session.getUrl();
	}

	public void success(Integer orderId) {

		Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderException("Order not found"));

		order.setPaymentStatus(PaymentStatus.PAID);
		orderRepository.save(order);
	}

	public void failure(Integer orderId) {

		Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderException("Order not found"));

		order.setPaymentStatus(PaymentStatus.FAILED);
		orderRepository.save(order);
	}
}