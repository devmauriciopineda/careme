import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Emits .next/standalone so the Docker image ships only what it needs.
  output: "standalone",
  experimental: {
    serverActions: {
      // A measurements file may weigh up to 10 MB, and the default limit is 1 MB.
      bodySizeLimit: "11mb",
    },
  },
};

export default nextConfig;
