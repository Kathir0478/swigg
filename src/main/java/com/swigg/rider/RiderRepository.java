package com.swigg.rider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiderRepository extends JpaRepository<Rider, UUID> {
    Optional<Rider> findByUserId(UUID userId);
    Optional<Rider> findById(UUID riderId);
    Optional<Rider> findByUser_PhoneNumber(String phoneNumber);
    Optional<Rider> findByUser_UserName(String userName);
    List<Rider> findAll();
}
