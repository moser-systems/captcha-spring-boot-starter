package com.mosersystems.captcha.mcaptcha;

import com.mosersystems.captcha.CaptchaProperties;
import com.mosersystems.captcha.CaptchaVerificationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("MCaptchaVerifier Tests")
class MCaptchaVerifierTest {

    private static final String URL = "https://mcaptcha.example.com";
    private static final String ENDPOINT = URL + "/api/v1/pow/siteverify";

    private MockRestServiceServer server;

    private MCaptchaVerifier verifier(String url) {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        return new MCaptchaVerifier(new CaptchaProperties.MCaptcha(url, "site-key", "test-secret"), builder.build());
    }

    private MCaptchaVerifier verifier() {
        return verifier(URL);
    }

    @Test
    @DisplayName("Should send JSON POST request with token, key and secret")
    void testVerifySuccessful() {
        MCaptchaVerifier verifier = verifier();
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"token\":\"valid-token\",\"key\":\"site-key\",\"secret\":\"test-secret\"}", JsonCompareMode.STRICT))
                .andRespond(withSuccess("{\"valid\":true}", MediaType.APPLICATION_JSON));

        assertDoesNotThrow(() -> verifier.verify("valid-token"));
        server.verify();
    }

    @Test
    @DisplayName("Should handle trailing slash in instance URL")
    void testTrailingSlash() {
        MCaptchaVerifier verifier = verifier(URL + "/");
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("{\"valid\":true}", MediaType.APPLICATION_JSON));

        assertDoesNotThrow(() -> verifier.verify("valid-token"));
        assertEquals(URL + "/widget/?sitekey=site-key", verifier.widgetUrl());
    }

    @Test
    @DisplayName("Should throw exception when token is invalid")
    void testVerifyFailed() {
        MCaptchaVerifier verifier = verifier();
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("{\"valid\":false}", MediaType.APPLICATION_JSON));

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify("invalid-token"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should throw exception for null, empty or whitespace token")
    void testEmptyToken(String token) {
        MCaptchaVerifier verifier = verifier();

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify(token));
    }

    @Test
    @DisplayName("Should throw exception when not configured")
    void testNotConfigured() {
        MCaptchaVerifier verifier = new MCaptchaVerifier(
                new CaptchaProperties.MCaptcha(null, null, null), RestClient.create());

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify("valid-token"));
    }

    @Test
    @DisplayName("Should throw exception when response body is empty")
    void testEmptyResponseBody() {
        MCaptchaVerifier verifier = verifier();
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess());

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify("valid-token"));
    }

    @Test
    @DisplayName("Should throw exception when REST call fails")
    void testRestCallFailure() {
        MCaptchaVerifier verifier = verifier();
        server.expect(requestTo(ENDPOINT)).andRespond(withServerError());

        assertThrows(CaptchaVerificationException.class, () -> verifier.verify("valid-token"));
    }
}
