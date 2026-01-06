package com.rakeshgupta.cafebrew_backend.customer.service;

import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import com.rakeshgupta.cafebrew_backend.customer.entity.Payment;
import com.rakeshgupta.cafebrew_backend.customer.repository.PaymentRepository;
import com.rakeshgupta.cafebrew_backend.customer.repository.OrderRepository;
import com.rakeshgupta.cafebrew_backend.common.dto.PaymentWebhookRequest;
import com.rakeshgupta.cafebrew_backend.common.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    
    /**
     * Creates Razorpay order.
     * Returns razorpay_order_id.
     */
    @Transactional
    public String createOnlinePayment(Order order) {
        // TODO: Integrate with actual Razorpay API
        // RazorpayOrder rpOrder = razorpayClient.createOrder(order.getTotalAmount(), order.getOrderCode());
        String razorpayOrderId = "rzp_order_" + System.currentTimeMillis();
        
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalStateException("Payment not found"));
        
        payment.setRazorpayOrderId(razorpayOrderId);
        paymentRepository.save(payment);
        
        return razorpayOrderId;
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
        
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new IllegalStateException("Payment not found"));
        
        Order order = payment.getOrder();
        
        if ("captured".equalsIgnoreCase(status)) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setRazorpayPaymentId(razorpayPaymentId);
            order.setPaymentStatus(PaymentStatus.PAID);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);
        }
        
        paymentRepository.save(payment);
        orderRepository.save(order);
    }
}