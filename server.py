from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import os
import sqlite3
from typing import Literal, Optional

app = FastAPI(
    title="Creator Gig Marketplace API",
    description="Standard REST API for Code2Career Hackathon 2026",
    version="1.0.0"
)

# Enable CORS for frontend integration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

DB_PATH = os.getenv("DATABASE_PATH", "database.db")

def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS gigs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            creator_name TEXT NOT NULL,
            title TEXT NOT NULL,
            category TEXT NOT NULL,
            rate REAL NOT NULL,
            description TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
    """)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS bookings (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            gig_id INTEGER NOT NULL,
            client_name TEXT NOT NULL,
            client_email TEXT NOT NULL,
            requirements TEXT NOT NULL,
            status TEXT DEFAULT 'Pending',
            rejection_reason TEXT,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (gig_id) REFERENCES gigs(id)
        )
    """)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS saved_gigs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            client_email TEXT NOT NULL,
            gig_id INTEGER NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(client_email, gig_id),
            FOREIGN KEY (gig_id) REFERENCES gigs(id)
        )
    """)
    # Existing demo databases predate timestamps. Add the columns without
    # discarding a creator's listings or client requests.
    gig_columns = {row["name"] for row in cursor.execute("PRAGMA table_info(gigs)")}
    booking_columns = {row["name"] for row in cursor.execute("PRAGMA table_info(bookings)")}
    if "created_at" not in gig_columns:
        cursor.execute("ALTER TABLE gigs ADD COLUMN created_at TEXT")
        cursor.execute("UPDATE gigs SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL")
    if "created_at" not in booking_columns:
        cursor.execute("ALTER TABLE bookings ADD COLUMN created_at TEXT")
        cursor.execute("UPDATE bookings SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL")
    if "updated_at" not in booking_columns:
        cursor.execute("ALTER TABLE bookings ADD COLUMN updated_at TEXT")
        cursor.execute("UPDATE bookings SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL")

    cursor.execute("CREATE INDEX IF NOT EXISTS idx_gigs_category ON gigs(category)")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_bookings_gig_id ON bookings(gig_id)")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_bookings_client_email ON bookings(client_email)")
    cursor.execute("CREATE INDEX IF NOT EXISTS idx_saved_gigs_client_email ON saved_gigs(client_email)")
    conn.commit()

    # Seed sample gigs if empty so marketplace is never blank on initial launch
    cursor.execute("SELECT COUNT(*) as count FROM gigs")
    if cursor.fetchone()["count"] == 0:
        seed_sample_gigs(cursor)
        conn.commit()

    conn.close()

def seed_sample_gigs(cursor):
    sample_gigs = [
        (
            "Alex Rivera",
            "High-Converting TikTok & Reels UGC Video Ads",
            "Video & UGC",
            95.0,
            "Authentic, engaging user-generated content filmed in 4K. Includes hook ideation, caption scripts, and licensed trending audio."
        ),
        (
            "Maya Chen",
            "Viral YouTube Thumbnails & Complete Branding Kit",
            "Design & Graphics",
            45.0,
            "Custom 3D-rendered facial expressions, high-contrast typography, and CTR-tested layout guaranteed to lift your impressions."
        ),
        (
            "Dev Patel",
            "Custom AI Automation Workflow (Zapier / Make / OpenAI)",
            "Tech & AI",
            120.0,
            "Automate customer lead capture, email nurturing, and AI summarization without touching complex code. Includes 3 revisions."
        ),
        (
            "Sarah Jenkins",
            "SEO-Optimized Tech & Founder Newsletters",
            "Writing & Translation",
            60.0,
            "Deeply researched, entertaining newsletters tailored for Substack and Beehiiv readers. Boosts open rates with killer subject lines."
        ),
        (
            "Marcus Brody",
            "Podcast Audio Cleanup & Multi-Platform Social Clips",
            "Video & UGC",
            80.0,
            "Turn 1 hour raw audio/video into crystal-clear masters plus 5 vertical shorts with animated subtitles and sound effects."
        ),
        (
            "Elena Rostova",
            "Full-Stack MVP Landing Page in Streamlit / FastAPI",
            "Tech & AI",
            150.0,
            "Rapid interactive prototype deployed to cloud in 48 hours. Clean modern UI, responsive controls, and documented backend API."
        )
    ]
    cursor.executemany(
        "INSERT INTO gigs (creator_name, title, category, rate, description) VALUES (?, ?, ?, ?, ?)",
        sample_gigs
    )

# Run database setup on module load
init_db()

# --- Pydantic Models ---
class GigCreate(BaseModel):
    creator_name: str = Field(..., min_length=1)
    title: str = Field(..., min_length=1)
    category: str = Field(..., min_length=1)
    rate: float = Field(..., gt=0)
    description: str = Field(..., min_length=1)

class BookingCreate(BaseModel):
    gig_id: int
    client_name: str = Field(..., min_length=1)
    client_email: str = Field(..., min_length=3)
    requirements: str = Field(..., min_length=1)

class BookingStatusUpdate(BaseModel):
    status: Literal["Accepted", "Declined"]
    rejection_reason: Optional[str] = None

class GigUpdate(BaseModel):
    title: Optional[str] = Field(default=None, min_length=1)
    category: Optional[str] = Field(default=None, min_length=1)
    rate: Optional[float] = Field(default=None, gt=0)
    description: Optional[str] = Field(default=None, min_length=1)

class SavedGigCreate(BaseModel):
    gig_id: int
    client_email: str = Field(..., min_length=3)

# --- API Endpoints ---
@app.get("/")
def root():
    return {
        "status": "Marketplace API operational",
        "service": "Creator Gig Marketplace",
        "version": "1.0.0",
        "hackathon": "Code2Career 2026"
    }

@app.get("/api/stats")
def get_stats():
    """Returns platform live metrics for the interactive dashboard header."""
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) as count FROM gigs")
    total_gigs = cursor.fetchone()["count"]

    cursor.execute("SELECT COUNT(*) as count FROM bookings")
    total_bookings = cursor.fetchone()["count"]

    cursor.execute("SELECT COUNT(*) as count FROM bookings WHERE status = 'Pending'")
    pending_bookings = cursor.fetchone()["count"]

    cursor.execute("SELECT COUNT(*) as count FROM bookings WHERE status = 'Accepted'")
    accepted_bookings = cursor.fetchone()["count"]

    cursor.execute("""
        SELECT COALESCE(SUM(g.rate), 0) as total_volume
        FROM bookings b
        JOIN gigs g ON b.gig_id = g.id
        WHERE b.status = 'Accepted'
    """)
    total_volume = cursor.fetchone()["total_volume"]
    conn.close()

    return {
        "total_gigs": total_gigs,
        "total_bookings": total_bookings,
        "pending_bookings": pending_bookings,
        "accepted_bookings": accepted_bookings,
        "total_volume": round(total_volume, 2)
    }

@app.post("/api/gigs", status_code=201)
def post_gig(gig: GigCreate):
    """Feature 1: Creators list a service."""
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute(
        "INSERT INTO gigs (creator_name, title, category, rate, description, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
        (gig.creator_name.strip(), gig.title.strip(), gig.category.strip(), gig.rate, gig.description.strip())
    )
    conn.commit()
    gig_id = cursor.lastrowid
    conn.close()
    return {"id": gig_id, "message": "Gig posted successfully"}

@app.get("/api/gigs")
def get_gigs(
    category: Optional[str] = None,
    search: Optional[str] = None,
    creator_name: Optional[str] = None,
    min_rate: Optional[float] = Query(default=None, ge=0),
    max_rate: Optional[float] = Query(default=None, gt=0),
    sort_by: Optional[str] = Query("newest", pattern="^(newest|cheapest|priciest)$"),
    limit: int = Query(default=24, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
):
    """
    Feature 2: Browse & Search.
    DP3 Discovery: Supports hybrid recency ranking (newest), price ranking (cheapest/priciest).
    """
    conn = get_db()
    cursor = conn.cursor()
    query = "SELECT * FROM gigs WHERE 1=1"
    params = []

    if category and category != "All":
        query += " AND category = ?"
        params.append(category)

    if search:
        query += " AND (title LIKE ? OR description LIKE ? OR creator_name LIKE ?)"
        wildcard = f"%{search.strip()}%"
        params.extend([wildcard, wildcard, wildcard])

    if creator_name and creator_name.strip():
        query += " AND LOWER(creator_name) = LOWER(?)"
        params.append(creator_name.strip())
    if min_rate is not None:
        query += " AND rate >= ?"
        params.append(min_rate)
    if max_rate is not None:
        query += " AND rate <= ?"
        params.append(max_rate)

    # DP3 Ranking Strategy
    if sort_by == "cheapest":
        query += " ORDER BY rate ASC, id DESC"
    elif sort_by == "priciest":
        query += " ORDER BY rate DESC, id DESC"
    else:
        # Default: newest first. This gives new creators a fair discovery path.
        query += " ORDER BY datetime(created_at) DESC, id DESC"

    cursor.execute(query, params)
    gigs = [dict(row) for row in cursor.fetchall()]
    conn.close()
    return gigs

@app.get("/api/gigs/{gig_id}")
def get_gig(gig_id: int):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM gigs WHERE id = ?", (gig_id,))
    row = cursor.fetchone()
    conn.close()
    if not row:
        raise HTTPException(status_code=404, detail=f"Gig with ID {gig_id} not found")
    return dict(row)

@app.patch("/api/gigs/{gig_id}")
def update_gig(gig_id: int, update: GigUpdate):
    """Update an existing creator listing without creating a duplicate gig."""
    fields = update.model_dump(exclude_none=True)
    if not fields:
        raise HTTPException(status_code=400, detail="Provide at least one field to update.")

    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM gigs WHERE id = ?", (gig_id,))
    if not cursor.fetchone():
        conn.close()
        raise HTTPException(status_code=404, detail=f"Gig with ID {gig_id} not found")

    assignments = ", ".join(f"{column} = ?" for column in fields)
    values = [value.strip() if isinstance(value, str) else value for value in fields.values()]
    cursor.execute(f"UPDATE gigs SET {assignments} WHERE id = ?", [*values, gig_id])
    conn.commit()
    conn.close()
    return {"message": "Gig updated successfully", "id": gig_id}

@app.delete("/api/gigs/{gig_id}")
def delete_gig(gig_id: int):
    """Remove an unbooked gig; booked gigs are retained to protect order history."""
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM gigs WHERE id = ?", (gig_id,))
    if not cursor.fetchone():
        conn.close()
        raise HTTPException(status_code=404, detail=f"Gig with ID {gig_id} not found")
    cursor.execute("SELECT COUNT(*) AS count FROM bookings WHERE gig_id = ?", (gig_id,))
    if cursor.fetchone()["count"]:
        conn.close()
        raise HTTPException(status_code=409, detail="Booked gigs cannot be deleted because their order history is retained.")
    cursor.execute("DELETE FROM saved_gigs WHERE gig_id = ?", (gig_id,))
    cursor.execute("DELETE FROM gigs WHERE id = ?", (gig_id,))
    conn.commit()
    conn.close()
    return {"message": "Gig deleted successfully"}

@app.post("/api/bookings", status_code=201)
def book_gig(booking: BookingCreate):
    """
    Feature 3: Book a gig.
    Validates gig existence (HTTP 404 if invalid).
    DP2 Double Booking: Allowed to submit new bookings even if another booking is Pending.
    """
    conn = get_db()
    cursor = conn.cursor()

    # Gig validation: ensure gig actually exists
    cursor.execute("SELECT id FROM gigs WHERE id = ?", (booking.gig_id,))
    if not cursor.fetchone():
        conn.close()
        raise HTTPException(status_code=404, detail=f"Gig ID {booking.gig_id} does not exist.")

    cursor.execute(
        """INSERT INTO bookings
           (gig_id, client_name, client_email, requirements, status, created_at, updated_at)
           VALUES (?, ?, ?, ?, 'Pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)""",
        (booking.gig_id, booking.client_name.strip(), booking.client_email.strip().lower(), booking.requirements.strip())
    )
    conn.commit()
    booking_id = cursor.lastrowid
    conn.close()
    return {
        "id": booking_id,
        "status": "Pending",
        "message": "Booking request submitted successfully"
    }

