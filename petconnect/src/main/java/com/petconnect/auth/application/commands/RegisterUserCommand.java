package com.petconnect.auth.application.commands;

import com.petconnect.auth.domain.valueobjects.Email;

public record RegisterUserCommand(
                Email email,
                String password,
                String firstName,
                String lastName,
                String profileType) {
}
