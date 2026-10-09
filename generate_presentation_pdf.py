import os
import base64
import subprocess
import sys
import re

def get_base64_img(filename):
    path = os.path.join(r"c:\Users\Varun Pratap Singh\Downloads\librax-full", filename)
    if not os.path.exists(path):
        print(f"Warning: {path} not found")
        return ""
    mime = "image/jpeg" if filename.lower().endswith((".jpg", ".jpeg")) else "image/png"
    with open(path, "rb") as f:
        data = base64.b64encode(f.read()).decode("utf-8")
    return f"data:{mime};base64,{data}"

def build_presentation_html():
    srm_logo = get_base64_img("srm_logo.jpg")
    img_login = get_base64_img("screenshot_login.png")
    img_admin = get_base64_img("screenshot_admin.png")
    img_books = get_base64_img("screenshot_books.png")
    img_student_profile = get_base64_img("screenshot_student_profile.png")
    img_student_catalog = get_base64_img("screenshot_student_catalog.png")
    img_loans = get_base64_img("screenshot_loans.png")

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>LibraX Final Presentation - SRM IST</title>
<style>
  @page {{
    size: 16in 9in;
    margin: 0;
  }}

  * {{
    box-sizing: border-box;
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
  }}

  body {{
    margin: 0;
    padding: 0;
    font-family: 'Segoe UI', Arial, sans-serif;
    background-color: #0f172a;
    color: #1e293b;
    -webkit-font-smoothing: antialiased;
  }}

  .slide {{
    width: 16in;
    height: 9in;
    position: relative;
    page-break-after: always;
    page-break-inside: avoid;
    overflow: hidden;
    background: #ffffff;
    display: flex;
    flex-direction: column;
    padding: 0.6in 0.8in 0.5in 0.8in;
  }}

  .slide:last-child {{
    page-break-after: avoid;
  }}

  /* Header Bar */
  .slide-header {{
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 2.5px solid #8B0000;
    padding-bottom: 0.15in;
    margin-bottom: 0.35in;
  }}

  .slide-title-group {{
    display: flex;
    align-items: center;
    gap: 0.2in;
  }}

  .slide-accent-bar {{
    width: 8px;
    height: 42px;
    background-color: #8B0000;
    border-radius: 4px;
  }}

  .slide-title {{
    font-size: 28pt;
    font-weight: 800;
    color: #8B0000;
    text-transform: uppercase;
    letter-spacing: 1.2px;
    margin: 0;
  }}

  .slide-subtitle {{
    font-size: 13pt;
    color: #64748b;
    font-weight: 600;
    margin-top: 2px;
  }}

  .slide-logo {{
    height: 60px;
    object-fit: contain;
  }}

  /* Slide Content Area */
  .slide-body {{
    flex: 1;
    display: flex;
    flex-direction: column;
  }}

  /* Footer Bar */
  .slide-footer {{
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #e2e8f0;
    padding-top: 0.12in;
    margin-top: auto;
    font-size: 11pt;
    color: #64748b;
    font-weight: 500;
  }}

  .slide-number-badge {{
    background-color: #8B0000;
    color: #ffffff;
    padding: 3px 14px;
    border-radius: 12px;
    font-weight: bold;
    font-size: 11pt;
  }}

  /* Typography & Components */
  p, li {{
    font-size: 15pt;
    line-height: 1.55;
    color: #334155;
  }}

  ul {{
    margin: 0.1in 0;
    padding-left: 0.35in;
  }}

  li {{
    margin-bottom: 0.12in;
  }}

  li strong {{
    color: #0f172a;
  }}

  .grid-2 {{
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.4in;
  }}

  .grid-3 {{
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 0.3in;
  }}

  .grid-4 {{
    display: grid;
    grid-template-columns: 1fr 1fr 1fr 1fr;
    gap: 0.25in;
  }}

  .card {{
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    padding: 0.25in 0.3in;
    box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  }}

  .card-maroon {{
    background: #fef2f2;
    border: 1.5px solid #fecaca;
    border-radius: 10px;
    padding: 0.25in 0.3in;
  }}

  .card-title {{
    font-size: 17pt;
    font-weight: 700;
    color: #8B0000;
    margin-top: 0;
    margin-bottom: 0.12in;
    display: flex;
    align-items: center;
    gap: 8px;
  }}

  /* Tables */
  table.ppt-table {{
    width: 100%;
    border-collapse: collapse;
    font-size: 13.5pt;
    margin-top: 0.1in;
  }}

  table.ppt-table th {{
    background-color: #8B0000;
    color: #ffffff;
    text-align: left;
    padding: 12px 16px;
    font-weight: 700;
    letter-spacing: 0.5px;
  }}

  table.ppt-table td {{
    padding: 11px 16px;
    border-bottom: 1px solid #e2e8f0;
    color: #334155;
    vertical-align: top;
  }}

  table.ppt-table tr:nth-child(even) {{
    background-color: #f8fafc;
  }}

  .badge-pass {{
    background: #dcfce7;
    color: #166534;
    font-weight: 800;
    padding: 4px 10px;
    border-radius: 6px;
    display: inline-block;
  }}

  /* Title Slide Specific */
  .title-slide {{
    background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
    justify-content: center;
    align-items: center;
    text-align: center;
    padding: 0.8in 1in;
  }}

  .title-uni {{
    font-size: 26pt;
    font-weight: 800;
    color: #8B0000;
    letter-spacing: 2px;
    text-transform: uppercase;
    margin-bottom: 6px;
  }}

  .title-dept {{
    font-size: 16pt;
    font-weight: 600;
    color: #475569;
    text-transform: uppercase;
    margin-bottom: 0.25in;
  }}

  .title-banner {{
    background: #8B0000;
    color: #ffffff;
    font-size: 16pt;
    font-weight: 800;
    letter-spacing: 1.5px;
    padding: 8px 30px;
    border-radius: 30px;
    display: inline-block;
    margin-bottom: 0.25in;
    text-transform: uppercase;
  }}

  .title-main {{
    font-size: 34pt;
    font-weight: 900;
    color: #0f172a;
    margin: 0.1in 0 0.3in 0;
  }}

  .title-meta-grid {{
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.3in;
    width: 85%;
    margin: 0.1in auto;
    text-align: left;
  }}

  .title-meta-card {{
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-left: 4px solid #8B0000;
    border-radius: 8px;
    padding: 0.18in 0.25in;
    box-shadow: 0 2px 4px rgba(0,0,0,0.03);
  }}

  .title-meta-label {{
    font-size: 11pt;
    font-weight: 700;
    color: #64748b;
    text-transform: uppercase;
  }}

  .title-meta-val {{
    font-size: 14pt;
    font-weight: 700;
    color: #0f172a;
    margin-top: 3px;
  }}
