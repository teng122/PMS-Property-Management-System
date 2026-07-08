import { NextRequest } from "next/server";
import { loginViaGateway } from "@/lib/api/server";

export async function POST(request: NextRequest) {
  const body = await request.json();
  return loginViaGateway(body);
}
