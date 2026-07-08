package com.petconnect.auth.application.commands;

import com.petconnect.auth.domain.valueobjects.Email;

public record LoginCommand(
        Email email,
        String password) {
}