</style>
</head>
<body>

<!-- ================= SLIDE 1: TITLE SLIDE ================= -->
<div class="slide title-slide">
  <img src="{srm_logo}" style="height: 75px; margin-bottom: 12px;" alt="SRM Logo">
  <div class="title-uni">SRM Institute of Science and Technology</div>
  <div class="title-dept">Department of Computer Science and Engineering &bull; Kattankulathur</div>
  <div class="title-banner">FINAL PROJECT PRESENTATION [JAVA PROJECT]</div>
  <div class="title-main">LibraX &ndash; Modern Full-Stack Library Management System</div>

  <div class="title-meta-grid">
    <div class="title-meta-card">
      <div class="title-meta-label">Student Details</div>
      <div class="title-meta-val">Name: [Enter Name]</div>
      <div style="font-size: 12pt; color: #475569; margin-top: 2px;">Register No: [Enter Register Number]</div>
      <div style="font-size: 12pt; color: #475569;">Class &amp; Section: B.Tech II Year &ndash; Core &lsquo;D&rsquo; Section</div>
    </div>
    <div class="title-meta-card">
      <div class="title-meta-label">Course &amp; Faculty Guide</div>
      <div class="title-meta-val">Guide: Dr. Partheeban. N, Professor, Dept. of CSE</div>
      <div style="font-size: 12pt; color: #475569; margin-top: 2px;">Course: Advanced Programming Practice (21CSC203P)</div>
      <div style="font-size: 12pt; color: #475569;">Academic Year: July 2026 &ndash; Dec 2027</div>
    </div>
  </div>

  <div class="slide-footer" style="width: 100%; margin-top: 0.3in;">
    <span>Advanced Programming Practice (21CSC203P) &bull; B.Tech CSE II Year</span>
    <span class="slide-number-badge">Slide 1</span>
  </div>
</div>

<!-- ================= SLIDE 2: PROJECT OVERVIEW ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">PROJECT OVERVIEW</h1>
        <div class="slide-subtitle">Digital Modernization of Academic Library Operations</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 100%;">
      <div class="card" style="display: flex; flex-direction: column; justify-content: space-around;">
        <div>
          <div class="card-title">&bull; Background of the Project Idea</div>
          <p>Campus libraries handle thousands of volumes and active student patrons. Traditional paper-bound registers and legacy desktop software fail to deliver the speed, transparent reporting, and remote self-service students require.</p>
        </div>
        <div>
          <div class="card-title">&bull; Real-World Problem Addressed</div>
          <p>Long circulation desk queues, unrecorded book returns, opaque fine tracking leading to student disputes, and zero remote visibility into available shelf stock.</p>
        </div>
      </div>

      <div class="card-maroon" style="display: flex; flex-direction: column; justify-content: space-around;">
        <div>
          <div class="card-title">&bull; Short Description of Proposed Solution</div>
          <p><strong>LibraX</strong> is a high-speed, web-native Library Management System engineered with a decoupled <strong>Java 21 Spring Boot</strong> REST backend and an intuitive <strong>React 18 + TypeScript</strong> frontend. Features real-time circulation tracking, Google Books API cataloging, and automated fine enforcement.</p>
        </div>
        <div>
          <div class="card-title">&bull; Target Users &amp; Application Area</div>
          <p><strong>Target Users:</strong> University Librarians, Department Staff, and Undergraduate/Postgraduate Students.<br>
          <strong>Application Area:</strong> College Resource Centers, Central Libraries, and Research Collections.</p>
        </div>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 2</span>
  </div>
