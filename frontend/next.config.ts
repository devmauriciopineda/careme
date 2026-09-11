import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Emits .next/standalone so the Docker image ships only what it needs.
  output: "standalone",
};

export default nextConfig;
