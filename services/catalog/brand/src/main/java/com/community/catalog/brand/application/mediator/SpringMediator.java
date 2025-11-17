package com.community.catalog.brand.application.mediator;

import com.community.catalog.brand.application.handlers.CommandHandler;
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
                        CommandHandler.class,
                        ResolvableType.forClass(
                                Object.class), // We use Object for the return type and cast later
                        commandType);

        String[] beanNames = applicationContext.getBeanNamesForType(handlerType);
        if (beanNames.length == 0) {
            throw new IllegalStateException(
                    "No handler found for command: " + request.getClass().getName());
        }
        if (beanNames.length > 1) {
            throw new IllegalStateException(
                    "Multiple handlers found for command: " + request.getClass().getName());
        }

        // The type system can't fully resolve the generics here, so we suppress the warning.
        // The logic ensures that we are getting the correct handler for the specific command type.
        @SuppressWarnings("unchecked")
        CommandHandler<R, Object> handlerBean =
                (CommandHandler<R, Object>) applicationContext.getBean(beanNames[0]);
        return handlerBean;
    }
}
