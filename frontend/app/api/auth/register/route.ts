import { NextRequest } from "next/server";
import { registerViaGateway } from "@/lib/api/server";

export async function POST(request: NextRequest) {
  const body = await request.json();
  return registerViaGateway(body);
}
