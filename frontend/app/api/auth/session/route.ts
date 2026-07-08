import { NextResponse } from "next/server";
import { getSessionFromCookies } from "@/lib/auth/cookies";

export async function GET() {
  return NextResponse.json({ user: await getSessionFromCookies() });
}
