package com.mosersystems.captcha;

/**
 * Thrown when a captcha token could not be verified.
 */
public class CaptchaVerificationException extends RuntimeException {

    /**
     * @param message detail message
     */
    public CaptchaVerificationException(String message) {
        super(message);
    }

    /**
     * @param message detail message
     * @param cause   underlying cause
     */
    public CaptchaVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
