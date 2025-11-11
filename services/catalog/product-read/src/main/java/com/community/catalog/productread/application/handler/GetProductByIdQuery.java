package com.community.catalog.productread.application.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetProductByIdQuery {
    private final Long productId;
}
