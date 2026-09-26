package com.mosersystems.captcha.hcaptcha;

import com.mosersystems.captcha.CaptchaProperties;
import com.mosersystems.captcha.CaptchaProvider;
import com.mosersystems.captcha.CaptchaVerificationException;
import com.mosersystems.captcha.CaptchaVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Verifies hCaptcha tokens against the hCaptcha siteverify endpoint.
 */
public class HCaptchaVerifier implements CaptchaVerifier {

    /** Form parameter the hCaptcha widget submits the token in. */
    public static final String TOKEN_PARAMETER = "h-captcha-response";

    private static final Logger log = LoggerFactory.getLogger(HCaptchaVerifier.class);

    private final CaptchaProperties.HCaptcha properties;
    private final RestClient restClient;

    /**
     * @param properties hCaptcha configuration
     * @param restClient client used to call the siteverify endpoint
     */
    public HCaptchaVerifier(CaptchaProperties.HCaptcha properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public CaptchaProvider provider() {
        return CaptchaProvider.HCAPTCHA;
    }

    @Override
    public String siteKey() {
        return properties.siteKey();
    }

    @Override
    public String tokenParameterName() {
        return TOKEN_PARAMETER;
    }

    @Override
    public void verify(String captchaToken, String remoteIp) {
        if (captchaToken == null || captchaToken.isBlank()) {
            throw new CaptchaVerificationException("Captcha token is missing");
        }
        if (!hasText(properties.secret())) {
            throw new CaptchaVerificationException("captcha.hcaptcha.secret is not configured");
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
            throw new CaptchaVerificationException("Error during captcha verification: " + e.getMessage(), e);
        }

        validateResponse(response);
        log.debug("Captcha verification successful");
    }

    private void validateResponse(HCaptchaVerificationResponse response) {
        if (response == null) {
            throw new CaptchaVerificationException("Captcha service returned null response");
        }

        if (!Boolean.TRUE.equals(response.success())) {
            String errorMessage = "Captcha verification failed";
            if (response.errorCodes() != null && !response.errorCodes().isEmpty()) {
                errorMessage += ": " + String.join(", ", response.errorCodes());
                log.warn("Captcha verification failed with error codes: {}", response.errorCodes());
            }
            throw new CaptchaVerificationException(errorMessage);
        }

        if (hasText(properties.hostname())) {
            validateHostname(response.hostname());
        }
    }

    private void validateHostname(String responseHostname) {
        if (!hasText(responseHostname)) {
            throw new CaptchaVerificationException("Captcha response missing hostname");
        }

        String expectedHostname = properties.hostname();
        if (!responseHostname.equals(expectedHostname)) {
            log.warn("Hostname mismatch: expected '{}', got '{}'", expectedHostname, responseHostname);
            throw new CaptchaVerificationException(
                    String.format("Hostname mismatch: expected '%s', got '%s'", expectedHostname, responseHostname)
            );
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
