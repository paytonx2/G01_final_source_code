require("dotenv").config();
const express = require("express");
const mysql = require("mysql");
const multer = require("multer");
const path = require("path");
const bcrypt = require("bcryptjs"); // ใช้สำหรับการ Hash Password

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve static uploads folder for images
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

/* =======================
   MySQL Connection Pool
======================= */
const db = mysql.createPool({
  connectionLimit: 10,
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  timezone: '+07:00' // กำหนด Timezone ให้ตรงกับประเทศไทย (ICT)
});

/* =======================
   Promise Wrapper
======================= */
function query(sql, params = []) {
  return new Promise((resolve, reject) => {
    db.query(sql, params, (err, results) => {
      if (err) return reject(err);
      resolve(results);
    });
  });
}

/* =======================
   Multer Setup (Image Uploads)
======================= */
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, 'uploads/'); 
  },
  filename: function (req, file, cb) {
    cb(null, Date.now() + path.extname(file.originalname));
  }
});
const upload = multer({ storage: storage });

/* =======================
   Root
======================= */
app.get("/", (req, res) => {
  res.json({ message: "SMARTDORM API is running", system_time: new Date().toLocaleString() });
});

/* =======================
   1. AUTHENTICATION & USERS
======================= */

// GET /users?role=tenant
app.get("/users", async (req, res) => {
  try {
    const { role } = req.query;
    let usersQuery = "SELECT user_id, name, email, phone, role FROM users";
    let params = [];
    if (role) {
      usersQuery += " WHERE role = ?";
      params.push(role);
    }
    const users = await query(usersQuery, params);
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

// POST /register (With Password Hashing)
app.post("/register", async (req, res) => {
  try {
    const { name, email, password, phone, role } = req.body;
    if (!name || !email || !password) {
      return res.status(400).json({ error: true, message: "Missing required fields" });
    }

    // 1. Hash Password ก่อนบันทึก
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    const userRole = role || 'tenant';
    const result = await query(
      "INSERT INTO users (name, email, password, phone, role) VALUES (?, ?, ?, ?, ?)",
      [name, email, hashedPassword, phone, userRole]
    );
    res.status(201).json({ message: "User registered successfully", user_id: result.insertId });
  } catch (err) {
    if (err.code === 'ER_DUP_ENTRY') {
        return res.status(400).json({ error: true, message: "Email already exists" });
    }
    res.status(500).json({ error: true, message: err.message });
  }
});

// POST /login (Check Hashed Password)
app.post("/login", async (req, res) => {
  try {
    const { email, password } = req.body;
    const users = await query("SELECT * FROM users WHERE email = ?", [email]);
    
    if (users.length === 0) {
      return res.status(401).json({ error: true, message: "Invalid credentials" });
    }

    const user = users[0];
    
    // 2. ตรวจสอบ Password ที่รับมากับ Hash ใน DB
    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(401).json({ error: true, message: "Invalid credentials" });
    }

    res.json({ 
      message: "Login successful", 
      user: { user_id: user.user_id, name: user.name, role: user.role } 
    });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

/* =======================
   2. ROOMS & TENANTS
======================= */

app.get("/rooms", async (req, res) => {
  try {
    const rooms = await query("SELECT * FROM rooms");
    res.json(rooms);
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

app.post("/rooms", async (req, res) => {
  try {
    const { room_number, floor, price, description } = req.body;
    const result = await query(
      "INSERT INTO rooms (room_number, floor, price, description) VALUES (?, ?, ?, ?)",
      [room_number, floor, price, description]
    );
    res.status(201).json({ message: "Room added successfully", room_id: result.insertId });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

app.post("/rooms/assign-tenant", async (req, res) => {
  try {
    const { room_id, user_id, move_in_date, move_out_date } = req.body;

    const roomCheck = await query("SELECT status FROM rooms WHERE room_id = ?", [room_id]);
    if (roomCheck.length === 0) return res.status(404).json({ message: "Room not found" });
    if (roomCheck[0].status !== 'available') return res.status(400).json({ message: "Room is not available" });

    // บันทึกวันที่ (MySQL จะรับเป็น String จาก VARCHAR ที่คุณแก้)
    await query(
      "INSERT INTO room_tenants (room_id, user_id, move_in_date, move_out_date, status) VALUES (?, ?, ?, ?, 'active')",
      [room_id, user_id, move_in_date, move_out_date]
    );

    await query("UPDATE rooms SET status = 'occupied' WHERE room_id = ?", [room_id]);

    res.status(201).json({ message: "Tenant assigned successfully" });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

app.delete("/rooms/:id/tenant", async (req, res) => {
  try {
    const roomId = req.params.id;
    await query(
      "UPDATE room_tenants SET status = 'moved_out', move_out_date = CURDATE() WHERE room_id = ? AND status = 'active'",
      [roomId]
    );
    await query("UPDATE rooms SET status = 'available' WHERE room_id = ?", [roomId]);
    res.json({ message: "Tenant removed successfully" });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

/* =======================
   3. INVOICES & PAYMENTS
======================= */

app.post("/create-invoice", async (req, res) => {
  try {
    const { room_id, month, rent, water, electricity, other } = req.body;
    const total = parseFloat(rent) + parseFloat(water) + parseFloat(electricity) + parseFloat(other);

    const result = await query(
      "INSERT INTO invoices (room_id, month, rent, water, electricity, other, total) VALUES (?, ?, ?, ?, ?, ?, ?)",
      [room_id, month, rent, water, electricity, other, total]
    );
    res.status(201).json({ message: "Invoice created successfully", invoice_id: result.insertId });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

// 1. ดึงบิลของฉัน (สำหรับฝั่ง Tenant/ผู้เช่า) -> ตรงกับ @GET("myinvoice")
app.get("/myinvoice", async (req, res) => {
  try {
    const { user_id } = req.query;
    if (!user_id) {
      return res.status(400).json({ error: true, message: "user_id is required" });
    }

    // ไปหาว่า user คนนี้อยู่ห้องไหน (สถานะ active) แล้วดึงบิลของห้องนั้นมา
    const invoices = await query(`
      SELECT i.*, r.room_number 
      FROM invoices i
      JOIN room_tenants rt ON i.room_id = rt.room_id
      JOIN rooms r ON i.room_id = r.room_id
      WHERE rt.user_id = ? AND rt.status = 'active'
      ORDER BY i.invoice_id DESC
    `, [user_id]);

    res.json(invoices);
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

// 2. ดึงประวัติบิลของห้องนั้นๆ (สำหรับฝั่ง Admin) -> ตรงกับ @GET("invoice/{room_id}")
app.get("/invoice/:room_id", async (req, res) => {
  try {
    const roomId = req.params.room_id;
    const invoices = await query(`
      SELECT * FROM invoices 
      WHERE room_id = ? 
      ORDER BY invoice_id DESC
    `, [roomId]);
    
    res.json(invoices);
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

app.post("/upload-slip", upload.single('slip_image'), async (req, res) => {
  try {
    const { invoice_id, user_id, amount } = req.body;
    const slip_image = req.file ? req.file.filename : null;

    if (!slip_image) return res.status(400).json({ error: true, message: "Slip image is required" });

    await query(
      "INSERT INTO payments (invoice_id, user_id, amount, slip_image) VALUES (?, ?, ?, ?)",
      [invoice_id, user_id, amount, slip_image]
    );

    await query("UPDATE invoices SET status = 'pending' WHERE invoice_id = ?", [invoice_id]);
    res.status(201).json({ message: "Payment slip uploaded" });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

// ดึงรายการแจ้งโอนเงินทั้งหมดมาให้ Admin ตรวจสอบ
// ดึงรายการแจ้งโอนเงินทั้งหมดมาให้ Admin ตรวจสอบ
app.get("/admin/payments", async (req, res) => {
  try {
    const payments = await query(`
      SELECT 
        p.payment_id,
        p.invoice_id,
        p.user_id,
        p.amount,
        p.slip_image,
        p.status,
        p.created_at,
        u.name AS tenant_name,
        i.month,
        i.total AS invoice_total
      FROM payments p
      LEFT JOIN users u ON p.user_id = u.user_id
      LEFT JOIN invoices i ON p.invoice_id = i.invoice_id
      ORDER BY p.created_at DESC
    `);
    
    // ตรวจสอบข้อมูลก่อนส่งกลับไปที่แอป
    const formattedPayments = payments.map(payment => ({
      ...payment,
      tenant_name: payment.tenant_name || "Unknown Tenant",
      month: payment.month || "Unknown Month",
      invoice_total: payment.invoice_total || 0.0
    }));

    res.json(formattedPayments);
  } catch (err) {
    console.error("Error fetching payments:", err); // เพิ่มบรรทัดนี้เพื่อดู Error ใน Terminal
    res.status(500).json({ error: true, message: err.message });
  }
});
app.put("/admin/payments/:id/status", async (req, res) => {
  try {
    const { status } = req.body; // รับค่า 'approved' หรือ 'rejected'
    const paymentId = req.params.id;

    // 1. อัปเดตสถานะในตาราง payments ก่อน
    await query("UPDATE payments SET status = ? WHERE payment_id = ?", [status, paymentId]);

    // 2. 🌟 พิเศษ: ถ้าแอดมินกด Approve ให้ไปอัปเดตบิล (invoices) ว่า "จ่ายแล้ว" ด้วย
    if (status === "approved") {
      const paymentInfo = await query("SELECT invoice_id FROM payments WHERE payment_id = ?", [paymentId]);
      if (paymentInfo.length > 0) {
        const invoiceId = paymentInfo[0].invoice_id;
        // เปลี่ยนบิลเป็น paid
        await query("UPDATE invoices SET status = 'paid' WHERE invoice_id = ?", [invoiceId]);
      }
    } else if (status === "rejected") {
      const paymentInfo = await query("SELECT invoice_id FROM payments WHERE payment_id = ?", [paymentId]);
      if (paymentInfo.length > 0) {
        const invoiceId = paymentInfo[0].invoice_id;
        // ถ้า Reject ให้เปลี่ยนบิลกลับเป็น unpaid
        await query("UPDATE invoices SET status = 'unpaid' WHERE invoice_id = ?", [invoiceId]);
      }
    }

    res.json({ message: "Status updated successfully" });
  } catch (err) {
    console.error("Error updating payment status:", err);
    res.status(500).json({ error: true, message: err.message });
  }
});

/* =======================
   4. TENANT DASHBOARD
======================= */

app.get("/myroom", async (req, res) => {
  try {
    const { user_id } = req.query;
    const roomInfo = await query(`
      SELECT r.*, rt.move_in_date 
      FROM room_tenants rt 
      JOIN rooms r ON rt.room_id = r.room_id 
      WHERE rt.user_id = ? AND rt.status = 'active'
    `, [user_id]);
    res.json(roomInfo);
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

/* =======================
   5. SUMMARY & MAINTENANCE
======================= */

app.get("/admin/summary", async (req, res) => {
  try {
    const totalRooms = await query("SELECT COUNT(*) as count FROM rooms");
    const availableRooms = await query("SELECT COUNT(*) as count FROM rooms WHERE status = 'available'");
    const pendingSlips = await query("SELECT COUNT(*) as count FROM payments WHERE status = 'pending'");
    const pendingRepairs = await query("SELECT COUNT(*) as count FROM maintenance_requests WHERE status = 'pending'");

    res.json({
      total_rooms: totalRooms[0].count,
      available_rooms: availableRooms[0].count,
      pending_slips: pendingSlips[0].count,
      pending_repairs: pendingRepairs[0].count
    });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

/* =======================
   ROOM EDIT & DELETE (เพิ่มใหม่)
======================= */

// 1. แก้ไขข้อมูลห้อง (เลขห้อง, ชั้น, ราคา)
app.put("/rooms/:id/update", async (req, res) => {
  try {
    const { room_number, floor, price, description } = req.body;
    await query(
      "UPDATE rooms SET room_number = ?, floor = ?, price = ?, description = ? WHERE room_id = ?",
      [room_number, floor, price, description, req.params.id]
    );
    res.json({ message: "Room updated successfully" });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

// 2. ลบห้อง (จะลบได้เฉพาะห้องที่ 'available' เท่านั้นเพื่อความปลอดภัย)
app.delete("/rooms/:id", async (req, res) => {
  try {
    const roomId = req.params.id;
    // เช็คก่อนว่ามีคนอยู่ไหม
    const room = await query("SELECT status FROM rooms WHERE room_id = ?", [roomId]);
    if (room.length > 0 && room[0].status === 'occupied') {
      return res.status(400).json({ error: true, message: "Cannot delete occupied room. Please remove tenant first." });
    }
    await query("DELETE FROM rooms WHERE room_id = ?", [roomId]);
    res.json({ message: "Room deleted successfully" });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});


/* =======================
   6. MAINTENANCE (แจ้งซ่อม)
======================= */
app.post("/maintenance", upload.single('image'), async (req, res) => {
  try {
    const { room_id, user_id, title, description } = req.body;
    const image = req.file ? req.file.filename : null;
    const result = await query(
      "INSERT INTO maintenance_requests (room_id, user_id, title, description, image) VALUES (?, ?, ?, ?, ?)",
      [room_id, user_id, title, description, image]
    );
    res.status(201).json({ message: "Maintenance request created", request_id: result.insertId });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

app.get("/maintenance", async (req, res) => {
  try {
    const reqs = await query("SELECT * FROM maintenance_requests ORDER BY created_at DESC");
    res.json(reqs);
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

app.put("/maintenance/:id", async (req, res) => {
  try {
    const { status } = req.body; 
    const requestId = req.params.id;

    // 🚨 จุดที่ต้องแก้คือบรรทัดนี้ครับ: WHERE request_id = ?
    await query(
      "UPDATE maintenance_requests SET status = ? WHERE request_id = ?", 
      [status, requestId]
    );

    res.json({ message: "Maintenance status updated successfully" });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

/* =======================
   7. ANNOUNCEMENTS (ข่าวประกาศ)
======================= */
app.get("/announcements", async (req, res) => {
  try {
    const list = await query("SELECT * FROM announcements ORDER BY created_at DESC");
    res.json(list);
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

app.post("/announcements", upload.single('image'), async (req, res) => {
  try {
    const { title, content, created_by } = req.body;
    const image = req.file ? req.file.filename : null;
    const result = await query(
      "INSERT INTO announcements (title, content, image, created_by) VALUES (?, ?, ?, ?)",
      [title, content, image, created_by]
    );
    res.status(201).json({ message: "Announcement posted", announce_id: result.insertId });
  } catch (err) {
    res.status(500).json({ error: true, message: err.message });
  }
});

/* =======================
   Start Server
======================= */
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`✅ Server running on port ${PORT}`);
});