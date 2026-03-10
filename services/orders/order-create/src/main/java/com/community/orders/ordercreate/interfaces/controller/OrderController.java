package com.community.orders.ordercreate.interfaces.controller;

import com.community.orders.ordercreate.application.dto.OrderDTO;
import com.community.orders.ordercreate.application.service.OrderService;
import com.community.platform.shared.proto.order.CreateOrderCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderCommand command) {
        OrderDTO createdOrder = orderService.createOrder(command);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }
}
