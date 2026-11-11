# Blocked accounts

Authenticated users can manage blocked accounts through `GET /api/v1/users/blocked`, `POST /api/v1/users/blocked/{userId}`, and `DELETE /api/v1/users/blocked/{userId}`. IDs in these routes are authentication-user IDs, not profile IDs.

Blocking hides profiles and posts in feed/profile reads and prevents direct conversations or new direct messages in either direction. A user can unblock an account from Settings. The relation is persisted by Flyway migration `V35__add_blocked_users.sql`.

The app does not currently have a real mention/people-tag workflow or notification contract. A setting for who may mention or tag an account is intentionally omitted until that feature exists.
