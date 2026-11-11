package com.petconnect.auth.application.usecases;

import com.petconnect.auth.domain.exceptions.AuthException;
import com.petconnect.auth.domain.repositories.AuthUserRepository;
import com.petconnect.auth.domain.valueobjects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class RequestEmailChangeUseCase {

    private static final long TOKEN_TTL_HOURS = 24;

    private final AuthUserRepository authUserRepository;
    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String fromAddress;
    private final long tokenTtlHours;
    private final SecureRandom secureRandom = new SecureRandom();

    public RequestEmailChangeUseCase(
            AuthUserRepository authUserRepository,
            JavaMailSender mailSender,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.mail.from}") String fromAddress,
            @Value("${app.auth.email-change-token-ttl-hours:24}") long tokenTtlHours) {
        this.authUserRepository = authUserRepository;
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.fromAddress = fromAddress;
        this.tokenTtlHours = tokenTtlHours > 0 ? tokenTtlHours : TOKEN_TTL_HOURS;
    }

    @Transactional
    public void execute(UUID userId, String requestedEmail) {
        var newEmail = new Email(requestedEmail).value();
        var user = authUserRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        if (user.getEmail().equalsIgnoreCase(newEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new email is already in use");
        }
        if (authUserRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The email is already in use");
        }

        var token = generateToken();
        var tokenHash = EmailChangeTokenHasher.hash(token);
        user.requestEmailChange(newEmail, tokenHash, LocalDateTime.now().plusHours(tokenTtlHours));
        authUserRepository.save(user);

        var confirmationUrl = frontendUrl.replaceAll("/+$", "")
                + "/auth/confirm-email?token=" + token;
        var message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(newEmail);
        message.setSubject("Confirma el cambio de correo de Petconnect");
        message.setText("Se ha solicitado cambiar el correo de tu cuenta de Petconnect. "
                + "Confirma la nueva dirección desde este enlace:\n\n"
                + confirmationUrl
                + "\n\nEl enlace caduca en " + tokenTtlHours
                + " horas. Si no solicitaste este cambio, puedes ignorar este mensaje.");
        mailSender.send(message);
    }

    private String generateToken() {
        var bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
