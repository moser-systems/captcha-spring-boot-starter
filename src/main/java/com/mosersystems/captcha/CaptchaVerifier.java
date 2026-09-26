package com.mosersystems.captcha;

/**
 * Verifies captcha tokens submitted by clients.
 */
public interface CaptchaVerifier {

    /**
     * @return the provider this verifier talks to
     */
    CaptchaProvider provider();

    /**
     * @return the public site key used by the frontend widget
     */
    String siteKey();

    /**
     * @return name of the form parameter the frontend widget submits the token in
     */
    String tokenParameterName();

    /**
     * Verifies a token.
     *
     * @param captchaToken token submitted by the client
     * @throws CaptchaVerificationException if the token is missing or invalid, or verification failed
     */
    default void verify(String captchaToken) {
        verify(captchaToken, null);
    }

    /**
     * Verifies a token and passes the client's IP address to the provider, if it supports it.
     *
     * @param captchaToken token submitted by the client
     * @param remoteIp     IP address of the client, may be {@code null}
     * @throws CaptchaVerificationException if the token is missing or invalid, or verification failed
     */
    void verify(String captchaToken, String remoteIp);
}
