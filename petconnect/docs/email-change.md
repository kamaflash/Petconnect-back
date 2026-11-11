# Email address change

The authenticated settings flow keeps the current address active until the user confirms the new one. The backend stores only a SHA-256 hash of a random, single-use token; the token expires after 24 hours by default. Confirming the change marks the address verified and revokes the refresh session.

## SMTP configuration

Configure these environment variables for the backend deployment. Do not commit credentials:

| Variable | Default | Purpose |
|---|---|---|
| `SMTP_HOST` | `localhost` | SMTP server hostname |
| `SMTP_PORT` | `587` | SMTP server port |
| `SMTP_USERNAME` | empty | SMTP account |
| `SMTP_PASSWORD` | empty | SMTP credential |
| `SMTP_AUTH` | `true` | Enable SMTP authentication |
| `SMTP_STARTTLS` | `true` | Enable STARTTLS |
| `SMTP_FROM` | `no-reply@petconnect.local` | Sender address accepted by the SMTP provider |
| `FRONTEND_BASE_URL` | `http://localhost:4200` | Public frontend base URL used in verification links |
| `EMAIL_CHANGE_TOKEN_TTL_HOURS` | `24` | Verification-link lifetime |

Email changes will fail explicitly until a reachable SMTP server and a valid sender address are configured.
