package com.community.cart.snapshot.domain.repository;

import com.community.cart.snapshot.domain.model.UserCartSnapshot;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCartSnapshotRepository extends JpaRepository<UserCartSnapshot, UUID> {}
