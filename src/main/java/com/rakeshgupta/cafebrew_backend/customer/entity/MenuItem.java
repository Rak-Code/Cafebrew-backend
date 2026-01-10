package com.rakeshgupta.cafebrew_backend.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    /**
     * @deprecated Use categoryEntity instead. Kept for backward compatibility during migration.
     */
    @Deprecated
    @Column(name = "category")
    private String category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category categoryEntity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Boolean available = true;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * @deprecated Use constructor with Category entity instead.
     */
    @Deprecated
    public MenuItem(String name, String description, String category, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
    }
    
    /**
     * @deprecated Use constructor with Category entity instead.
     */
    @Deprecated
    public MenuItem(String name, String description, String category, BigDecimal price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
    }
    
    /**
     * Constructor with Category entity reference.
     */
    public MenuItem(String name, String description, Category categoryEntity, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.categoryEntity = categoryEntity;
        this.category = categoryEntity != null ? categoryEntity.getName() : null;
        this.price = price;
    }
    
    /**
     * Constructor with Category entity reference and image URL.
     */
    public MenuItem(String name, String description, Category categoryEntity, BigDecimal price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.categoryEntity = categoryEntity;
        this.category = categoryEntity != null ? categoryEntity.getName() : null;
        this.price = price;
        this.imageUrl = imageUrl;
    }
    
    /**
     * Gets the category name from the Category entity if available, 
     * otherwise falls back to the legacy category string field.
     */
    public String getCategoryName() {
        if (categoryEntity != null) {
            return categoryEntity.getName();
        }
        return category;
    }
    
    /**
     * Gets the category ID from the Category entity if available.
     */
    public Long getCategoryId() {
        return categoryEntity != null ? categoryEntity.getId() : null;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}