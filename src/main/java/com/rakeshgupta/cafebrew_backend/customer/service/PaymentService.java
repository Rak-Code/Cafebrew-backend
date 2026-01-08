package com.rakeshgupta.cafebrew_backend.customer.service;

import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import com.rakeshgupta.cafebrew_backend.customer.entity.Payment;
import com.rakeshgupta.cafebrew_backend.customer.repository.PaymentRepository;
import com.rakeshgupta.cafebrew_backend.customer.repository.OrderRepository;
import com.rakeshgupta.cafebrew_backend.common.dto.PaymentWebhookRequest;
import com.rakeshgupta.cafebrew_backend.common.enums.PaymentStatus;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RazorpayClient razorpayClient;
    private final String razorpayKeySecret;
    
    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            @Value("${razorpay.key.id}") String razorpayKeyId,
            @Value("${razorpay.key.secret}") String razorpayKeySecret
    ) throws RazorpayException {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.razorpayKeySecret = razorpayKeySecret;
        this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }
    
    /**
     * Creates Razorpay order using actual Razorpay API.
     * Returns razorpay_order_id.
     */
    @Transactional
    public String createOnlinePayment(Order order) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", order.getTotalAmount().multiply(new java.math.BigDecimal(100)).intValue()); // Convert to paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", order.getOrderCode());
            
            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");
            
            log.info("Created Razorpay order: {} for order: {}", razorpayOrderId, order.getOrderCode());
            
            Payment payment = paymentRepository.findByOrder(order)
                    .orElseThrow(() -> new IllegalStateException("Payment not found"));
            
            payment.setRazorpayOrderId(razorpayOrderId);
            paymentRepository.save(payment);
            
            return razorpayOrderId;
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for order: {}", order.getOrderCode(), e);
            throw new RuntimeException("Failed to create payment order: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifies Razorpay webhook signature.
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = bytesToHex(hash);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Failed to verify webhook signature", e);
            return false;
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Webhook handler is the SINGLE source of truth.
     */
    @Transactional
    public void handleWebhook(PaymentWebhookRequest payload) {
        
        PaymentWebhookRequest.EntityData entity = payload.getPayload().getPayment().getEntity();
        String razorpayOrderId = entity.getOrderId();
        String razorpayPaymentId = entity.getId();
        String status = entity.getStatus();
        
        log.info("Processing webhook for Razorpay order: {}, status: {}", razorpayOrderId, status);
        
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new IllegalStateException("Payment not found for Razorpay order: " + razorpayOrderId));
        
        Order order = payment.getOrder();
        
        if ("captured".equalsIgnoreCase(status)) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setRazorpayPaymentId(razorpayPaymentId);
            order.setPaymentStatus(PaymentStatus.PAID);
            log.info("Payment successful for order: {}", order.getOrderCode());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);
            log.warn("Payment failed for order: {}, status: {}", order.getOrderCode(), status);
        }
        
        paymentRepository.save(payment);
        orderRepository.save(order);
    }
}
