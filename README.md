# hCaptcha Spring Boot Starter

Spring Boot auto-configuration for server-side [hCaptcha](https://www.hcaptcha.com/) token verification.

Requires Java 25 and Spring Boot 4.

## Installation

```xml
<dependency>
    <groupId>com.moser-systems</groupId>
    <artifactId>hcaptcha-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Configuration

```yaml
hcaptcha:
  enabled: true                   # default true; set false to disable the verifier bean (e.g. in dev)
  site-key: ${HCAPTCHA_SITEKEY}
  secret: ${HCAPTCHA_SECRET}
  hostname: example.com           # optional: reject tokens solved on another hostname
  # endpoint: https://api.hcaptcha.com/siteverify   (default)
```

If the application provides a `RestClient.Builder` bean (e.g. via `spring-boot-starter-restclient`),
it is used to call hCaptcha, so timeouts, proxies and observability configured there apply.

## Usage

Frontend (Thymeleaf example):

```html
<script src="https://js.hcaptcha.com/1/api.js" async defer></script>
<div class="h-captcha" th:data-sitekey="${@environment.getProperty('hcaptcha.site-key')}"></div>
```

Controller:

```java
@PostMapping("/signup")
public String signup(@RequestParam(name = "h-captcha-response", required = false) String token,
                     HttpServletRequest request) {
    try {
        hCaptchaVerifier.verify(token, request.getRemoteAddr());
    } catch (HCaptchaVerificationException e) {
        // reject submission
    }
    // ...
}
```

`verify` returns the `HCaptchaVerificationResponse` on success and throws `HCaptchaVerificationException`
if the token is missing or invalid, the hostname doesn't match, or hCaptcha can't be reached.

To customize, define your own `HCaptchaVerifier` bean; the auto-configured one backs off.

## Releasing

Releases are published to Maven Central by the `Release` GitHub workflow when a `v*` tag is pushed:

```
git tag v0.1.0 && git push origin v0.1.0
```

## License

Apache License 2.0
