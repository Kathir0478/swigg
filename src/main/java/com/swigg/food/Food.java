package com.swigg.food;

import com.swigg.restaurant.Restaurant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "foods", indexes = {
    @Index(name = "idx_foods_restaurantid", columnList = "restaurantid"),
    @Index(name = "idx_foods_category", columnList = "category"),
    @Index(name = "idx_foods_isactive", columnList = "isactive"),
    @Index(name = "idx_foods_isavailable", columnList = "isavailable"),
    @Index(name = "idx_foods_restaurantid_isactive", columnList = "restaurantid,isactive"),
    @Index(name = "idx_foods_restaurantid_isavailable", columnList = "restaurantid,isavailable")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "foodid", nullable = false, updatable = false)
    private UUID foodId;

    @NotBlank(message = "Food name cannot be blank")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @DecimalMin(value = "0.0", message = "Rating cannot be lower than 0")
    @DecimalMax(value = "5.0", message = "Rating cannot be higher than 5")
    @Column(name = "rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal rating = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 0, message = "Review count cannot be negative")
    @Column(name = "reviewcount", nullable = false)
    private Integer reviewCount = 0;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private FoodCategory category;

    @NotNull(message = "Restaurant ID is required")
    @Column(name = "restaurantid", nullable = false, updatable = false)
    private UUID restaurantId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurantid", referencedColumnName = "restaurantid", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_foods_restaurants"))
    private Restaurant restaurant;

    @Builder.Default
    @Column(name = "isavailable", nullable = false)
    private Boolean isAvailable = true;

    @Builder.Default
    @Column(name = "isactive", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}
