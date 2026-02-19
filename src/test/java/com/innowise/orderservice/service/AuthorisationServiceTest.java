package com.innowise.orderservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorisationService Unit Tests")
class AuthorisationServiceTest {

  @InjectMocks
  private AuthorisationService authorisationService;

  private Authentication adminAuthentication;
  private Authentication userAuthentication;
  private Authentication unauthenticatedAuthentication;

  @BeforeEach
  void setUp() {
    adminAuthentication = new UsernamePasswordAuthenticationToken(
            "1",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );

    userAuthentication = new UsernamePasswordAuthenticationToken(
            "2",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );

    unauthenticatedAuthentication = new UsernamePasswordAuthenticationToken(
            null,
            null,
            Collections.emptyList()
    );
  }

  @Nested
  @DisplayName("isSelf Method Tests")
  class IsSelfTests {

    @Test
    @DisplayName("Should return true when authenticated user matches requested user ID")
    void shouldReturnTrueWhenUserMatchesId() {
      Long userId = 2L;

      boolean result = authorisationService.isSelf(userId, userAuthentication);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when authenticated user does not match requested user ID")
    void shouldReturnFalseWhenUserDoesNotMatchId() {
      Long differentUserId = 3L;

      boolean result = authorisationService.isSelf(differentUserId, userAuthentication);

      assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when authentication is null")
    void shouldReturnFalseWhenAuthenticationIsNull() {
      Long userId = 2L;

      boolean result = authorisationService.isSelf(userId, null);

      assertFalse(result);
    }

    @Test
    @DisplayName("Should handle string to Long conversion for user ID")
    void shouldHandleStringToLongConversion() {
      Long userId = 1L;
      Authentication auth = new UsernamePasswordAuthenticationToken(
              "1",
              null,
              Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
      );

      boolean result = authorisationService.isSelf(userId, auth);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false for invalid user ID string")
    void shouldReturnFalseForInvalidUserIdString() {
      Long userId = 123L;
      Authentication auth = new UsernamePasswordAuthenticationToken(
              "invalid-number",
              null,
              Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
      );

      boolean result = authorisationService.isSelf(userId, auth);

      assertFalse(result);
    }
  }

  @Nested
  @DisplayName("hasAdminRole Method Tests")
  class HasAdminRoleTests {

    @Test
    @DisplayName("Should return true when user has ADMIN role")
    void shouldReturnTrueWhenUserHasAdminRole() {
      boolean result = authorisationService.hasAdminRole(adminAuthentication);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user has USER role")
    void shouldReturnFalseWhenUserHasUserRole() {
      boolean result = authorisationService.hasAdminRole(userAuthentication);

      assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when authentication is null")
    void shouldReturnFalseWhenAuthenticationIsNull() {
      boolean result = authorisationService.hasAdminRole(null);

      assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when authentication is not authenticated")
    void shouldReturnFalseWhenNotAuthenticated() {
      boolean result = authorisationService.hasAdminRole(unauthenticatedAuthentication);

      assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when user has multiple roles including ADMIN")
    void shouldReturnTrueWhenUserHasMultipleRolesIncludingAdmin() {
      Authentication multiRoleAuth = new UsernamePasswordAuthenticationToken(
              "1",
              null,
              List.of(
                      new SimpleGrantedAuthority("ROLE_USER"),
                      new SimpleGrantedAuthority("ROLE_ADMIN"),
                      new SimpleGrantedAuthority("ROLE_MODERATOR")
              )
      );

      boolean result = authorisationService.hasAdminRole(multiRoleAuth);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user has no roles")
    void shouldReturnFalseWhenUserHasNoRoles() {
      Authentication noRoleAuth = new UsernamePasswordAuthenticationToken(
              "1",
              null,
              Collections.emptyList()
      );

      boolean result = authorisationService.hasAdminRole(noRoleAuth);

      assertFalse(result);
    }
  }
}