package com.community.catalog.category.application.handlers;

public interface CommandHandler<R, C> {
    R handle(C command);
}
