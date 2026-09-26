package com.mosersystems.captcha.mcaptcha;

/**
 * Request body of the mCaptcha siteverify endpoint.
 *
 * @param token  token submitted by the client
 * @param key    site key
 * @param secret account secret
 */
public record MCaptchaVerificationRequest(String token, String key, String secret) {
}
