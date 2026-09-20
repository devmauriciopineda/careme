## ADDED Requirements

### Requirement: End the consultation from the chat surface

The interface MUST let the user end the consultation in progress from the chat surface and MUST reflect that the consultation has been closed. Once a consultation is closed the interface MUST NOT offer to continue it and MUST let the user start a new one. The interface MUST show the close outcome reported by the backend — the facts that were registered, or that no facts were registered — distinctly enough for the user to tell a close that registered facts from one that did not, and from a close that failed. The interface MUST NOT expose the consultation identifier, its summary, or its motive.

#### Scenario: End the consultation

- **WHEN** the user ends the consultation in progress
- **THEN** the interface sends the close request for the current conversation
- **AND** renders the Spanish close outcome reported by the backend
- **AND** exposes a busy state while the request is pending, without creating a second request

#### Scenario: Show the closed consultation

- **WHEN** the backend reports that the consultation was closed
- **THEN** the interface shows that the consultation is closed
- **AND** does not offer to continue it
- **AND** lets the user start a new consultation

#### Scenario: Close a consultation that collected no clinical facts

- **WHEN** the close outcome reports that no facts were registered
- **THEN** the interface shows that outcome distinctly from an error

#### Scenario: Keep the consultation internals out of the surface

- **WHEN** the interface renders any consultation state
- **THEN** it does not show the consultation identifier, the summary, or the motive
- **AND** it does not display technical error details
