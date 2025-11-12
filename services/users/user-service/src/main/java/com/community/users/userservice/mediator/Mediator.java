package com.community.users.userservice.mediator;

public interface Mediator {
    /**
     * Sends a command to its appropriate handler and returns the result.
     *
     * @param <R> The type of the response.
     * @param <T> The type of the command.
     * @param command The command to send.
     * @return The response from the command handler.
     */
    <R, T> R send(T command);
}
