package com.filepilot.vcs.security;

import com.filepilot.vcs.model.Role;
import com.filepilot.vcs.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "jwt.secret=01234567890123456789012345678901",
        "jwt.expiration=60000"
})
@ActiveProfiles("test")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generate_and_validate_token_and_extract_fields() throws Exception {
        // Arrange
        User user = new User();
        // Use reflection to set private fields in test environment where Lombok may be unavailable for tests
        java.lang.reflect.Field fId = User.class.getDeclaredField("id");
        fId.setAccessible(true);
        fId.set(user, 1L);
        java.lang.reflect.Field fUsername = User.class.getDeclaredField("username");
        fUsername.setAccessible(true);
        fUsername.set(user, "alice");
        java.lang.reflect.Field fEmail = User.class.getDeclaredField("email");
        fEmail.setAccessible(true);
        fEmail.set(user, "alice@example.com");
        java.lang.reflect.Field fPassword = User.class.getDeclaredField("passwordHash");
        fPassword.setAccessible(true);
        fPassword.set(user, "pwd");
        java.lang.reflect.Field fRole = User.class.getDeclaredField("role");
        fRole.setAccessible(true);
        fRole.set(user, Role.ADMIN);

        // Act
        String token = jwtUtil.generateToken(user);

        // Assert
        Assertions.assertNotNull(token);
        Assertions.assertTrue(jwtUtil.validateToken(token));
        Assertions.assertEquals("alice", jwtUtil.extractUsername(token));
        Assertions.assertEquals(1L, jwtUtil.extractUserId(token));
        Assertions.assertEquals(Role.ADMIN.name(), jwtUtil.extractRole(token));
    }
}