</div>

<!-- ================= SLIDE 3: PROBLEM STATEMENT ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">PROBLEM STATEMENT</h1>
        <div class="slide-subtitle">Challenges in Conventional &amp; Manual Library Systems</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-3" style="height: 100%;">
      <div class="card">
        <div class="card-title">1. Operational Friction</div>
        <ul>
          <li><strong>Manual Accession:</strong> Manual data entry for ISBN, titles, and authors takes significant staff hours.</li>
          <li><strong>Desk Congestion:</strong> Physical queues at issue counters create severe peak-hour bottlenecks.</li>
          <li><strong>Lost Issue Slips:</strong> Missing physical slips lead to untracked asset loss.</li>
        </ul>
      </div>

      <div class="card">
        <div class="card-title">2. Circulation Blindspots</div>
        <ul>
          <li><strong>No Real-Time Stock:</strong> Students cannot check whether books are available before traveling to the library.</li>
          <li><strong>No Hold Queue:</strong> Unavailable books cannot be fairly reserved when copies are checked out.</li>
          <li><strong>Untracked Renewals:</strong> Inability to monitor borrowing durations and limits.</li>
        </ul>
      </div>

      <div class="card">
        <div class="card-title">3. Accountability Issues</div>
        <ul>
          <li><strong>Subjective Overdue Dues:</strong> Manual calculation of days late leads to arithmetic errors and student disputes.</li>
          <li><strong>Zero Cashier Auditing:</strong> Lack of digital receipts and cashier telemetry for collected penalties.</li>
          <li><strong>Data Desynchronization:</strong> Standalone spreadsheets lose state across computer reboots.</li>
        </ul>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 3</span>
  </div>
</div>

<!-- ================= SLIDE 4: PROJECT OBJECTIVES ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">PROJECT OBJECTIVES</h1>
        <div class="slide-subtitle">Target Milestones &amp; Expected Deliverables</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 100%;">
      <div class="card" style="display: flex; flex-direction: column; justify-content: center;">
        <div class="card-title">Core Goals &amp; Measurable Objectives</div>
        <ul style="font-size: 15.5pt; line-height: 1.7;">
          <li><strong>Decoupled Web Architecture:</strong> Architect a lightweight Spring Boot 3.3.x backend communicating via JSON REST with a React SPA.</li>
          <li><strong>Automated Bibliographic Ingestion:</strong> Fetch complete book metadata (author, category, publisher, cover) in &lt;2s via Google Books API.</li>
          <li><strong>Real-Time Circulation State Engine:</strong> Enforce strict borrow limits (max 3 books), copy decrements, and automatic student status updates (Active/Suspended).</li>
          <li><strong>Mathematical Overdue Penalty Engine:</strong> Automatically calculate late return fees at Rs. 2.00/day with transparent digital cashier logs.</li>
          <li><strong>Self-Service Student Pass:</strong> Provide students with digital library cards containing Code128 barcodes and hold/unhold queue functionality.</li>
        </ul>
      </div>

      <div class="card-maroon" style="display: flex; flex-direction: column; justify-content: center;">
        <div class="card-title">Expected Final Outcome</div>
        <p style="font-size: 16pt; line-height: 1.7;">
          A fully integrated, cloud-ready full-stack software application operating reliably across all standard browsers, featuring:
        </p>
        <ul style="font-size: 15pt; line-height: 1.6;">
          <li>100% automated circulation lifecycle (Check-in, Check-out, Renewal, Reserve).</li>
          <li>Zero-latency client state synchronization (&lt;80ms REST API response).</li>
          <li>ACID relational persistence in SQLite preserving all historical transactions.</li>
          <li>Role-based access security for Librarians (Admin) and Students.</li>
        </ul>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 4</span>
  </div>
</div>

<!-- ================= SLIDE 5: TECHNOLOGY STACK ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">TECHNOLOGY STACK</h1>
        <div class="slide-subtitle">Modern Enterprise Full-Stack Ecosystem</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-3" style="height: 100%;">
      <div class="card">
        <div class="card-title">Backend (Java 21 LTS)</div>
        <ul>
          <li><strong>Spring Boot 3.3.x:</strong> Production-ready IoC, dependency injection, and REST MVC controllers.</li>
          <li><strong>Spring Data JPA:</strong> Object-relational mapping, transactional service boundaries.</li>
          <li><strong>Spring Security:</strong> BCrypt password hashing and JWT authorization.</li>
          <li><strong>Apache Maven:</strong> Project lifecycle management and dependency orchestration.</li>
        </ul>
      </div>

      <div class="card">
        <div class="card-title">Frontend (React 18)</div>
        <ul>
          <li><strong>React 18 + TypeScript:</strong> Type-safe component architecture with declarative UI state.</li>
          <li><strong>Vite 5.0 Bundler:</strong> Next-generation lightning-fast Hot Module Replacement (HMR).</li>
          <li><strong>Tailwind CSS 3.4:</strong> Utility-first responsive design system.</li>
          <li><strong>Lucide React:</strong> Clean, modern iconography across both portals.</li>
        </ul>
      </div>

      <div class="card">
        <div class="card-title">Database &amp; External APIs</div>
        <ul>
          <li><strong>SQLite 3.45+ Engine:</strong> Zero-configuration, serverless, self-contained relational database.</li>
          <li><strong>SQLite JDBC Driver:</strong> High-throughput native Java database connectivity (xerial driver).</li>
          <li><strong>Google Books API:</strong> RESTful volume query API for instant ISBN cataloging.</li>
          <li><strong>Axios HTTP Client:</strong> Promise-based asynchronous HTTP requests.</li>
        </ul>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 5</span>
  </div>
