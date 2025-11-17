package com.community.catalog.brand.application.mediator;

public interface Mediator {
    <R> R send(Object request);
}
