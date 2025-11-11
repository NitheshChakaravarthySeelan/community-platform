package com.community.catalog.productwrite.application.mediator;

public interface Mediator {
    <R, T> R send(T command);
}
