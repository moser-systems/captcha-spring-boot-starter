package com.mosersystems.hcaptcha;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for hCaptcha, bound to the {@code hcaptcha.*} prefix.
 *
 * @param enabled  whether the {@link HCaptchaVerifier} bean is auto-configured
 * @param siteKey  public site key, used by the frontend widget and sent along on verification
 * @param secret   secret key used to verify tokens server-side
 * @param endpoint siteverify endpoint URL
 * @param hostname expected hostname of the site where the captcha was solved; not checked if empty
 */
@ConfigurationProperties(prefix = "hcaptcha")
public record HCaptchaProperties(
        @DefaultValue("true") boolean enabled,
        String siteKey,
        String secret,
        @DefaultValue(HCaptchaProperties.DEFAULT_ENDPOINT) String endpoint,
        String hostname
) {

    /** Default hCaptcha siteverify endpoint. */
    public static final String DEFAULT_ENDPOINT = "https://api.hcaptcha.com/siteverify";
}
