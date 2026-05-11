package com.community.orders.ordercreate.domain.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="order_item")
public class OrderItem {
    @Id
    private UUID id;
    @Id
    private UUID productId;
    private String productName;
    private int quantity;
    private int priceAtTime;
    
}