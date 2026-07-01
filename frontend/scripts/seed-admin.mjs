// Seed tài khoản demo cho 4 vai trò qua API Gateway (identity-service).
// Chạy khi backend đã bật:  node scripts/seed-admin.mjs
// Base URL: đối số 1 hoặc env API_URL, mặc định http://localhost:8080

const BASE = process.argv[2] || process.env.API_URL || "http://localhost:8080";
const REGISTER = `${BASE}/identity-service/api/auth/register`;

const USERS = [
  { username: "admin",     password: "admin123",     fullName: "Quản trị viên", email: "admin@hotel.local",     role: "ADMIN" },
  { username: "reception", password: "reception123", fullName: "Nhân viên Lễ tân", email: "reception@hotel.local", role: "RECEPTIONIST" },
  { username: "cleaner",   password: "cleaner123",   fullName: "Nhân viên Lao công", email: "cleaner@hotel.local",  role: "HOUSEKEEPER" },
  { username: "customer",  password: "customer123",  fullName: "Khách hàng Demo", email: "customer@hotel.local", role: "CUSTOMER" },
];

async function seed(u) {
  try {
    const res = await fetch(REGISTER, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(u),
    });
    if (res.ok) {
      console.log(`✅ Tạo thành công: ${u.username} / ${u.password}  (${u.role})`);
    } else {
      const text = await res.text();
      if (res.status === 400 && text.includes("tồn tại")) {
        console.log(`ℹ️  Đã tồn tại: ${u.username} (bỏ qua)`);
      } else {
        console.log(`⚠️  Lỗi ${res.status} với ${u.username}: ${text}`);
      }
    }
  } catch (err) {
    console.error(`❌ Không kết nối được ${REGISTER} — backend đã chạy chưa?\n   ${err.message}`);
    process.exitCode = 1;
  }
}

console.log(`Seeding users tới: ${REGISTER}\n`);
for (const u of USERS) {
  // eslint-disable-next-line no-await-in-loop
  await seed(u);
}
console.log(`\nXong. Đăng nhập admin: username="admin", password="admin123".`);