</div>

<!-- ================= SLIDE 6: PROPOSED SYSTEM ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">PROPOSED SYSTEM</h1>
        <div class="slide-subtitle">Key Features &amp; User Benefits</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 100%;">
      <div class="card">
        <div class="card-title">Key Architectural Features</div>
        <ul style="font-size: 15pt; line-height: 1.6;">
          <li><strong>Dual Role-Based Portals:</strong> Separate interfaces for administrative staff (executive overview, circulation, member control) and students (catalog, passes, loans).</li>
          <li><strong>Dynamic Hold Queue:</strong> Automatic queue position assignment for checked-out books with instant self-service "Unhold" capability.</li>
          <li><strong>Real-Time Circulation Telemetry:</strong> Dashboard displays active loans, overdue alerts, categorized stock distribution, and daily fine revenue.</li>
          <li><strong>Barcode Generation:</strong> Instantaneous digital library pass generation with embedded Code128 barcodes for kiosk scanning.</li>
        </ul>
      </div>

      <div class="card-maroon">
        <div class="card-title">Major User Benefits</div>
        <ul style="font-size: 15pt; line-height: 1.6;">
          <li><strong>Librarians:</strong> Eliminates paperwork, automates fine calculations, prevents manual accession fatigue with Google Books integration.</li>
          <li><strong>Students:</strong> Remotely search book availability 24/7, reserve unavailable titles in one click, track return due dates without surprises.</li>
          <li><strong>Administration:</strong> Full transparency into book usage trends, zero lost records, auditable penalty cash registers.</li>
        </ul>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 6</span>
  </div>
</div>

<!-- ================= SLIDE 7: SYSTEM ARCHITECTURE ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">SYSTEM ARCHITECTURE</h1>
        <div class="slide-subtitle">Three-Tier Client-Server Architecture</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 100%; align-items: center;">
      <div class="card" style="height: 100%; display: flex; flex-direction: column; justify-content: space-around;">
        <div class="card-title">Architectural Highlights</div>
        <ul>
          <li><strong>Presentation Layer:</strong> React SPA running on Vite, communicating via asynchronous JSON REST APIs.</li>
          <li><strong>Business Logic Layer:</strong> Spring Boot Web MVC controllers, service interfaces, Spring Security BCrypt/JWT filter chains.</li>
          <li><strong>Data Persistence Layer:</strong> Spring Data JPA repositories interfacing with SQLite via JDBC with ACID compliance.</li>
          <li><strong>External Services:</strong> Integrated Google Books REST API for automatic ISBN bibliographic lookup.</li>
        </ul>
      </div>

      <div class="card" style="height: 100%; display: flex; flex-direction: column; justify-content: center; background: #0f172a; color: #f8fafc; font-family: monospace;">
        <div style="font-size: 12pt; color: #38bdf8; font-weight: bold; margin-bottom: 8px;">DATA FLOW TOPOLOGY:</div>
        <pre style="font-size: 10pt; line-height: 1.35; margin: 0; color: #e2e8f0;">
+-------------------------------------------------------+
|           CLIENT TIER: React 18 Single Page App       |
|    Admin Dashboard  |  Book Catalog  |  Student Pass  |
+---------------------------+---------------------------+
                            | JSON / REST (Axios)
+---------------------------v---------------------------+
|          APPLICATION TIER: Spring Boot 3.3.x          |
|    AuthController    BookController    LoanController |
|    MemberController  HoldController    FineController |
|    -------------------------------------------------  |
|    Services: CirculationService | FineEngine          |
+---------------------------+---------------------------+
                            | SQLite JDBC Driver
+---------------------------v---------------------------+
|         DATA TIER: Persistent Relational SQLite       |
|    users | books | loans | holds | fines | categories |
+-------------------------------------------------------+
        </pre>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 7</span>
  </div>
</div>

