package com.community.users.userservice.mediator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.community.users.userservice.application.command.CreateUserCommand;
import com.community.users.userservice.application.dto.UserResponseDTO;
import com.community.users.userservice.application.handler.CreateUserCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SpringMediatorTest {

    @Mock private ApplicationContext applicationContext;

    @Mock private CreateUserCommandHandler createUserCommandHandler;

    private SpringMediator springMediator;

    @BeforeEach
    void setUp() {
        springMediator = new SpringMediator(applicationContext);
    }

    @Test
    void send_shouldDispatchCommandToCorrectHandler() {
        CreateUserCommand command =
                new CreateUserCommand("mediatorTestUser", "mediator@example.com", "password");
        UserResponseDTO expectedResponse =
                UserResponseDTO.builder()
                        .userId(2L)
                        .userName("mediatorTestUser")
                        .email("mediator@example.com")
                        .build();

        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"createUserCommandHandler"});
        when(applicationContext.getBean("createUserCommandHandler")).thenReturn(createUserCommandHandler);
        when(createUserCommandHandler.handle(command)).thenReturn(expectedResponse);

        UserResponseDTO actualResponse = springMediator.send(command);

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void send_shouldThrowException_whenNoHandlerFound() {
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{});

        CreateUserCommand unknownCommand =
                new CreateUserCommand("unknown", "unknown@example.com", "pass");

        assertThatThrownBy(() -> springMediator.send(unknownCommand))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error dispatching command")
                .getCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No handler found for command");
    }
}
