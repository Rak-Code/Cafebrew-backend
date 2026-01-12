package com.rakeshgupta.cafebrew_backend.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"order", "extras"})
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;
    
    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;
    
    @Column(name = "menu_item_name", nullable = false)
    private String menuItemName;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OrderItemExtra> extras = new ArrayList<>();
    
    public OrderItem(Order order, Long menuItemId, String menuItemName, BigDecimal price, Integer quantity) {
        this.order = order;
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
        this.extras = new ArrayList<>();
    }

    /**
     * Adds an extra ingredient to this order item.
     */
    public void addExtra(OrderItemExtra extra) {
        extras.add(extra);
        extra.setOrderItem(this);
    }

    /**
     * Removes an extra ingredient from this order item.
     */
    public void removeExtra(OrderItemExtra extra) {
        extras.remove(extra);
        extra.setOrderItem(null);
    }

    /**
     * Calculates the total price including extras.
     * Formula: (basePrice + sum of extra prices) * quantity
     */
    public void calculateTotalPrice() {
        BigDecimal extrasTotal = extras.stream()
            .map(OrderItemExtra::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalPrice = price.add(extrasTotal).multiply(BigDecimal.valueOf(quantity));
    }
}