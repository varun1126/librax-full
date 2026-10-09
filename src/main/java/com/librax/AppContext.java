package com.librax;

import com.librax.api.GoogleBooksClient;
import com.librax.api.GoogleBooksService;
import com.librax.config.ApiConfig;
import com.librax.config.AppConfig;
import com.librax.config.DatabaseConfig;
import com.librax.config.DatabaseManager;
import com.librax.logging.AuditLogger;
import com.librax.repository.*;
import com.librax.repository.impl.*;
import com.librax.security.PasswordHasher;
import com.librax.security.SessionManager;
import com.librax.service.*;

/**
 * Composition root: constructs every repository, service, and shared
 * component exactly once and wires them together via constructor
 * injection (RULES.md §4). UI classes receive what they need from here
 * rather than constructing their own dependencies.
 */
public final class AppContext {

    public final AppConfig appConfig;
    public final DatabaseConfig databaseConfig;
    public final DatabaseManager databaseManager;

    public final UserRepository userRepository;
    public final MemberRepository memberRepository;
    public final BookRepository bookRepository;
    public final LoanRepository loanRepository;
    public final ReservationRepository reservationRepository;
    public final FineRepository fineRepository;
    public final AuditLogRepository auditLogRepository;

    public final PasswordHasher passwordHasher;
    public final SessionManager sessionManager;
    public final AuditLogger auditLogger;

    public final AuthenticationService authenticationService;
    public final BookService bookService;
    public final MemberService memberService;
    public final FineService fineService;
    public final ReservationService reservationService;
    public final LoanService loanService;
    public final ReportService reportService;
    public final AnalyticsService analyticsService;

    public final ApiConfig apiConfig;
    public final GoogleBooksClient googleBooksClient;
    public final GoogleBooksService googleBooksService;

    public AppContext() {
        this.appConfig = AppConfig.getInstance();
        this.databaseConfig = new DatabaseConfig(appConfig);
        this.databaseManager = new DatabaseManager(databaseConfig);
        this.databaseManager.initializeSchema();

        this.userRepository = new JdbcUserRepository(databaseManager);
        this.memberRepository = new JdbcMemberRepository(databaseManager);
        this.bookRepository = new JdbcBookRepository(databaseManager);
        this.loanRepository = new JdbcLoanRepository(databaseManager);
        this.reservationRepository = new JdbcReservationRepository(databaseManager);
        this.fineRepository = new JdbcFineRepository(databaseManager);
        this.auditLogRepository = new JdbcAuditLogRepository(databaseManager);

        this.passwordHasher = new PasswordHasher();
        this.sessionManager = new SessionManager();
        this.auditLogger = new AuditLogger(auditLogRepository);

        this.authenticationService = new AuthenticationService(userRepository, passwordHasher, sessionManager, auditLogger);
        this.bookService = new BookService(bookRepository, auditLogger, sessionManager);
        this.memberService = new MemberService(memberRepository, auditLogger, sessionManager);
        this.fineService = new FineService(fineRepository, auditLogger, sessionManager, appConfig);
        this.reservationService = new ReservationService(reservationRepository, bookRepository, auditLogger, sessionManager);
        this.loanService = new LoanService(loanRepository, bookRepository, memberRepository, memberService,
                fineService, reservationService, auditLogger, sessionManager, appConfig);
        this.reportService = new ReportService(bookRepository, memberRepository, loanRepository, fineRepository);
        this.analyticsService = new AnalyticsService(bookRepository, memberRepository, loanRepository,
                reservationRepository, fineRepository);

        this.apiConfig = new ApiConfig(appConfig);
        this.googleBooksClient = new GoogleBooksClient(apiConfig);
        this.googleBooksService = new GoogleBooksService(googleBooksClient);
    }
}
