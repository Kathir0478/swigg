package com.swigg.cart;

import com.swigg.food.Food;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_items_cartid", columnList = "cartid"),
        @Index(name = "idx_cart_items_foodid", columnList = "foodid"),
        @Index(name = "idx_cart_items_cartid_foodid", columnList = "cartid,foodid")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cartitemid")
    private UUID cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartid", nullable = false)
    private Cart cart;

    @Column(name = "cartid", insertable = false, updatable = false)
    private UUID cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foodid", nullable = false)
    private Food food;

    @Column(name = "foodid", insertable = false, updatable = false)
    private UUID foodId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @CreationTimestamp
    @Column(name = "createdat", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedat", nullable = false)
    private LocalDateTime updatedAt;
}