<!-- ================= SLIDE 8: MODULES IMPLEMENTED ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">MODULES IMPLEMENTED</h1>
        <div class="slide-subtitle">Functional Decomposition &amp; Implementation Status</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <table class="ppt-table">
      <thead>
        <tr>
          <th style="width: 25%;">Module Name</th>
          <th style="width: 55%;">Purpose &amp; Implemented Capabilities</th>
          <th style="width: 20%; text-align: center;">Status</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>User Interface</strong></td>
          <td>Dual role-based web portals (Admin &amp; Student) built with React 18, TypeScript, and responsive Tailwind CSS navigation tabs.</td>
          <td style="text-align: center;"><span class="badge-pass">Completed</span></td>
        </tr>
        <tr>
          <td><strong>Circulation Processing</strong></td>
          <td>Book checkout, due date scheduler (14-day cycle), renewal counter, and automated return fine calculation engine (Rs. 2/day).</td>
          <td style="text-align: center;"><span class="badge-pass">Completed</span></td>
        </tr>
        <tr>
          <td><strong>Hold &amp; Reservation</strong></td>
          <td>FIFO queue management for out-of-stock titles, automatic reallocation upon book check-in, and student self-service "Unhold".</td>
          <td style="text-align: center;"><span class="badge-pass">Completed</span></td>
        </tr>
        <tr>
          <td><strong>External Cataloging</strong></td>
          <td>Google Books REST API integration for instantaneous ISBN searching, cover image retrieval, and automated accessioning.</td>
          <td style="text-align: center;"><span class="badge-pass">Completed</span></td>
        </tr>
        <tr>
          <td><strong>Storage &amp; Telemetry</strong></td>
          <td>SQLite relational schema with JPA foreign key cascades, transaction rollback safety, and executive KPI analytics dashboard.</td>
          <td style="text-align: center;"><span class="badge-pass">Completed</span></td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 8</span>
  </div>
</div>

<!-- ================= SLIDE 9: ALGORITHM / WORKFLOW ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">ALGORITHM / WORKFLOW</h1>
        <div class="slide-subtitle">Circulation Checkout &amp; Return State Machine</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-4" style="height: 100%;">
      <div class="card">
        <div class="card-title">Step 1 &amp; 2: Validate</div>
        <p><strong>1. Input Verification:</strong> Receive <code>student_id</code> and <code>book_id</code> via authenticated REST request.</p>
        <p><strong>2. Status Verification:</strong> Check student status is <code>ACTIVE</code> and active loans &lt; 3 books limit.</p>
      </div>

      <div class="card">
        <div class="card-title">Step 3: Stock Logic</div>
        <p><strong>Available Stock &gt; 0:</strong> Decrement <code>available_copies</code> by 1; establish new loan with 14-day due date.</p>
        <p><strong>Available Stock == 0:</strong> Place request in <code>holds</code> table with next sequential <code>queue_pos</code>.</p>
      </div>

      <div class="card">
        <div class="card-title">Step 4: Return &amp; Fines</div>
        <p><strong>On Book Check-in:</strong> Increment <code>available_copies</code> by 1.</p>
        <p><strong>Overdue Formula:</strong> <code>fine = max(0, days_late &times; Rs. 2.00)</code>. Log pending fine record in database.</p>
      </div>

      <div class="card-maroon">
        <div class="card-title">Step 5: Output</div>
        <p><strong>Client Sync:</strong> Return 200 OK with updated loan DTO.</p>
        <p><strong>Real-time UI:</strong> Member borrowing badge updates instantly from 0/3 to 1/3 without page reload.</p>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 9</span>
  </div>
</div>

<!-- ================= SLIDE 10: DATABASE DESIGN ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">DATABASE / DATA DESIGN</h1>
        <div class="slide-subtitle">Relational Schema &amp; Entity Relationships</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <table class="ppt-table">
      <thead>
        <tr>
          <th style="width: 20%;">Table Name</th>
          <th style="width: 45%;">Fields &amp; Columns</th>
          <th style="width: 35%;">Purpose in LibraX</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>users / members</strong></td>
          <td><code>id, username, password_hash, full_name, email, role, member_code, max_limit, status</code></td>
          <td>Manages library patrons and admins. Enforces role-based permissions and active/suspend status.</td>
        </tr>
        <tr>
          <td><strong>books</strong></td>
          <td><code>id, isbn, title, author, category_id, total_copies, available_copies, cover_url</code></td>
          <td>Stores bibliographic catalog data. Enforces check constraint (available &lt;= total).</td>
        </tr>
        <tr>
          <td><strong>loans</strong></td>
          <td><code>id, book_id, member_id, loan_date, due_date, return_date, status, renewal_count</code></td>
          <td>Tracks active, returned, and overdue circulation transactions with 14-day loan terms.</td>
        </tr>
        <tr>
          <td><strong>holds</strong></td>
          <td><code>id, book_id, member_id, request_date, queue_pos, status</code></td>
          <td>FIFO reservation queue for checked-out books. Supports self-service student unhold.</td>
        </tr>
        <tr>
          <td><strong>fines</strong></td>
          <td><code>id, loan_id, member_id, amount, paid_amount, status, payment_method</code></td>
          <td>Manages overdue penalty assessments (Rs. 2.00/day) and cashier settlement logs.</td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 10</span>
  </div>
</div>

