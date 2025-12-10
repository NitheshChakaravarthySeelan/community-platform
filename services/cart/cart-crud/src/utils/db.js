import { Pool } from "pg";
import dotenv from "dotenv";
dotenv.config(); // Load environment variables here
let pool = null;
const getPool = () => {
  if (pool) {
    return pool;
  }
  pool = new Pool({
    user: process.env.DB_USER,
    host: process.env.DB_HOST,
    database: process.env.DB_DATABASE,
    password: process.env.DB_PASSWORD,
    port: parseInt(process.env.DB_PORT || "5432", 10), // Convert port to number, default to 5432
  });
  return pool;
};
export const dbPool = getPool();
//# sourceMappingURL=db.js.map
