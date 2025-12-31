package com.community.orders.refund.application.service;

import com.community.orders.refund.domain.model.Refund;
import com.community.orders.refund.domain.repository.RefundRepository;
import com.community.orders.refund.interfaces.dto.RefundRequest;
import com.community.orders.refund.interfaces.dto.RefundResponse;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {

  private final RefundRepository refundRepository;
  private final KafkaTemplate<String, RefundResponse> kafkaTemplate;
  private final Random random = new Random();

  @Transactional
  public RefundResponse processRefund(RefundRequest request) {
    // Simulate refund processing
    boolean refundSuccessful = random.nextBoolean(); // Simulate success/failure randomly

    String status = refundSuccessful ? "COMPLETED" : "FAILED";
    UUID transactionId = refundSuccessful ? UUID.randomUUID() : null; // Link to payment transaction if successful

    Refund refund = Refund.builder()
        .orderId(request.getOrderId())
        .userId(request.getUserId())
        .amount(request.getAmount())
        .reason(request.getReason())
        .status(status)
        .refundDate(Instant.now())
        .transactionId(transactionId)
        .build();

    Refund savedRefund = refundRepository.save(refund);

    // Create the event object
    RefundResponse refundResponse = mapToResponse(savedRefund);

    if ("COMPLETED".equals(refundResponse.getStatus())) {
      kafkaTemplate.send("refund.event.completed", refundResponse);
      System.out.println("Published RefundCompleted event for Order ID: " + refundResponse.getOrderId());
    } else {
      kafkaTemplate.send("refund.event.failed", refundResponse);
      System.out.println("published RefundFailed event for Order ID: " + refundResponse.getOrderId());
    }
  }

  @Transactional(readOnly = true)
  public RefundResponse getRefundByOrderId(UUID orderId) {
    Refund refund = refundRepository.findByOrderId(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Refund not found for order ID: " + orderId));
    return mapToResponse(refund);
  }

  private RefundResponse mapToResponse(Refund refund) {
    return new RefundResponse(
        refund.getRefundId(),
        refund.getOrderId(),
        refund.getUserId(),
        refund.getAmount(),
        refund.getStatus(),
        refund.getReason(),
        refund.getRefundDate(),
        refund.getTransactionId());
  }
}
