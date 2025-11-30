package com.community.cart.snapshot.application.service;

import com.community.cart.snapshot.domain.model.UserCartSnapshot;
import com.community.cart.snapshot.domain.repository.UserCartSnapshotRepository;
import com.community.cart.snapshot.infrastructure.client.CartCrudAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SnapshotService {
  private final UserCartSnapshotRepository userCartSnapshotRepository;
  private final CartCrudAdapter cartCrudAdapter;

  @Transactional
  public UserCartSnapshot createSnapshot(Long userId) {
    // call the external url
    String liverCartItemsJson = cartCrudAdapter.getLiveCart(userId);

    // Validation
    if (liverCartItemsJson == null
        || liverCartItemsJson.isEmpty()
        || liverCartItemsJson.equals("[]")) {
      throw new IllegalStateException(
          "Cannot create a snapshot of an empty or non-existent cart.");
    }

    // Create a new snapshot entity.
    UserCartSnapshot snapshot = new UserCartSnapshot();
    snapshot.setUserId(userId);
    snapshot.setItems(liverCartItemsJson);

    // Save the immutable snapshot to the db.
    return userCartSnapshotRepository.save(snapshot);
  }
}
