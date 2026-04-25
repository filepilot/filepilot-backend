package com.filepilot.vcs.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Refuses to start the prod profile if it's still using the dev defaults for the JWT secret
 * or DB password. In any profile, logs a warning when defaults are in use.
 */
@Slf4j
@Component
public class StartupConfigValidator {

    static final String DEFAULT_JWT_SECRET = "myDefaultDevSecretKeyThatIsAtLeast256BitsLong!!";
    static final String DEFAULT_DB_PASSWORD = "filepilot123";

    private final Environment environment;
    private final String jwtSecret;
    private final String dbPassword;

    public StartupConfigValidator(Environment environment,
                                  @Value("${jwt.secret}") String jwtSecret,
                                  @Value("${spring.datasource.password:}") String dbPassword) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.dbPassword = dbPassword;
    }

    @PostConstruct
    public void validate() {
        boolean isProd = false;
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile)) {
                isProd = true;
                break;
            }
        }

        boolean defaultJwt = DEFAULT_JWT_SECRET.equals(jwtSecret);
        boolean defaultDb = DEFAULT_DB_PASSWORD.equals(dbPassword);

        if (isProd && (defaultJwt || defaultDb)) {
            String which = (defaultJwt ? " jwt.secret" : "")
                    + (defaultDb ? " spring.datasource.password" : "");
            String msg = "Refusing to start: prod profile is using default credentials for"
                    + which + ". Set the corresponding environment variables.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        if (defaultJwt) {
            log.warn("Using default JWT secret — only safe for local dev.");
        }
        if (defaultDb) {
            log.warn("Using default DB password — only safe for local dev.");
        }
    }
}
