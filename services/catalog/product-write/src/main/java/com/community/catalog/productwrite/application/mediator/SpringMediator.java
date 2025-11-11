package com.community.catalog.productwrite.application.mediator;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
public class SpringMediator implements Mediator {

    private final ApplicationContext applicationContext;

    public SpringMediator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <R, T> R send(T command) {
        try {
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                Object bean = applicationContext.getBean(beanName);
                Method handleMethod = findHandleMethod(bean.getClass(), command.getClass());
                if (handleMethod != null) {
                    return (R) handleMethod.invoke(bean, command);
                }
            }
            throw new IllegalStateException("No handler found for command: " + command.getClass().getName());
        } catch (Exception e) {
            // Unwrap invocation target exceptions to get the real business exception
            if (e.getCause() != null && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException("Error dispatching command", e);
        }
    }

    private Method findHandleMethod(Class<?> handlerClass, Class<?> commandClass) {
        return Arrays.stream(handlerClass.getMethods())
                .filter(method -> method.getName().equals("handle") && method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(commandClass))
                .findFirst()
                .orElse(null);
    }
}
