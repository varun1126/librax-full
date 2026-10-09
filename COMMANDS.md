# LibraX — Terminal Commands

A simple, direct cheat sheet of all commands needed to run, test, build, and manage LibraX.

---

## 1. Prerequisites Check

### Check Java
```powershell
java -version
```
Checks if Java (JDK 21 or higher) is installed.

### Check Maven
```powershell
mvn -version
```
Checks if Apache Maven is installed and mapped to Java 21.

### Check Node.js
```powershell
node -v
```
Checks if Node.js (v18+) is installed for running the frontend.

### Check npm
```powershell
npm -v
```
Checks if npm package manager is available.

---

## 2. Install Dependencies

### Install Backend Dependencies
```powershell
mvn clean compile
```
Downloads all required Java libraries (Spring Boot, SQLite JDBC, etc.) and compiles the backend.

### Install Frontend Dependencies
```powershell
cd frontend
npm install
cd ..
```
Installs all required frontend packages into `frontend/node_modules`.

---

## 3. Run the Application

### Option A: Single VS Code Terminal (Recommended)
You do NOT need two separate terminals. You can run both the frontend and backend together in a single VS Code terminal:

```powershell
mvn spring-boot:run
```
Spring Boot serves both the REST API and the complete React frontend simultaneously on **http://localhost:8085**.

*(If you ever edit React code and want to update the bundle, run: `cd frontend; npm run build; cd ..; mvn spring-boot:run`)*

---

### Option B: 1-Click Launch (.bat script)
```powershell
.\start-librax.bat
```
Launches the servers in background windows.

---

### Option C: Separate Development Terminals (Vite Hot-Reload)
*Only needed if you want live instant Vite auto-refresh while editing frontend code:*

**Terminal 1 (Backend):**
```powershell
mvn spring-boot:run
```
**Terminal 2 (Frontend with live reload):**
```powershell
cd frontend; npm run dev
```

---

## 4. Open the Website

* **Development Website**: [http://localhost:5173](http://localhost:5173)
* **Backend API Base**: [http://localhost:8085](http://localhost:8085)
* **Health Check**: [http://localhost:8085/api/health](http://localhost:8085/api/health)

### Default Logins
* **Admin**: `admin` / `Admin@123`
* **Student**: `student01` / `Student@123`
* **New Student**: Click "Register here" on the login page.

---

## 5. Testing

### Run Backend Unit & Integration Tests
```powershell
mvn test
```
Runs all automated JUnit tests (configuration, security, student registration, and database concurrency).

### Run Frontend Linter & Build Check
```powershell
cd frontend
npm run lint
npm run build
cd ..
```
Verifies TypeScript types and builds the frontend bundle with zero errors.

---

## 6. Production Build & Run

### Step 1: Build Frontend for Production
```powershell
cd frontend
npm run build
cd ..
```
Compiles and optimizes the React application into `frontend/dist`.

### Step 2: Package Standalone Backend JAR
```powershell
mvn clean package -DskipTests
```
Packages the entire application into a single runnable JAR at `target/librax.jar`.

### Step 3: Run the Production JAR
```powershell
java -jar target/librax.jar
```
Runs the full system (backend + frontend together) on **http://localhost:8085** without needing `npm run dev`.

---

## 7. Stop Application

### Stop Active Servers in Terminal
Press `Ctrl + C` in the running terminal window, then press `Y`.

### Force Stop Any Lingering Processes (PowerShell)
```powershell
Get-Process -Name java, node -ErrorAction SilentlyContinue | Stop-Process -Force
```
Terminates any background Java or Node processes holding ports 8085 or 5173.

---

## 8. Common Problems & Quick Fixes

### Port 8085 Already in Use
```powershell
$pid = (Get-NetTCPConnection -LocalPort 8085 -ErrorAction SilentlyContinue).OwningProcess
if ($pid) { Stop-Process -Id $pid -Force }
```
Kills whatever process is occupying port 8085 so Spring Boot can start.

### Reset Database to Clean Default State
```powershell
Remove-Item "$HOME\.librax\librax-data\librax.db*" -Force -ErrorAction SilentlyContinue
```
Deletes the local SQLite database. On the next backend start, a fresh database is created with default `admin` and `student01` accounts.
