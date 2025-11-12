package com.community.users.userservice.mediator;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public final class SpringMediator implements Mediator {

    /** The Spring application context. */
    private final ApplicationContext applicationContext;

    /**
     * Constructs a new SpringMediator.
     *
     * @param theApplicationContext The Spring application context.
     */
    public SpringMediator(final ApplicationContext theApplicationContext) {
        this.applicationContext = theApplicationContext;
    }

    @Override
    public <R, T> R send(final T command) {
        try {
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                Object bean = applicationContext.getBean(beanName);
                Method handleMethod = findHandleMethod(bean.getClass(), command.getClass());
                if (handleMethod != null) {
                    return (R) handleMethod.invoke(bean, command);
                }
            }
            throw new IllegalStateException(
                    "No handler found for command: " + command.getClass().getName());
        } catch (final Exception e) {
            // Unwrap invocation target exceptions
            if (e.getCause() != null && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException("Error dispatching command", e);
        }
    }

    /**
     * Finds the handle method in a given handler.
     *
     * @param handlerClass The class of the handler.
     * @param commandClass The class of the command.
     * @return The handle method, or null if not found.
     */
    private Method findHandleMethod(final Class<?> handlerClass, final Class<?> commandClass) {
        return Arrays.stream(handlerClass.getMethods())
                .filter(
                        method ->
                                method.getName().equals("handle")
                                        && method.getParameterCount() == 1
                                        && method.getParameterTypes()[0].equals(commandClass))
                .findFirst()
                .orElse(null);
    }
}
