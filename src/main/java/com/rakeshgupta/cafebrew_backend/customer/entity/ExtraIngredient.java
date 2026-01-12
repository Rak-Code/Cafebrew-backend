package com.rakeshgupta.cafebrew_backend.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an extra ingredient that can be added to menu items.
 * Extra ingredients are mapped to categories, so they appear as add-on options
 * for all items within those categories.
 */
@Entity
@Table(name = "extra_ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"categories"})
public class ExtraIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToMany
    @JoinTable(
        name = "extra_ingredient_categories",
        joinColumns = @JoinColumn(name = "extra_ingredient_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @ToString.Exclude
    @JsonIgnore
    private Set<Category> categories = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ExtraIngredient(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = true;
    }

    public ExtraIngredient(String name, String description, BigDecimal price, Boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
    }

    /**
     * Adds a category mapping to this extra ingredient.
     */
    public void addCategory(Category category) {
        categories.add(category);
        category.getExtraIngredients().add(this);
    }

    /**
     * Removes a category mapping from this extra ingredient.
     */
    public void removeCategory(Category category) {
        categories.remove(category);
        category.getExtraIngredients().remove(this);
    }

    /**
     * Clears all category mappings from this extra ingredient.
     */
    public void clearCategories() {
        for (Category category : new HashSet<>(categories)) {
            removeCategory(category);
        }
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
