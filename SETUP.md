# LibraX — Setup Guide

## 1. Requirements

- **JDK 21** (LTS) — verify with `java -version`
- **Maven 3.9+** — verify with `mvn -version`
- **Git**
- IntelliJ IDEA or Eclipse (recommended, not required)

## 2. Installation

1. **Clone the project**
   ```bash
   git clone <repo-url> librax
   cd librax
   ```

2. **Open as a Maven project**
   - IntelliJ IDEA: *File → Open* → select the `librax` folder (it detects
     `pom.xml` automatically).
   - Eclipse: *File → Import → Maven → Existing Maven Projects* → select
     `librax`.

3. **Verify JDK 21 is selected** as the project SDK in your IDE
   (`File → Project Structure → SDK` in IntelliJ, or
   `Project → Properties → Java Build Path` in Eclipse).

4. **Download dependencies**
   ```bash
   mvn dependency:resolve
   ```
   (or just let your IDE resolve them on import).

5. **Configure the application** — edit
   `src/main/resources/application.properties` if you want to change the
   database location, loan period, fine rate, library name, etc. Defaults
   work out of the box.

6. **Initialize the database** — nothing to do manually; on first launch
   LibraX creates its data directory and SQLite database automatically and
   walks you through creating the first administrator account.

7. **Run `App.java`**
   - From an IDE: right-click `com.librax.App` → Run.
   - From the command line: see §5 below.

## 3. Database

- Location: configurable via `library.database.path` in
  `application.properties`; defaults to a `librax.db` SQLite file under
  the application's data directory (e.g. `~/.librax/librax.db` on
  Linux/macOS, `%APPDATA%\LibraX\librax.db` on Windows).
- Schema initialization: handled automatically by `DatabaseManager` from
  `src/main/resources/db/schema.sql` on first run; subsequent runs reuse
  the existing file untouched (see §28 first-run process in the project
  brief and `MEMORY.md` for current status — full schema ships in Phase 2).

## 4. Google Books API

No API key is required for basic public search against
`https://www.googleapis.com/books/v1/volumes`. If you want to configure a
key (for higher rate limits):

1. Obtain a key from the Google Cloud Console (Books API enabled).
2. Set it via an environment variable, e.g.:
   ```bash
   export LIBRAX_GOOGLE_BOOKS_API_KEY=your-key-here
   ```
3. `config.ApiConfig` reads this environment variable at startup and falls
   back to unauthenticated requests if it is not set. **Never** commit a
   key into `application.properties` or source code.

## 5. Running

```bash
mvn clean test        # run the test suite
mvn clean package      # build target/librax.jar
java -jar target/librax.jar   # launch the application
```

## 6. Troubleshooting

| Problem | Likely cause / fix |
|---|---|
| `mvn` fails with "invalid target release: 21" | Your `JAVA_HOME`/IDE JDK is older than 21 — install/select JDK 21 |
| Dependency resolution errors | Check internet access to Maven Central, or a corporate proxy/mirror is misconfigured in `~/.m2/settings.xml` |
| `SQLITE_BUSY` / locked database errors | Another running instance holds the DB file open — close other running copies of LibraX |
| `SQLITE_CONSTRAINT` errors on startup | The database file is from an older/incompatible schema version — back it up and delete it to let LibraX recreate it, or run the appropriate migration |
| Google Books search always fails | Check internet connectivity; corporate firewalls may block `googleapis.com`; the app remains fully usable offline regardless |
| UI looks like default Swing (not modern) | FlatLaf failed to initialize — check startup logs for a `UnsupportedLookAndFeelException` and confirm the `flatlaf` dependency resolved correctly |
| Blank/garbled fonts on Linux | Install a system font such as `Inter` or accept the FlatLaf system-font fallback |
