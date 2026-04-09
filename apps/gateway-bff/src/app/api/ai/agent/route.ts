import { NextRequest, NextResponse } from "next/server";
import { agentClient } from "@/lib/grpc/agent.client";

export async function POST(req: NextRequest) {
  try {
    const { query } = await req.json();

    if (!query) {
      return NextResponse.json({ error: "Query is required" }, { status: 400 });
    }

    const userId = req.headers.get("X-User-ID") || "anonymous";
    const grpcStream = agentClient.executeWorkflow(query, userId);

    const encoder = new TextEncoder();
    const stream = new ReadableStream({
      start(controller) {
        grpcStream.on("data", (data) => {
          controller.enqueue(encoder.encode(JSON.stringify(data) + "\n"));
        });

        grpcStream.on("end", () => {
          controller.close();
        });

        grpcStream.on("error", (err) => {
          controller.error(err);
        });
      },
      cancel() {
        grpcStream.cancel();
      },
    });

    return new Response(stream, {
      headers: {
        "Content-Type": "application/x-ndjson",
        "Cache-Control": "no-cache",
        Connection: "keep-alive",
      },
    });
  } catch (error: any) {
    console.error("Agent workflow error:", error);
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
