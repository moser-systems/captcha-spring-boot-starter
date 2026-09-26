package com.mosersystems.hcaptcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Verifies hCaptcha tokens against the hCaptcha siteverify endpoint.
 */
public class HCaptchaVerifier {

    private static final Logger log = LoggerFactory.getLogger(HCaptchaVerifier.class);

    private final HCaptchaProperties properties;
    private final RestClient restClient;

    /**
     * @param properties hCaptcha configuration
     * @param restClient client used to call the siteverify endpoint
     */
    public HCaptchaVerifier(HCaptchaProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    /**
     * Verifies a token, e.g. the {@code h-captcha-response} form parameter.
     *
     * @param captchaToken token submitted by the client
     * @return the successful verification response
     * @throws HCaptchaVerificationException if the token is missing or invalid, or verification failed
     */
    public HCaptchaVerificationResponse verify(String captchaToken) {
        return verify(captchaToken, null);
    }

    /**
     * Verifies a token and passes the client's IP address to hCaptcha as an additional signal.
     *
     * @param captchaToken token submitted by the client
     * @param remoteIp     IP address of the client, may be {@code null}
     * @return the successful verification response
     * @throws HCaptchaVerificationException if the token is missing or invalid, or verification failed
     */
    public HCaptchaVerificationResponse verify(String captchaToken, String remoteIp) {
        if (captchaToken == null || captchaToken.isBlank()) {
            throw new HCaptchaVerificationException("Captcha token is missing");
        }
        if (properties.secret() == null || properties.secret().isBlank()) {
            throw new HCaptchaVerificationException("hcaptcha.secret is not configured");
        }

        HCaptchaVerificationResponse response;
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", properties.secret());
            body.add("response", captchaToken);
            if (hasText(properties.siteKey())) {
                body.add("sitekey", properties.siteKey());
            }
            if (hasText(remoteIp)) {
                body.add("remoteip", remoteIp);
            }

            response = restClient.post()
                    .uri(properties.endpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(HCaptchaVerificationResponse.class);
        } catch (Exception e) {
            log.error("Error during captcha verification", e);
            throw new HCaptchaVerificationException("Error during captcha verification: " + e.getMessage(), e);
        }

        validateResponse(response);
        log.debug("Captcha verification successful");
        return response;
    }

    private void validateResponse(HCaptchaVerificationResponse response) {
        if (response == null) {
            throw new HCaptchaVerificationException("Captcha service returned null response");
        }

        if (!Boolean.TRUE.equals(response.success())) {
            String errorMessage = "Captcha verification failed";
            if (response.errorCodes() != null && !response.errorCodes().isEmpty()) {
                errorMessage += ": " + String.join(", ", response.errorCodes());
                log.warn("Captcha verification failed with error codes: {}", response.errorCodes());
            }
            throw new HCaptchaVerificationException(errorMessage);
        }

        if (hasText(properties.hostname())) {
            validateHostname(response.hostname());
        }
    }

    private void validateHostname(String responseHostname) {
        if (!hasText(responseHostname)) {
            throw new HCaptchaVerificationException("Captcha response missing hostname");
        }

        String expectedHostname = properties.hostname();
        if (!responseHostname.equals(expectedHostname)) {
            log.warn("Hostname mismatch: expected '{}', got '{}'", expectedHostname, responseHostname);
            throw new HCaptchaVerificationException(
                    String.format("Hostname mismatch: expected '%s', got '%s'", expectedHostname, responseHostname)
            );
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
