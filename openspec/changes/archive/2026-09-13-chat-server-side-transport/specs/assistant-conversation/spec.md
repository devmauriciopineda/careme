## ADDED Requirements

### Requirement: Allow the declared origin to reach the operations a browser client uses

The API MUST accept a cross-origin preflight from every configured browser origin for the HTTP verbs those clients use, so a browser client is not rejected before its request is read. The API MUST keep every other verb closed for the same mapping.

#### Scenario: Accept a preflight for a browser write
- **WHEN** a browser client at a configured origin sends a preflight for a write operation it uses
- **THEN** the API answers it successfully
- **AND** the answer names that origin and that verb

#### Scenario: Keep unused verbs closed
- **WHEN** a browser client asks for a verb outside the exposed set
- **THEN** the API does not allow it
