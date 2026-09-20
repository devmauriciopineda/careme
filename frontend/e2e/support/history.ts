import { createHash } from "node:crypto";
import { readdirSync, readFileSync } from "node:fs";
import { join } from "node:path";

/**
 * The directory the backend uses as the Markdown source of truth for clinical events. The run sets
 * it explicitly, so a scenario can assert what a turn did — or did not do — to the history.
 */
export function eventsDirectory(): string {
  const directory = process.env.CAREME_EVENTS_DIRECTORY;
  if (!directory) {
    throw new Error(
      "CAREME_EVENTS_DIRECTORY must point at the backend's clinical-event directory so the " +
        "scenarios can check the history",
    );
  }
  return directory;
}

/**
 * A content-level snapshot of the clinical history. Comparing snapshots proves a turn left the
 * history exactly as it was, instead of only proving that no new document appeared.
 */
export function historySnapshot(): Record<string, string> {
  const snapshot: Record<string, string> = {};
  let names: string[];
  try {
    names = readdirSync(eventsDirectory()).filter((name) => name.endsWith(".md"));
  } catch {
    return snapshot;
  }
  for (const name of names.sort()) {
    const content = readFileSync(join(eventsDirectory(), name));
    snapshot[name] = createHash("sha256").update(content).digest("hex");
  }
  return snapshot;
}

/** Documents the history gained, comparing content and not only names. */
export function addedDocuments(
  before: Record<string, string>,
  after: Record<string, string>,
): string[] {
  return Object.keys(after).filter((name) => !(name in before));
}
