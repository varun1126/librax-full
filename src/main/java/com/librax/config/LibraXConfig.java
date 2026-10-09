package com.librax.config;

import com.librax.api.GoogleBooksClient;
import com.librax.api.GoogleBooksService;
import com.librax.logging.AuditLogger;
import com.librax.repository.*;
import com.librax.repository.impl.*;
import com.librax.security.PasswordHasher;
import com.librax.security.SessionManager;
import com.librax.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration exposing existing LibraX core services,
 * repositories, and configurations as managed Spring beans.
 * Preserves existing architecture and enables clean injection in REST controllers.
 */
@Configuration
public class LibraXConfig {

    @Bean
    public AppConfig appConfig() {
        return AppConfig.getInstance();
    }

    @Bean
    public DatabaseConfig databaseConfig(AppConfig appConfig) {
        return new DatabaseConfig(appConfig);
    }

    @Bean
    public DatabaseManager databaseManager(DatabaseConfig databaseConfig) {
        DatabaseManager dm = new DatabaseManager(databaseConfig);
        dm.initializeSchema();
        return dm;
    }

    @Bean
    public UserRepository userRepository(DatabaseManager databaseManager) {
        return new JdbcUserRepository(databaseManager);
    }

    @Bean
    public MemberRepository memberRepository(DatabaseManager databaseManager) {
        return new JdbcMemberRepository(databaseManager);
    }

    @Bean
    public BookRepository bookRepository(DatabaseManager databaseManager) {
        return new JdbcBookRepository(databaseManager);
    }

    @Bean
    public LoanRepository loanRepository(DatabaseManager databaseManager) {
        return new JdbcLoanRepository(databaseManager);
    }

    @Bean
    public ReservationRepository reservationRepository(DatabaseManager databaseManager) {
        return new JdbcReservationRepository(databaseManager);
    }

    @Bean
    public FineRepository fineRepository(DatabaseManager databaseManager) {
        return new JdbcFineRepository(databaseManager);
    }

    @Bean
    public AuditLogRepository auditLogRepository(DatabaseManager databaseManager) {
        return new JdbcAuditLogRepository(databaseManager);
    }

    @Bean
    public NotificationRepository notificationRepository(DatabaseManager databaseManager) {
        return new JdbcNotificationRepository(databaseManager);
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    public SessionManager sessionManager() {
        return new SessionManager();
    }

    @Bean
    public AuditLogger auditLogger(AuditLogRepository auditLogRepository) {
        return new AuditLogger(auditLogRepository);
    }

    @Bean
    public AuthenticationService authenticationService(UserRepository userRepository,
                                                       PasswordHasher passwordHasher,
                                                       SessionManager sessionManager,
                                                       AuditLogger auditLogger) {
        return new AuthenticationService(userRepository, passwordHasher, sessionManager, auditLogger);
    }

    @Bean
    public BookService bookService(BookRepository bookRepository,
                                   AuditLogger auditLogger,
                                   SessionManager sessionManager) {
        return new BookService(bookRepository, auditLogger, sessionManager);
    }

    @Bean
    public MemberService memberService(MemberRepository memberRepository,
                                       AuditLogger auditLogger,
                                       SessionManager sessionManager) {
        return new MemberService(memberRepository, auditLogger, sessionManager);
    }

    @Bean
    public FineService fineService(FineRepository fineRepository,
                                   AuditLogger auditLogger,
                                   SessionManager sessionManager,
                                   AppConfig appConfig) {
        return new FineService(fineRepository, auditLogger, sessionManager, appConfig);
    }

    @Bean
    public ReservationService reservationService(ReservationRepository reservationRepository,
                                                 BookRepository bookRepository,
                                                 AuditLogger auditLogger,
                                                 SessionManager sessionManager) {
        return new ReservationService(reservationRepository, bookRepository, auditLogger, sessionManager);
    }

    @Bean
    public LoanService loanService(LoanRepository loanRepository,
                                   BookRepository bookRepository,
                                   MemberRepository memberRepository,
                                   MemberService memberService,
                                   FineService fineService,
                                   ReservationService reservationService,
                                   AuditLogger auditLogger,
                                   SessionManager sessionManager,
                                   AppConfig appConfig) {
        return new LoanService(loanRepository, bookRepository, memberRepository, memberService,
                fineService, reservationService, auditLogger, sessionManager, appConfig);
    }

    @Bean
    public ReportService reportService(BookRepository bookRepository,
                                       MemberRepository memberRepository,
                                       LoanRepository loanRepository,
                                       FineRepository fineRepository) {
        return new ReportService(bookRepository, memberRepository, loanRepository, fineRepository);
    }

    @Bean
    public AnalyticsService analyticsService(BookRepository bookRepository,
                                             MemberRepository memberRepository,
                                             LoanRepository loanRepository,
                                             ReservationRepository reservationRepository,
                                             FineRepository fineRepository) {
        return new AnalyticsService(bookRepository, memberRepository, loanRepository,
                reservationRepository, fineRepository);
    }

    @Bean
    public ApiConfig apiConfig(AppConfig appConfig) {
        return new ApiConfig(appConfig);
    }

    @Bean
    public GoogleBooksClient googleBooksClient(ApiConfig apiConfig) {
        return new GoogleBooksClient(apiConfig);
    }

    @Bean
    public GoogleBooksService googleBooksService(GoogleBooksClient googleBooksClient) {
        return new GoogleBooksService(googleBooksClient);
    }
}
