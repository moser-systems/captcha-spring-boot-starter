package com.mosersystems.captcha.mcaptcha;

import com.mosersystems.captcha.CaptchaProperties;
import com.mosersystems.captcha.CaptchaProvider;
import com.mosersystems.captcha.CaptchaVerificationException;
import com.mosersystems.captcha.CaptchaVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Verifies mCaptcha tokens against the siteverify endpoint of an mCaptcha instance.
 */
public class MCaptchaVerifier implements CaptchaVerifier {

    /** Form parameter the mCaptcha widget submits the token in. */
    public static final String TOKEN_PARAMETER = "mcaptcha__token";

    private static final String SITEVERIFY_PATH = "/api/v1/pow/siteverify";

    private static final Logger log = LoggerFactory.getLogger(MCaptchaVerifier.class);

    private final CaptchaProperties.MCaptcha properties;
    private final RestClient restClient;

    /**
     * @param properties mCaptcha configuration
     * @param restClient client used to call the siteverify endpoint
     */
    public MCaptchaVerifier(CaptchaProperties.MCaptcha properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public CaptchaProvider provider() {
        return CaptchaProvider.MCAPTCHA;
    }

    @Override
    public String siteKey() {
        return properties.siteKey();
    }

    @Override
    public String tokenParameterName() {
        return TOKEN_PARAMETER;
    }

    /**
     * @return URL of the widget, to be set as {@code data-mcaptcha_url} in the frontend
     */
    public String widgetUrl() {
        return baseUrl() + "/widget/?sitekey=" + properties.siteKey();
    }

    /**
     * Verifies a token. mCaptcha doesn't use the client's IP address, so {@code remoteIp} is ignored.
     */
    @Override
    public void verify(String captchaToken, String remoteIp) {
        if (captchaToken == null || captchaToken.isBlank()) {
            throw new CaptchaVerificationException("Captcha token is missing");
        }
        if (!hasText(properties.url()) || !hasText(properties.siteKey()) || !hasText(properties.secret())) {
            throw new CaptchaVerificationException(
                    "captcha.mcaptcha.url, captcha.mcaptcha.site-key and captcha.mcaptcha.secret must be configured");
        }

        MCaptchaVerificationResponse response;
        try {
            response = restClient.post()
                    .uri(baseUrl() + SITEVERIFY_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new MCaptchaVerificationRequest(captchaToken, properties.siteKey(), properties.secret()))
                    .retrieve()
                    .body(MCaptchaVerificationResponse.class);
        } catch (Exception e) {
            log.error("Error during captcha verification", e);
            throw new CaptchaVerificationException("Error during captcha verification: " + e.getMessage(), e);
        }

        if (response == null) {
            throw new CaptchaVerificationException("Captcha service returned null response");
        }
        if (!Boolean.TRUE.equals(response.valid())) {
            throw new CaptchaVerificationException("Captcha verification failed");
        }
        log.debug("Captcha verification successful");
    }

    private String baseUrl() {
        String url = properties.url();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
