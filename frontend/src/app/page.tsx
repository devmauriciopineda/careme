import type { Metadata } from "next";

import { ChatWorkspace } from "@/features/chat/components/ChatWorkspace";
import { STRINGS } from "@/features/measurements/lib/strings";

export const metadata: Metadata = {
    title: STRINGS.app.title,
    description: STRINGS.app.description,
};

export default function Home() {
    return <ChatWorkspace />;
}
