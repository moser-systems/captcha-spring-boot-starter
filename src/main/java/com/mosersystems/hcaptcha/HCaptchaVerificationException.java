package com.mosersystems.hcaptcha;

/**
 * Thrown when an hCaptcha token could not be verified.
 */
public class HCaptchaVerificationException extends RuntimeException {

    /**
     * @param message detail message
     */
    public HCaptchaVerificationException(String message) {
        super(message);
    }

    /**
     * @param message detail message
     * @param cause   underlying cause
     */
    public HCaptchaVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