@app.get("/api/creator/bookings")
def get_creator_bookings(
    creator_name: Optional[str] = None,
    limit: int = Query(default=24, ge=1, le=100),
    offset: int = Query(default=0, ge=0),
):
    """Feature 4: Creator views incoming bookings."""
    conn = get_db()
    cursor = conn.cursor()
    query = """
        SELECT b.*, g.title as gig_title, g.creator_name, g.category, g.rate 
        FROM bookings b 
        JOIN gigs g ON b.gig_id = g.id 
    """
    params = []
    if creator_name and creator_name.strip():
        query += " WHERE LOWER(g.creator_name) = LOWER(?)"
        params.append(creator_name.strip())
    query += " ORDER BY datetime(b.created_at) DESC, b.id DESC"
    query += " LIMIT ? OFFSET ?"
    cursor.execute(query, [*params, limit, offset])
    bookings = [dict(row) for row in cursor.fetchall()]
    conn.close()
    return bookings

@app.patch("/api/bookings/{booking_id}")
def update_booking_status(booking_id: int, update: BookingStatusUpdate):
    """
    Feature 4 & DP1 Rejection:
    Creator accepts or declines booking with optional rejection reason.
    """
    conn = get_db()
    cursor = conn.cursor()

    cursor.execute("SELECT status FROM bookings WHERE id = ?", (booking_id,))
    existing_booking = cursor.fetchone()
    if not existing_booking:
        conn.close()
        raise HTTPException(status_code=404, detail=f"Booking ID {booking_id} not found.")

    if existing_booking["status"] != "Pending":
        conn.close()
        raise HTTPException(status_code=409, detail="Only pending bookings can be updated.")

    reason = update.rejection_reason.strip() if update.rejection_reason else None
    if update.status == "Accepted":
        reason = None

    cursor.execute(
        "UPDATE bookings SET status = ?, rejection_reason = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
        (update.status, reason, booking_id)
    )
    conn.commit()
    conn.close()
    return {"message": f"Booking status updated to {update.status}", "status": update.status}

@app.get("/api/client/bookings")
def get_client_bookings(client_name: Optional[str] = None, client_email: Optional[str] = None):
    """
    Feature 5: Client views their bookings and status (Pending, Accepted, Declined).
    """
    conn = get_db()
    cursor = conn.cursor()
    query = """
        SELECT b.*, g.title as gig_title, g.creator_name, g.rate, g.category 
        FROM bookings b 
        JOIN gigs g ON b.gig_id = g.id 
    """
    params = []
    if client_email and client_email.strip():
        query += " WHERE LOWER(b.client_email) = LOWER(?)"
        params.append(client_email.strip())
    elif client_name:
        query += " WHERE LOWER(b.client_name) LIKE LOWER(?)"
        params.append(f"%{client_name.strip()}%")
    query += " ORDER BY b.id DESC"

    cursor.execute(query, params)
    bookings = [dict(row) for row in cursor.fetchall()]
    conn.close()
    return bookings

@app.post("/api/seed")
def reset_seed():
    """Utility endpoint to re-seed demo data if needed."""
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM bookings")
    cursor.execute("DELETE FROM gigs")
    seed_sample_gigs(cursor)
    conn.commit()
    conn.close()
    return {"message": "Sample database successfully reset and seeded"}
