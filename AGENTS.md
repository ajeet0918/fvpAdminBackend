# Backend API - Agent Instructions

## What this repo is
Central Spring Boot API used by both the public website and admin panel.
Contains business logic, authentication, data access, integrations, and workflows.

## Stack
- Java 21, Spring Boot 3.x
- PostgreSQL via Spring Data JPA
- Cashfree payment integration
- Document handling with local filesystem and AWS S3
- No Lombok - plain Java only

## Build and test commands
- Build: `mvn clean install`
- Run tests: `mvn test`
- Run one test class: `mvn test -Dtest=ClassName`
- Run app locally: `mvn spring-boot:run`
- Always run `mvn test` before finishing any task

## Package structure
- `com.agriplatform.backend.auth` - admin auth/login and token issuance
- `com.agriplatform.backend.security` - JWT filters, security chain wiring, auth guards
- `com.agriplatform.backend.customer` - customer signup/login, profile, addresses, customer account operations
- `com.agriplatform.backend.product` - product catalog and product CRUD support
- `com.agriplatform.backend.category` - product categories and category management
- `com.agriplatform.backend.order` - order creation, status workflow, pricing snapshots, history
- `com.agriplatform.backend.payment` - Cashfree session creation, webhook validation, payment events
- `com.agriplatform.backend.lead` - lead capture and lead lifecycle management
- `com.agriplatform.backend.inquiry` - inquiry capture, investor/farmer/collection-hub intake flow
- `com.agriplatform.backend.investor` - investor account, returns, payouts, receipts
- `com.agriplatform.backend.user` - admin users, roles, assignable owners, user lifecycle
- `com.agriplatform.backend.document` - upload/download, metadata, storage-provider abstraction
- `com.agriplatform.backend.portal` - OTP/auth challenge and portal support models
- `com.agriplatform.backend.common` - shared API handling and common helpers
- `com.agriplatform.backend.config` - Spring configuration, CORS, static resources, seeders

## Code conventions
- No Lombok - use explicit constructors/getters/setters
- No unnecessary abstractions - introduce interface only with 2+ real implementations
- Minimal-change policy - only touch required code paths
- Prefer guard clauses and early returns
- User Logger for better understanding the logs with slf4j
- Keep controllers thin and services business-logic heavy
- Repositories should remain plain Spring Data JPA unless strong reason exists

## Authentication rules
- Separate admin and customer token handling must remain isolated
- Customer endpoints use customer JWT filter and customer auth context
- Admin endpoints use admin JWT filter with role checks
- Public endpoints must be explicitly whitelisted
- Never trust frontend-calculated totals or payment state

## Payment rules (Cashfree)
- Backend creates payment session and returns session details to frontend
- Never expose Cashfree secret on frontend
- Webhook signature verification is mandatory when enforce flag is enabled
- Payment status transitions must come from webhook or server-to-server confirmation
- Log every payment event with stable order reference and amount context

## Database rules
- Schema changes must go through migration scripts
- Do not use `spring.jpa.hibernate.ddl-auto=update` in production
- Keep `created_at` and `updated_at` on core mutable entities
- Use DB-level unique constraints for business keys
- Avoid hard deletes for customer-facing business records

## Security rules
- Never log passwords, raw tokens, gateway secrets, or sensitive PII
- Validate inputs with Bean Validation and `@Valid`
- Public high-risk endpoints should be rate-limited
- Validate file size and MIME type server-side for uploads
- Secrets must come from config/env, never hardcoded in source

## Workflow and background job rules
- Never rely on `SecurityContext` in scheduled/async execution paths
- Keep workflow logic isolated from request-context assumptions
- Background tasks must log start, completion, and failure with debug context

## Error handling
- Keep API error response format consistent:
  - `{ "error": "code", "message": "human readable" }`
- Use proper status mapping:
  - 400 validation errors
  - 401 authentication failures
  - 403 authorization failures
  - 404 resource not found
  - 500 unexpected errors
- Never expose stack traces/internal class names in public responses

## PR rules
- PR title format: `[FEATURE|FIX|REFACTOR|MIGRATION] Short description`
- DB changes must include migration script and impact notes
- New endpoints must include method/path/auth/request/response docs in PR description
- `mvn test` must pass before merge