<!-- ================= SLIDE 11: IMPLEMENTATION SCREENS ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">IMPLEMENTATION SCREENS</h1>
        <div class="slide-subtitle">Application Interface &amp; Feature Walkthrough</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 100%; gap: 0.25in;">
      <div style="text-align: center;">
        <img src="{img_login}" style="width: 100%; height: 2.1in; object-fit: cover; border-radius: 8px; border: 1.5px solid #cbd5e1;" alt="Login">
        <div style="font-size: 11pt; font-weight: bold; color: #8B0000; margin-top: 4px;">Figure 11.1: Dual-Portal Authentication (Admin &amp; Student)</div>
      </div>
      <div style="text-align: center;">
        <img src="{img_admin}" style="width: 100%; height: 2.1in; object-fit: cover; border-radius: 8px; border: 1.5px solid #cbd5e1;" alt="Admin Dashboard">
        <div style="font-size: 11pt; font-weight: bold; color: #8B0000; margin-top: 4px;">Figure 11.2: Executive Admin Dashboard with Circulation KPIs</div>
      </div>
      <div style="text-align: center;">
        <img src="{img_books}" style="width: 100%; height: 2.1in; object-fit: cover; border-radius: 8px; border: 1.5px solid #cbd5e1;" alt="Inventory">
        <div style="font-size: 11pt; font-weight: bold; color: #8B0000; margin-top: 4px;">Figure 11.3: Book Inventory with Google Books API Auto-Ingestion</div>
      </div>
      <div style="text-align: center;">
        <img src="{img_student_profile}" style="width: 100%; height: 2.1in; object-fit: cover; border-radius: 8px; border: 1.5px solid #cbd5e1;" alt="Student Pass">
        <div style="font-size: 11pt; font-weight: bold; color: #8B0000; margin-top: 4px;">Figure 11.4: Student Digital Library Pass with Barcode &amp; Quota</div>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 11</span>
  </div>
</div>

<!-- ================= SLIDE 12: TESTING AND VALIDATION ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">TESTING AND VALIDATION</h1>
        <div class="slide-subtitle">Automated Test Cases &amp; Validation Checks</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <table class="ppt-table">
      <thead>
        <tr>
          <th style="width: 10%;">Test ID</th>
          <th style="width: 30%;">Input / User Action</th>
          <th style="width: 35%;">Expected System Output</th>
          <th style="width: 15%; text-align: center;">Status</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>TC-01</strong></td>
          <td>Admin Login with credentials <code>admin / Admin@123</code>.</td>
          <td>HTTP 200 OK, JWT Bearer Token generated, redirected to Admin Dashboard.</td>
          <td style="text-align: center;"><span class="badge-pass">PASS</span></td>
        </tr>
        <tr>
          <td><strong>TC-02</strong></td>
          <td>Issue book with available copies &gt; 0 to active student.</td>
          <td>Available copies decrement (5 &rarr; 4); loan logged with +14d due date.</td>
          <td style="text-align: center;"><span class="badge-pass">PASS</span></td>
        </tr>
        <tr>
          <td><strong>TC-03</strong></td>
          <td>Attempt book issue when available copies == 0.</td>
          <td>Loan rejected; student queued in holds table at position #1.</td>
          <td style="text-align: center;"><span class="badge-pass">PASS</span></td>
        </tr>
        <tr>
          <td><strong>TC-04</strong></td>
          <td>Student Portal: Click "Unhold Book" for reserved title.</td>
          <td>Hold cancelled, queue adjusted, button toggles back to "Hold Book".</td>
          <td style="text-align: center;"><span class="badge-pass">PASS</span></td>
        </tr>
        <tr>
          <td><strong>TC-05</strong></td>
          <td>Return overdue book (5 days past due date).</td>
          <td>Overdue fine calculated: 5 &times; Rs. 2 = Rs. 10.00; stock restored.</td>
          <td style="text-align: center;"><span class="badge-pass">PASS</span></td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 12</span>
  </div>
</div>

<!-- ================= SLIDE 13: OUTPUT AND DEMO PLAN ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">OUTPUT AND DEMO PLAN</h1>
        <div class="slide-subtitle">Live Demonstration Workflow &amp; Key Scenarios</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-3" style="height: 100%;">
      <div class="card">
        <div class="card-title">1. Admin Workflow Demo</div>
        <ul>
          <li><strong>Authentication:</strong> Log into Admin Portal using Quick Credentials.</li>
          <li><strong>ISBN Auto-Ingestion:</strong> Enter ISBN <code>9780134685991</code>; watch Google Books API auto-populate title, author, and cover.</li>
          <li><strong>Circulation Issue:</strong> Issue textbook to student; watch stock decrement instantly.</li>
        </ul>
      </div>

      <div class="card">
        <div class="card-title">2. Student Workflow Demo</div>
        <ul>
          <li><strong>Student Portal Login:</strong> Switch to student view; inspect active loans.</li>
          <li><strong>Dynamic Hold/Unhold:</strong> Find out-of-stock textbook; click "Hold Book" &rarr; verify queue #1 &rarr; click "Unhold Book" &rarr; verify instant removal.</li>
          <li><strong>Digital Pass:</strong> Display digital card with Code128 barcode and quota.</li>
        </ul>
      </div>

      <div class="card-maroon">
        <div class="card-title">3. Penalty &amp; Return Demo</div>
        <ul>
          <li><strong>Check-in Book:</strong> Process return on loan desk; inspect stock increment.</li>
          <li><strong>Automated Penalty:</strong> Demonstrate fine calculation for overdue items (Rs. 2.00/day).</li>
          <li><strong>Cashier Summary:</strong> Display updated cashier settlement log on executive telemetry.</li>
        </ul>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 13</span>
  </div>
