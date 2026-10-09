package com.librax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Spring Boot REST backend entry point for LibraX.
 * Starts the embedded Tomcat HTTP server on port 8080 (or PORT env var).
 */
@SpringBootApplication
public class LibraXServerApplication {

    private static final Logger log = LoggerFactory.getLogger(LibraXServerApplication.class);

    public static void main(String[] args) {
        // Prevent Swing headless issues if launched in server mode
        System.setProperty("java.awt.headless", "true");
        SpringApplication app = new SpringApplication(LibraXServerApplication.class);
        Environment env = app.run(args).getEnvironment();
        logStartup(env);
    }

    private static void logStartup(Environment env) {
        String protocol = "http";
        String serverPort = env.getProperty("server.port", "8080");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Could not determine local host address: {}", e.getMessage());
        }
        log.info("""
                
                ----------------------------------------------------------
                \tApplication '{}' is running! Access URLs:
                \tLocal:      {}://localhost:{}
                \tExternal:   {}://{}:{}
                \tProfile(s): {}
                ----------------------------------------------------------""",
                env.getProperty("spring.application.name", "LibraX"),
                protocol, serverPort,
                protocol, hostAddress, serverPort,
                env.getActiveProfiles().length == 0 ? "default" : env.getActiveProfiles());
    }
}
