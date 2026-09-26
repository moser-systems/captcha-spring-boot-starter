package com.mosersystems.captcha.mcaptcha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response of the mCaptcha siteverify endpoint.
 *
 * @param valid whether the token is valid
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MCaptchaVerificationResponse(Boolean valid) {
}
