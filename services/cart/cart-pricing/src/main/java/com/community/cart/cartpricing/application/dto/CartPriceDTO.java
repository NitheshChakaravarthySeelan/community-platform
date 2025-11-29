package com.community.cart.cartpricing.application.dto;

import lombok.Data;
import lombok.Builder;

public class CartPriceDTO {
  private Long id;
  private Long cartId;
  private Long totalPrice;
}
