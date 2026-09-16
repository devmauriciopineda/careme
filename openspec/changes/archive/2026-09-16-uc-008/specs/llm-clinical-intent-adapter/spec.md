## MODIFIED Requirements

### Requirement: Compose grounded answers from retrieved events

The adapter MUST compose the Spanish answer from the retrieved clinical events it receives, MUST use only their content and temporal precision, MUST NOT introduce facts, dates, or interpretations absent from that set, and MUST report the references of the events the answer relies on.

The adapter MUST also report how much of the question the retrieved events support: the whole question, part of it, or none of it. When the retrieved set supports only part of the question, the adapter MUST answer the supported part and MUST report the part of the question it could not support. When the retrieved set does not support the question at all, the adapter MUST report that the events do not answer it, MUST NOT present any of them as support, and MUST NOT compose an answer from them. Deciding this is the adapter's job: the caller MUST NOT treat a question as answered merely because retrieval returned events.

#### Scenario: Compose from retrieved events
- **WHEN** the adapter receives a question and the set of clinical events retrieved for it
- **THEN** it returns a Spanish answer that uses only those events
- **AND** it reports the references of the events the answer relies on
- **AND** it reports that the retrieved events support the whole question

#### Scenario: Preserve temporal precision in the answer
- **WHEN** a retrieved event carries an approximate or unknown date
- **THEN** the composed answer keeps that precision and does not state an exact date

#### Scenario: No retrieved events
- **WHEN** the set of retrieved events is empty
- **THEN** the adapter reports that there is nothing to answer with
- **AND** it does not fall back to general knowledge or assumptions

#### Scenario: Retrieved events support only part of the question
- **WHEN** the retrieved events answer part of the question and leave another part unsupported
- **THEN** the adapter composes the answer for the supported part
- **AND** it reports that the retrieved events support the question only in part
- **AND** it reports the part of the question that the retrieved events do not support
- **AND** it does not fill the unsupported part with general knowledge, assumptions, or events outside the retrieved set

#### Scenario: Retrieved events do not support the question
- **WHEN** the retrieved events do not answer the question
- **THEN** the adapter reports that the retrieved events support none of the question
- **AND** it reports no references, because no event supports the answer
- **AND** it does not compose an answer from those events
- **AND** it does not fall back to general knowledge or assumptions
