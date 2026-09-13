/**
 * Base URL of the backend API.
 *
 * Deliberately not prefixed with `NEXT_PUBLIC_`: the API is only ever reached
 * from the server, so the browser never needs this value. Nothing in the client
 * bundle may import this module.
 */
export const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";
