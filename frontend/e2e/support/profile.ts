import { execFileSync } from "node:child_process";
import { existsSync, readFileSync } from "node:fs";
import { join } from "node:path";

/**
 * The directory the backend uses as the Markdown source of truth for the current patient profile.
 *
 * Two ways to reach it, because the backend can run in two ways:
 *
 * - `CAREME_PATIENT_PROFILE_DIRECTORY` on the host, which is how a locally run backend is checked
 *   (`README.md`, *Run the end-to-end tests*).
 * - `CAREME_PATIENT_PROFILE_CONTAINER` naming a running backend container, because compose keeps
 *   the documents inside a named volume a host path cannot point at, and the profile is then read
 *   through the container instead.
 */
export function profileDirectory(): string {
  const directory = process.env.CAREME_PATIENT_PROFILE_DIRECTORY;
  if (!directory) {
    throw new Error(
      "CAREME_PATIENT_PROFILE_DIRECTORY must point at the backend's patient-profile directory so " +
        "the scenarios can check the profile, or CAREME_PATIENT_PROFILE_CONTAINER must name the " +
        "backend container that holds it",
    );
  }
  return directory;
}

const CONTAINER_PROFILE_PATH = "/app/data/patient/profile.md";

/** The whole profile document, or null when the profile has not been published yet. */
export function profileDocument(): string | null {
  const container = process.env.CAREME_PATIENT_PROFILE_CONTAINER;
  if (container) {
    return readFromContainer(container);
  }
  const path = join(profileDirectory(), "profile.md");
  return existsSync(path) ? readFileSync(path, "utf8") : null;
}

/** Reads the document from inside the backend container, treating "no file" as absent. */
function readFromContainer(container: string): string | null {
  try {
    return execFileSync(
      "podman",
      ["exec", container, "sh", "-c", `test -f ${CONTAINER_PROFILE_PATH} && cat ${CONTAINER_PROFILE_PATH}`],
      { encoding: "utf8", stdio: ["ignore", "pipe", "ignore"] },
    );
  } catch {
    return null;
  }
}

/**
 * The current value of one profile field, read from the document's front matter, or null when the
 * field is absent. Absence is part of the contract, so it is reported and not defaulted.
 */
export function profileValue(document: string | null, key: string): string | null {
  if (!document) {
    return null;
  }
  const frontMatter = document.split("\n---\n", 1)[0];
  for (const line of frontMatter.replace(/^---\n/, "").split("\n")) {
    const separator = line.indexOf(":");
    if (separator > 0 && line.slice(0, separator).trim() === key) {
      const raw = line.slice(separator + 1).trim();
      if (raw.length >= 2 && raw.startsWith('"') && raw.endsWith('"')) {
        return raw.slice(1, -1);
      }
      return raw.length === 0 ? null : raw;
    }
  }
  return null;
}

/** The profile keys the document declares, so a scenario can assert what it holds. */
export function profileKeys(document: string | null): string[] {
  if (!document) {
    return [];
  }
  const frontMatter = document.split("\n---\n", 1)[0].replace(/^---\n/, "");
  return frontMatter
    .split("\n")
    .map((line) => line.slice(0, line.indexOf(":")).trim())
    .filter((key) => key.length > 0);
}
