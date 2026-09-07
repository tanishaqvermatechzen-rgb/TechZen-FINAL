import pg from 'pg';

const { Pool } = pg;

const connectionString = "postgresql://postgres:Taswnama%40.3@db.fwgcmdmxughkhddpirbb.supabase.co:5432/postgres";
const pool = new Pool({
  connectionString,
  ssl: { rejectUnauthorized: false }
});

async function resetStreak() {
  const client = await pool.connect();
  try {
    await client.query("UPDATE user_profile SET streak_days = 0, commits_today = 0;");
    console.log("✅ Reset user streak_days to 0 in Supabase PostgreSQL.");
  } catch (err) {
    console.error("Error resetting streak:", err);
  } finally {
    client.release();
    await pool.end();
  }
}

resetStreak();
