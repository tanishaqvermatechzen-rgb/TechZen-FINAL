import pg from 'pg';

const { Pool } = pg;

const connectionString = "postgresql://postgres:Taswnama%40.3@db.fwgcmdmxughkhddpirbb.supabase.co:5432/postgres";
const pool = new Pool({
  connectionString,
  ssl: { rejectUnauthorized: false }
});

async function fixHours() {
  const client = await pool.connect();
  try {
    await client.query("UPDATE metrics SET hours_coded = 0.0;");
    console.log("✅ Updated hours_coded to 0.0 in Supabase PostgreSQL.");
  } catch (err) {
    console.error("Error fixing hours:", err);
  } finally {
    client.release();
    await pool.end();
  }
}

fixHours();
