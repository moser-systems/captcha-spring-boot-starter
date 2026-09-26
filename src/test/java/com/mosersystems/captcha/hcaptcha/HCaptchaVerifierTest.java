package com.mosersystems.captcha.hcaptcha;

import com.mosersystems.captcha.CaptchaProperties;
import com.mosersystems.captcha.CaptchaVerificationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("HCaptchaVerifier Tests")
class HCaptchaVerifierTest {

    private static final String ENDPOINT = "https://example.com/siteverify";
    private static final String SECRET = "test-secret";

    private MockRestServiceServer server;

    private HCaptchaVerifier verifier(String siteKey, String hostname) {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        return new HCaptchaVerifier(new CaptchaProperties.HCaptcha(siteKey, SECRET, ENDPOINT, hostname), builder.build());
    }

    private HCaptchaVerifier verifier() {
        return verifier(null, null);
    }

    @Test
    @DisplayName("Should send form-encoded POST request to hCaptcha")
    void testVerifySuccessful() {
        HCaptchaVerifier verifier = verifier();
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formDataContains(Map.of("secret", SECRET, "response", "valid-token")))
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));

        assertDoesNotThrow(() -> verifier.verify("valid-token"));
        server.verify();
    }

    @Test
    @DisplayName("Should send sitekey and remoteip when available")
    void testSiteKeyAndRemoteIp() {
        HCaptchaVerifier verifier = verifier("site-key", null);
        server.expect(requestTo(ENDPOINT))
                .andExpect(content().formDataContains(Map.of("sitekey", "site-key", "remoteip", "192.0.2.1")))
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));

        assertDoesNotThrow(() -> verifier.verify("valid-token", "192.0.2.1"));
        server.verify();
    }

    @Test
    @DisplayName("Should throw exception when verification fails")
    void testVerifyFailed() {
        HCaptchaVerifier verifier = verifier();
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("{\"success\":false}", MediaType.APPLICATION_JSON));

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify("invalid-token"));
    }

    @Test
    @DisplayName("Should include error codes in exception message")
    void testVerifyFailedWithErrorCodes() {
        HCaptchaVerifier verifier = verifier();
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(
                        "{\"success\":false,\"error-codes\":[\"invalid-input-response\",\"timeout-or-duplicate\"]}",
                        MediaType.APPLICATION_JSON));

        CaptchaVerificationException exception = assertThrows(CaptchaVerificationException.class,
                () -> verifier.verify("invalid-token"));

        assertTrue(exception.getMessage().contains("invalid-input-response"),
                "Exception should contain error code");
    }

    @Test
    @DisplayName("Should validate hostname when configured")
    void testHostnameValidation() {
        HCaptchaVerifier verifier = verifier(null, "example.com");
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(
                        "{\"success\":true,\"challenge_ts\":\"2026-09-26T10:00:00Z\",\"hostname\":\"example.com\",\"credit\":false}",
                        MediaType.APPLICATION_JSON));

        assertDoesNotThrow(() -> verifier.verify("valid-token"));
    }

    @Test
    @DisplayName("Should fail when hostname does not match")
    void testHostnameMismatch() {
        HCaptchaVerifier verifier = verifier(null, "example.com");
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("{\"success\":true,\"hostname\":\"attacker.com\"}", MediaType.APPLICATION_JSON));

        CaptchaVerificationException exception = assertThrows(CaptchaVerificationException.class,
                () -> verifier.verify("valid-token"));

        assertTrue(exception.getMessage().contains("Hostname mismatch"),
                "Exception should mention hostname mismatch");
    }

    @Test
    @DisplayName("Should fail when hostname is expected but missing")
    void testHostnameMissing() {
        HCaptchaVerifier verifier = verifier(null, "example.com");
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify("valid-token"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should throw exception for null, empty or whitespace token")
    void testEmptyToken(String token) {
        HCaptchaVerifier verifier = verifier();

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify(token));
    }

    @Test
    @DisplayName("Should throw exception when secret is not configured")
    void testMissingSecret() {
        HCaptchaVerifier verifier = new HCaptchaVerifier(
                new CaptchaProperties.HCaptcha(null, null, ENDPOINT, null), RestClient.create());

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify("valid-token"));
    }

    @Test
    @DisplayName("Should throw exception when response body is empty")
    void testEmptyResponseBody() {
        HCaptchaVerifier verifier = verifier();
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess());

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify("valid-token"));
    }

    @Test
    @DisplayName("Should throw exception when REST call fails")
    void testRestCallFailure() {
        HCaptchaVerifier verifier = verifier();
        server.expect(requestTo(ENDPOINT)).andRespond(withServerError());

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify("valid-token"));
    }
}
