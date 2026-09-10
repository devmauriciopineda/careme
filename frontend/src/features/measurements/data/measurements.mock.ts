import type { Measurement } from "../types";

/** Number of daily records in the mock dataset (eight weeks). */
export const MOCK_MEASUREMENT_DAYS = 56;

const MILLISECONDS_PER_DAY = 86_400_000;
const RANDOM_SEED = 20_260_910;

const WEIGHT_START_KG = 84.6;
const WEIGHT_TOTAL_CHANGE_KG = -5.8;
const WEIGHT_DAILY_NOISE_KG = 0.7;

const WAIST_START_CM = 98.5;
const WAIST_TOTAL_CHANGE_CM = -6.4;
const WAIST_DAILY_NOISE_CM = 0.5;
/** Waist follows the weight noise so both metrics stay correlated. */
const WAIST_WEIGHT_COUPLING = 0.8;

/** Mulberry32: deterministic so the dataset is identical on every render. */
function createSeededRandom(seed: number): () => number {
    let state = seed;

    return () => {
        state = (state + 0x6d2b79f5) | 0;
        let value = Math.imul(state ^ (state >>> 15), 1 | state);
        value = (value + Math.imul(value ^ (value >>> 7), 61 | value)) ^ value;
        return ((value ^ (value >>> 14)) >>> 0) / 4_294_967_296;
    };
}

function roundToSingleDecimal(value: number): number {
    return Math.round(value * 10) / 10;
}

function toIsoDate(timestamp: number): string {
    return new Date(timestamp).toISOString().slice(0, 10);
}

/**
 * Generates eight weeks of daily records ending today, with a realistic
 * downward weight trend and a waist circumference that follows it.
 */
function createMockMeasurements(): Measurement[] {
    const random = createSeededRandom(RANDOM_SEED);
    const now = new Date();
    const todayUtc = Date.UTC(
        now.getUTCFullYear(),
        now.getUTCMonth(),
        now.getUTCDate()
    );
    const lastIndex = MOCK_MEASUREMENT_DAYS - 1;

    return Array.from({ length: MOCK_MEASUREMENT_DAYS }, (_, index) => {
        const progress = index / lastIndex;
        const weightNoise = (random() - 0.5) * WEIGHT_DAILY_NOISE_KG;
        const waistNoise = (random() - 0.5) * WAIST_DAILY_NOISE_CM;
        const date = toIsoDate(
            todayUtc - (lastIndex - index) * MILLISECONDS_PER_DAY
        );

        return {
            id: `measurement-${date}`,
            date,
            weightKg: roundToSingleDecimal(
                WEIGHT_START_KG + WEIGHT_TOTAL_CHANGE_KG * progress + weightNoise
            ),
            waistCm: roundToSingleDecimal(
                WAIST_START_CM +
                    WAIST_TOTAL_CHANGE_CM * progress +
                    waistNoise +
                    weightNoise * WAIST_WEIGHT_COUPLING
            ),
        };
    });
}

/** Mock dataset used until the measurements API is available. */
export const RAW_MEASUREMENTS: Measurement[] = createMockMeasurements();
