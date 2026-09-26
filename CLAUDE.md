# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot starter (library, not an app) for server-side captcha token verification with hCaptcha and mCaptcha. Targets Java 25 and Spring Boot 4 (Jackson 3, i.e. `tools.jackson.*` packages, not `com.fasterxml.jackson.*`). Maven coordinates: `com.moser-systems:captcha-spring-boot-starter`.

## Commands

```sh
mvn -B verify                                        # what CI runs: compile, test, source + javadoc jars
mvn test                                             # tests only
mvn test -Dtest=HCaptchaVerifierTest                 # single test class
mvn test -Dtest=HCaptchaVerifierTest#testVerifySuccessful   # single test method
```

The javadoc jar is built in the default lifecycle (not only on release), so public API without proper javadoc can break `mvn verify`.

Releases: pushing a `v*` tag triggers `.github/workflows/release.yml`, which sets the version from the tag and runs `mvn -P release deploy` (GPG signing + Maven Central). The `pom.xml` version stays at `-SNAPSHOT`.

## Architecture

- `CaptchaVerifier` is the public, provider-neutral interface (`verify`, `siteKey`, `tokenParameterName`, `provider`). Each provider lives in its own package (`hcaptcha`, `mcaptcha`) with a verifier implementation plus request/response records.
- `CaptchaProperties` is a single immutable record bound to `captcha.*`, with nested records per provider (`captcha.hcaptcha.*`, `captcha.mcaptcha.*`). Defaults are declared via `@DefaultValue`.
- `autoconfigure/CaptchaAutoConfiguration` (registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`) creates exactly one verifier bean chosen by `captcha.provider` (hCaptcha is the default via `matchIfMissing`). Everything backs off when `captcha.enabled=false` or the application defines its own `CaptchaVerifier`.
- Verifiers take a `RestClient` in their constructor. The auto-config uses an application-provided `RestClient.Builder` if present, otherwise `RestClient.builder()`.
- Failure contract: `verify` returns normally on success and throws `CaptchaVerificationException` for a missing token, missing configuration, transport errors, or a rejected token. It does not return a boolean.
- Provider differences: hCaptcha posts form-encoded data and supports `remoteip`, `sitekey` and an optional hostname check. mCaptcha posts JSON to `<url>/api/v1/pow/siteverify` on a self-hosted instance, ignores `remoteIp`, and exposes `widgetUrl()` for the frontend.

Adding a provider means adding a `CaptchaProvider` enum value, a nested properties record, a verifier package, a conditional `@Bean` in the auto-configuration, and a README section.

## Testing conventions

- Verifier tests build the verifier directly and bind `MockRestServiceServer` to a `RestClient.Builder`. They make no real HTTP calls.
- Auto-configuration tests use `ApplicationContextRunner` with `AutoConfigurations.of(CaptchaAutoConfiguration.class)` and property values.
