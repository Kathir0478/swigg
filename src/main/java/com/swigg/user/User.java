package com.swigg.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    @Column(name = "userid",updatable = false,nullable = false)
    private UUID userId;

    @Column(name = "username",nullable = false)
    private String userName;

    @Column(name = "phonenumber", unique = true, nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "totp_secret", nullable = false)
    private String totpSecret;

    @Builder.Default
    @Column(name = "isactive")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "isverified")
    private Boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

}
