package com.community.users.userservice.application.command;

public interface CommandHandler<C, R> {
    /**
     * Handles a command and returns a result.
     *
     * @param command The command to handle.
     * @return The result of the command handling.
     */
    R handle(C command);
}
