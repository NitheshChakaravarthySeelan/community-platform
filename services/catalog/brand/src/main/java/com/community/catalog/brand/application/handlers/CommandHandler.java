package com.community.catalog.brand.application.handlers;

public interface CommandHandler<R, C> {
    R handle(C command);
}
