package com.rakeshgupta.cafebrew_backend.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Entity representing an extra ingredient added to an order item.
 * Stores a snapshot of the extra ingredient details at the time of order
 * to preserve historical accuracy even if the original ingredient changes.
 */
@Entity
@Table(name = "order_item_extras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"orderItem"})
public class OrderItemExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    @ToString.Exclude
    private OrderItem orderItem;

    @Column(name = "extra_ingredient_id", nullable = false)
    private Long extraIngredientId;

    @Column(name = "extra_ingredient_name", nullable = false, length = 100)
    private String extraIngredientName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Creates an OrderItemExtra from an ExtraIngredient.
     * This captures the ingredient's current name and price at order time.
     */
    public OrderItemExtra(OrderItem orderItem, ExtraIngredient extraIngredient) {
        this.orderItem = orderItem;
        this.extraIngredientId = extraIngredient.getId();
        this.extraIngredientName = extraIngredient.getName();
        this.price = extraIngredient.getPrice();
    }

    /**
     * Creates an OrderItemExtra with explicit values.
     */
    public OrderItemExtra(OrderItem orderItem, Long extraIngredientId, String extraIngredientName, BigDecimal price) {
        this.orderItem = orderItem;
        this.extraIngredientId = extraIngredientId;
        this.extraIngredientName = extraIngredientName;
        this.price = price;
    }
}