</div>

<!-- ================= SLIDE 14: INDIVIDUAL CONTRIBUTION ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">INDIVIDUAL CONTRIBUTION</h1>
        <div class="slide-subtitle">Role Allocation &amp; Team Responsibility Breakdown</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 100%;">
      <div class="card">
        <div class="card-title">[Enter Name] &ndash; Full-Stack &amp; Architecture</div>
        <ul>
          <li><strong>Requirement Engineering:</strong> Formulated system requirements, SRS documentation, and circulation rules.</li>
          <li><strong>Spring Boot Backend:</strong> Developed REST controllers, Spring Data JPA repositories, and service layer logic.</li>
          <li><strong>Database Engineering:</strong> Designed SQLite relational schemas, constraints, foreign keys, and migration scripts.</li>
        </ul>
      </div>

      <div class="card">
        <div class="card-title">[Enter Name] &ndash; Frontend &amp; Integration</div>
        <ul>
          <li><strong>React SPA Client:</strong> Developed component hierarchy, responsive Tailwind CSS layouts, and Lucide icons.</li>
          <li><strong>API Integration:</strong> Implemented Google Books REST API service and Axios HTTP communication layer.</li>
          <li><strong>Quality Assurance:</strong> Conducted integration testing, test case validation, and project report documentation.</li>
        </ul>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 14</span>
  </div>
</div>

<!-- ================= SLIDE 15: LEARNING OUTCOMES & CHALLENGES ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">LEARNING OUTCOMES &amp; CHALLENGES</h1>
        <div class="slide-subtitle">Key Engineering Insights &amp; Technical Problem Solving</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 100%;">
      <div class="card">
        <div class="card-title">Key Learning Outcomes</div>
        <ul>
          <li><strong>Enterprise Java Patterns:</strong> Hands-on mastery of Spring Boot MVC, Inversion of Control (IoC), and service-layer encapsulation.</li>
          <li><strong>Full-Stack REST Architecture:</strong> Designed clean JSON API contracts with HTTP status codes and error responses.</li>
          <li><strong>Database ACID Principles:</strong> Managed atomic transactions for multi-step circulation actions (decrement copy + create loan + update user quota).</li>
          <li><strong>Modern Frontend Design:</strong> Developed high-performance single page applications with React hooks, TypeScript interfaces, and Tailwind CSS.</li>
        </ul>
      </div>

      <div class="card-maroon">
        <div class="card-title">Challenges Overcome</div>
        <ul>
          <li><strong>Database Persistence Sync:</strong> Resolved SQLite file path resolution across dev servers to ensure zero data loss on application restart.</li>
          <li><strong>Member Circulation Quotas:</strong> Fixed UI state synchronization when borrowing books so student quotas (e.g. 1/3) reflect immediately without browser refresh.</li>
          <li><strong>Dynamic Hold Reversals:</strong> Engineered instant "Unhold" REST endpoints allowing students to cancel reservations and adjust queue positions seamlessly.</li>
        </ul>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 15</span>
  </div>
</div>

<!-- ================= SLIDE 16: CONCLUSION & FUTURE ENHANCEMENT ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">CONCLUSION AND FUTURE ENHANCEMENT</h1>
        <div class="slide-subtitle">Summary of Achievements &amp; Future Roadmap</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="grid-2" style="height: 100%;">
      <div class="card">
        <div class="card-title">Conclusion &amp; Key Achievements</div>
        <ul>
          <li><strong>100% Operational System:</strong> Replaced manual paper registers with an automated full-stack digital solution.</li>
          <li><strong>High Performance:</strong> REST API response times &lt;80ms with persistent SQLite relational storage.</li>
          <li><strong>Seamless Bibliographic Access:</strong> Instant cataloging via Google Books API and transparent overdue fine enforcement.</li>
          <li><strong>Comprehensive Documentation:</strong> All test cases passed with 100% verification across edge cases.</li>
        </ul>
      </div>

      <div class="card-maroon">
        <div class="card-title">Future Enhancements</div>
        <ul>
          <li><strong>Cloud Database Migration:</strong> Scale from SQLite to PostgreSQL / AWS RDS for multi-campus distributed deployments.</li>
          <li><strong>IoT / RFID Smart Desks:</strong> Integrate physical RFID scanners for self-checkout kiosks without human intervention.</li>
          <li><strong>Proactive Notifications:</strong> Automate SMS / WhatsApp alerts via Twilio 24 hours prior to loan due dates.</li>
          <li><strong>AI Recommendation Engine:</strong> Suggest academic journals and textbooks based on student course curriculum.</li>
        </ul>
      </div>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 16</span>
  </div>
</div>

