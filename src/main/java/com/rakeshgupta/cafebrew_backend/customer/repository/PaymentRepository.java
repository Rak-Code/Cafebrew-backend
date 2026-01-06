package com.rakeshgupta.cafebrew_backend.customer.repository;

import com.rakeshgupta.cafebrew_backend.customer.entity.Order;
import com.rakeshgupta.cafebrew_backend.customer.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by Razorpay order ID for webhook processing
     * Critical for linking Razorpay events to internal orders
     */
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    
    /**
     * Find payment by order for updating Razorpay order ID
     */
    Optional<Payment> findByOrder(Order order);
}