/**
 * Base URL of the backend API.
 *
 * Deliberately not prefixed with `NEXT_PUBLIC_`: measurements are fetched on
 * the server, so the browser never needs this value.
 */
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? process.env.API_BASE_URL ?? "http://localhost:8080";