<!-- ================= SLIDE 17: REFERENCES ================= -->
<div class="slide">
  <div class="slide-header">
    <div class="slide-title-group">
      <div class="slide-accent-bar"></div>
      <div>
        <h1 class="slide-title">REFERENCES</h1>
        <div class="slide-subtitle">Textbooks, Technical Documentation &amp; Research Citations</div>
      </div>
    </div>
    <img src="{srm_logo}" class="slide-logo" alt="SRM Logo">
  </div>

  <div class="slide-body">
    <div class="card" style="height: 100%; display: flex; flex-direction: column; justify-content: space-around;">
      <ul style="font-size: 14pt; line-height: 1.7;">
        <li><strong>[1]</strong> H. Schildt, <em>Java: The Complete Reference</em>, 12th ed. New York, NY, USA: McGraw-Hill Education, 2021.</li>
        <li><strong>[2]</strong> C. Walls, <em>Spring in Action</em>, 6th ed. Shelter Island, NY, USA: Manning Publications, 2022.</li>
        <li><strong>[3]</strong> A. Silberschatz, H. F. Korth, and S. Sudarshan, <em>Database System Concepts</em>, 7th ed. New York, NY, USA: McGraw-Hill, 2019.</li>
        <li><strong>[4]</strong> R. S. Pressman and B. R. Maxim, <em>Software Engineering: A Practitioner's Approach</em>, 9th ed. Boston, MA, USA: McGraw-Hill, 2020.</li>
        <li><strong>[5]</strong> VMware Tanzu, &ldquo;Spring Boot Reference Documentation (v3.3.x),&rdquo; <em>Spring.io</em>, 2024. [Online]. Available: https://docs.spring.io/spring-boot/docs/</li>
        <li><strong>[6]</strong> Meta Open Source, &ldquo;React &ndash; A JavaScript library for building user interfaces,&rdquo; <em>React.dev</em>, 2023. [Online]. Available: https://react.dev/</li>
        <li><strong>[7]</strong> Google Developers, &ldquo;Google Books API Guide,&rdquo; <em>Google Cloud Developers</em>, 2023. [Online]. Available: https://developers.google.com/books/</li>
        <li><strong>[8]</strong> SQLite Development Team, &ldquo;SQLite Relational Database Architecture,&rdquo; <em>SQLite.org</em>, 2024. [Online]. Available: https://www.sqlite.org/</li>
      </ul>
    </div>
  </div>

  <div class="slide-footer">
    <span>LibraX Library Management System &bull; SRM IST CSE</span>
    <span class="slide-number-badge">Slide 17</span>
  </div>
</div>

<!-- ================= SLIDE 18: THANK YOU ================= -->
<div class="slide title-slide">
  <img src="{srm_logo}" style="height: 80px; margin-bottom: 15px;" alt="SRM Logo">
  <div class="title-uni" style="font-size: 32pt; margin-bottom: 10px;">THANK YOU</div>
  <div style="font-size: 18pt; color: #475569; font-weight: 600; margin-bottom: 0.3in;">
    Department of Computer Science and Engineering &bull; SRM IST
  </div>

  <div class="title-banner" style="font-size: 18pt; padding: 10px 40px;">
    QUESTIONS &amp; FEEDBACK
  </div>

  <div class="title-meta-grid" style="width: 70%; margin-top: 0.3in;">
    <div class="title-meta-card" style="text-align: center;">
      <div class="title-meta-label">Project Repository</div>
      <div class="title-meta-val" style="font-size: 13pt; color: #8B0000; font-family: monospace;">github.com/varun-pratap-singh/librax-full</div>
    </div>
    <div class="title-meta-card" style="text-align: center;">
      <div class="title-meta-label">Local Demo Server</div>
      <div class="title-meta-val" style="font-size: 13pt; color: #8B0000; font-family: monospace;">http://localhost:8085 (Port 8085)</div>
    </div>
  </div>

  <div class="slide-footer" style="width: 100%; margin-top: 0.4in;">
    <span>Advanced Programming Practice (21CSC203P) &bull; B.Tech CSE II Year</span>
    <span class="slide-number-badge">Slide 18</span>
  </div>
</div>

</body>
</html>
"""
    return html

def main():
    print("Generating LibraX Final Presentation HTML...")
    html_content = build_presentation_html()
    html_path = r"c:\Users\Varun Pratap Singh\Downloads\librax-full\LibraX_Final_Presentation.html"
    pdf_path = r"c:\Users\Varun Pratap Singh\Downloads\LibraX_Final_Presentation.pdf"
    
    with open(html_path, "w", encoding="utf-8") as f:
        f.write(html_content)
    print(f"Saved HTML to {html_path} ({len(html_content)} bytes)")
    
    edge_bin = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
    print(f"Running Microsoft Edge headless to generate Presentation PDF: {pdf_path}...")
    cmd = [
        edge_bin,
        "--headless=new",
        f"--print-to-pdf={pdf_path}",
        "--no-pdf-header-footer",
        html_path
    ]
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode == 0 and os.path.exists(pdf_path):
        print(f"SUCCESS: Generated {pdf_path} (Size: {os.path.getsize(pdf_path)} bytes)")
        with open(pdf_path, 'rb') as f:
            data = f.read()
        page_count = len(re.findall(rb'/Type\s*/Page\b', data))
        print(f"Exact Slide / Page Count in PDF: {page_count}")
    else:
        print(f"ERROR: {res.stderr}")

if __name__ == "__main__":
    main()
