# Captcha Spring Boot Starter

Spring Boot auto-configuration for server-side captcha token verification. Supported providers:

- [hCaptcha](https://www.hcaptcha.com/)
- [mCaptcha](https://mcaptcha.org/) (open source, proof-of-work, self-hostable)

Requires Java 25 and Spring Boot 4.

## Installation

```xml
<dependency>
    <groupId>com.moser-systems</groupId>
    <artifactId>captcha-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Configuration

```yaml
captcha:
  enabled: true           # default true; set false to disable the verifier bean (e.g. in dev)
  provider: hcaptcha      # hcaptcha (default) or mcaptcha

  hcaptcha:
    site-key: ${HCAPTCHA_SITEKEY}
    secret: ${HCAPTCHA_SECRET}
    hostname: example.com # optional: reject tokens solved on another hostname
    # endpoint: https://api.hcaptcha.com/siteverify   (default)

  mcaptcha:
    url: https://mcaptcha.example.com   # your mCaptcha instance
    site-key: ${MCAPTCHA_SITEKEY}
    secret: ${MCAPTCHA_SECRET}          # mCaptcha dashboard > Settings > Secret
```

If the application provides a `RestClient.Builder` bean (e.g. via `spring-boot-starter-restclient`),
it is used to call the provider, so timeouts, proxies and observability configured there apply.

## Usage

Inject `CaptchaVerifier`; the auto-configured implementation matches `captcha.provider`:

```java
@PostMapping("/signup")
public String signup(HttpServletRequest request) {
    try {
        captchaVerifier.verify(request.getParameter(captchaVerifier.tokenParameterName()), request.getRemoteAddr());
    } catch (CaptchaVerificationException e) {
        // reject submission
    }
    // ...
}
```

`verify` throws `CaptchaVerificationException` if the token is missing or invalid, or the provider
can't be reached. For hCaptcha it also checks the hostname if `captcha.hcaptcha.hostname` is set.

To customize, define your own `CaptchaVerifier` bean; the auto-configured one backs off.

### Frontend: hCaptcha

The token is submitted as `h-captcha-response`.

```html
<script src="https://js.hcaptcha.com/1/api.js" async defer></script>
<div class="h-captcha" th:data-sitekey="${@hCaptchaVerifier.siteKey()}"></div>
```

### Frontend: mCaptcha

The token is submitted as `mcaptcha__token`. `MCaptchaVerifier.widgetUrl()` returns the widget URL
(`<instance>/widget/?sitekey=<site-key>`).

```html
<label th:attr="data-mcaptcha_url=${@mCaptchaVerifier.widgetUrl()}" for="mcaptcha__token">
    I'm not a robot
    <input type="text" name="mcaptcha__token" id="mcaptcha__token" required />
</label>
<div id="mcaptcha__widget-container"></div>
<script src="https://unpkg.com/@mcaptcha/vanilla-glue@0.1.0-rc2/dist/index.js"></script>
```

## Releasing

Releases are published to Maven Central by the `Release` GitHub workflow when a `v*` tag is pushed:

```
git tag v0.1.0 && git push origin v0.1.0
```

## License

Apache License 2.0
