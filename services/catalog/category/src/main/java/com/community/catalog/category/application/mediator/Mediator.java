package com.community.catalog.category.application.mediator;

public interface Mediator {
    <R> R send(Object request);
}
