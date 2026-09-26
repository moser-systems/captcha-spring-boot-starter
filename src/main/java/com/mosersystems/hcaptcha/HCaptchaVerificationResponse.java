package com.mosersystems.hcaptcha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Response of the hCaptcha siteverify endpoint.
 *
 * @param success            whether the token is valid
 * @param errorCodes         error codes, if any
 * @param challengeTimestamp time the challenge was solved
 * @param hostname           hostname of the site where the challenge was solved
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HCaptchaVerificationResponse(
        Boolean success,
        @JsonProperty("error-codes") List<String> errorCodes,
        @JsonProperty("challenge_ts") Instant challengeTimestamp,
        String hostname
) {
}
