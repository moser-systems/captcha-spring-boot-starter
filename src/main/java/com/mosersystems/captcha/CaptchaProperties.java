package com.mosersystems.captcha;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for captcha verification, bound to the {@code captcha.*} prefix.
 *
 * @param enabled  whether a {@link CaptchaVerifier} bean is auto-configured
 * @param provider which provider to use
 * @param hcaptcha hCaptcha settings, used if {@code provider=hcaptcha}
 * @param mcaptcha mCaptcha settings, used if {@code provider=mcaptcha}
 */
@ConfigurationProperties(prefix = "captcha")
public record CaptchaProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("hcaptcha") CaptchaProvider provider,
        @DefaultValue HCaptcha hcaptcha,
        @DefaultValue MCaptcha mcaptcha
) {

    /**
     * hCaptcha settings.
     *
     * @param siteKey  public site key, used by the frontend widget and sent along on verification
     * @param secret   secret key used to verify tokens server-side
     * @param endpoint siteverify endpoint URL
     * @param hostname expected hostname of the site where the captcha was solved; not checked if empty
     */
    public record HCaptcha(
            String siteKey,
            String secret,
            @DefaultValue(HCaptcha.DEFAULT_ENDPOINT) String endpoint,
            String hostname
    ) {
        /** Default hCaptcha siteverify endpoint. */
        public static final String DEFAULT_ENDPOINT = "https://api.hcaptcha.com/siteverify";
    }

    /**
     * mCaptcha settings.
     *
     * @param url     base URL of the mCaptcha instance, e.g. {@code https://mcaptcha.example.com}
     * @param siteKey site key of the captcha configured in the mCaptcha dashboard
     * @param secret  account secret (mCaptcha dashboard, Settings, Secret)
     */
    public record MCaptcha(
            String url,
            String siteKey,
            String secret
    ) {
    }
}
