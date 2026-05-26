# AGENTS.md - Payment Module Rules

## Scope
Applies to `backend/src/main/java/com/agriplatform/backend/payment`.

## Critical Constraints
1. Never log API secrets, webhook signatures, or raw credentials.
2. Preserve idempotent behavior for webhook processing.
3. Never mark payment success without verified provider evidence.
4. Keep order state transitions auditable via status history/events.

## Cashfree Rules
1. Use SDK-based order/session creation path.
2. Keep config-driven environment selection (sandbox vs production).
3. Webhook verification must be explicit and configurable.
4. Fail safely: if gateway call fails, keep order intact and retryable.

## API/Model Rules
1. Keep payment response contract stable:
   - providerOrderId
   - paymentSessionId
   - paymentLink (optional)
2. Do not remove fields used by frontend fallback behavior.

## Error Handling
1. Capture provider error body safely for debugging.
2. Return user-safe messages to clients; keep detail in backend logs/history.
3. Never throw ambiguous runtime errors without context.

## Test Focus
1. Create session success path.
2. Create session failure/retry path.
3. Webhook success/failure status updates.
4. Signature verification enabled/disabled behavior.

