package com.community.catalog.category.application.mediator;

import com.community.catalog.category.application.handlers.CommandHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

@Component
public class SpringMediator implements Mediator {

    private final ApplicationContext applicationContext;

    public SpringMediator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R send(Object request) {
        CommandHandler<R, Object> handler = resolveHandler(request);
        return handler.handle(request);
    }

    private <R> CommandHandler<R, Object> resolveHandler(Object request) {
        ResolvableType commandType = ResolvableType.forClass(request.getClass());
        ResolvableType handlerType =
                ResolvableType.forClassWithGenerics(
                        CommandHandler.class, ResolvableType.forClass(Object.class), commandType);

        String[] beanNames = applicationContext.getBeanNamesForType(handlerType);
        if (beanNames.length == 0) {
            throw new IllegalStateException(
                    "No handler found for command: " + request.getClass().getName());
        }
        if (beanNames.length > 1) {
            throw new IllegalStateException(
                    "Multiple handlers found for command: " + request.getClass().getName());
        }

        @SuppressWarnings("unchecked")
        CommandHandler<R, Object> handlerBean =
                (CommandHandler<R, Object>) applicationContext.getBean(beanNames[0]);
        return handlerBean;
    }
}
