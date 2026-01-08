package com.rakeshgupta.cafebrew_backend.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakeshgupta.cafebrew_backend.common.dto.PaymentWebhookRequest;
import com.rakeshgupta.cafebrew_backend.customer.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    /**
     * POST /api/payments/webhook
     * Razorpay webhook endpoint
     * Always return 200 OK to Razorpay if processed
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature
    ) {
        log.info("Received Razorpay webhook");
        
        // Verify signature if provided (production should always have this)
        if (signature != null && !signature.isEmpty()) {
            if (!paymentService.verifyWebhookSignature(rawPayload, signature)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.badRequest().build();
            }
        } else {
            log.warn("Webhook received without signature - consider enabling signature verification in production");
        }
        
        try {
            PaymentWebhookRequest payload = objectMapper.readValue(rawPayload, PaymentWebhookRequest.class);
            paymentService.handleWebhook(payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to process webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
