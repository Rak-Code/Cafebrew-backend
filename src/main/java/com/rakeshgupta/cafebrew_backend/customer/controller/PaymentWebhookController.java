package com.rakeshgupta.cafebrew_backend.customer.controller;

import com.rakeshgupta.cafebrew_backend.common.dto.PaymentWebhookRequest;
import com.rakeshgupta.cafebrew_backend.customer.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    /**
     * POST /api/payments/webhook
     * Razorpay webhook endpoint
     * Always return 200 OK to Razorpay if processed
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody PaymentWebhookRequest payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        // TODO: Verify signature with RazorpaySignatureVerifier
        // signatureVerifier.verify(payload, signature);
        
        paymentService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
