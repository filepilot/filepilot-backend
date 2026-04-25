package com.filepilot.vcs.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Refuses to start whenever the dev defaults for jwt.secret or the DB password are in use,
 * unless app.allow-dev-defaults=true is explicitly set (only intended for local development).
 * This protects against deployments that forget to set SPRING_PROFILES_ACTIVE=prod.
 */
@Slf4j
@Component
public class StartupConfigValidator {

    static final String DEFAULT_JWT_SECRET = "myDefaultDevSecretKeyThatIsAtLeast256BitsLong!!";
    static final String DEFAULT_DB_PASSWORD = "filepilot123";

    private final Environment environment;
    private final String jwtSecret;
    private final String dbPassword;
    private final boolean allowDevDefaults;

    public StartupConfigValidator(Environment environment,
                                  @Value("${jwt.secret}") String jwtSecret,
                                  @Value("${spring.datasource.password:}") String dbPassword,
                                  @Value("${app.allow-dev-defaults:false}") boolean allowDevDefaults) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.dbPassword = dbPassword;
        this.allowDevDefaults = allowDevDefaults;
    }

    @PostConstruct
    public void validate() {
        boolean defaultJwt = DEFAULT_JWT_SECRET.equals(jwtSecret);
        boolean defaultDb = DEFAULT_DB_PASSWORD.equals(dbPassword);

        if (!defaultJwt && !defaultDb) {
            return;
        }

        String which = (defaultJwt ? " jwt.secret" : "")
                + (defaultDb ? " spring.datasource.password" : "");

        if (!allowDevDefaults) {
            String activeProfiles = String.join(",", environment.getActiveProfiles());
            String msg = "Refusing to start: default credentials in use for" + which
                    + " (active profiles: [" + activeProfiles + "]). "
                    + "Set the corresponding environment variables, "
                    + "or set app.allow-dev-defaults=true for local dev only.";
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
