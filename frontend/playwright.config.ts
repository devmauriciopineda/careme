import { defineConfig, devices } from "@playwright/test";

/**
 * End-to-end configuration for the real interface. It does not start a web server on its own: the
 * run is expected to have the backend, PostgreSQL and the frontend already up, because the
 * scenarios talk to the real assistant and assert against the clinical history on disk.
 *
 * Two projects, because the two runs cost differently: `interface` drives the browser, while
 * `corpus` drives the chat API with the evaluation corpus
 * (`roadmap_asistente_historia_clinica.md` §4.11) and needs no browser at all.
 */
export default defineConfig({
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: 0,
  reporter: [["list"]],
  // A turn can take several provider round trips, so the budget is per turn and generous.
  timeout: 240_000,
  expect: { timeout: 240_000 },
  use: {
    baseURL: process.env.E2E_BASE_URL ?? "http://localhost:3000",
    trace: "retain-on-failure",
  },
  projects: [
    { name: "interface", testDir: "./e2e/playwright", use: { ...devices["Desktop Chrome"] } },
    { name: "corpus", testDir: "./e2e/corpus", use: { ...devices["Desktop Chrome"] } },
  ],
});
