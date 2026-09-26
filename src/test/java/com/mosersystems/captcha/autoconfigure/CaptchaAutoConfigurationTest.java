package com.mosersystems.captcha.autoconfigure;

import com.mosersystems.captcha.CaptchaProperties;
import com.mosersystems.captcha.CaptchaProvider;
import com.mosersystems.captcha.CaptchaVerifier;
import com.mosersystems.captcha.hcaptcha.HCaptchaVerifier;
import com.mosersystems.captcha.mcaptcha.MCaptchaVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class CaptchaAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CaptchaAutoConfiguration.class));

    @Test
    void createsHCaptchaVerifierByDefault() {
        contextRunner
                .withPropertyValues(
                        "captcha.hcaptcha.site-key=site",
                        "captcha.hcaptcha.secret=secret",
                        "captcha.hcaptcha.hostname=example.com")
                .run(context -> {
                    assertThat(context).hasSingleBean(CaptchaVerifier.class);
                    assertThat(context).hasSingleBean(HCaptchaVerifier.class);
                    CaptchaProperties properties = context.getBean(CaptchaProperties.class);
                    assertThat(properties.enabled()).isTrue();
                    assertThat(properties.provider()).isEqualTo(CaptchaProvider.HCAPTCHA);
                    assertThat(properties.hcaptcha().siteKey()).isEqualTo("site");
                    assertThat(properties.hcaptcha().secret()).isEqualTo("secret");
                    assertThat(properties.hcaptcha().hostname()).isEqualTo("example.com");
                    assertThat(properties.hcaptcha().endpoint()).isEqualTo(CaptchaProperties.HCaptcha.DEFAULT_ENDPOINT);
                    assertThat(context.getBean(CaptchaVerifier.class).siteKey()).isEqualTo("site");
                });
    }

    @Test
    void createsMCaptchaVerifierWhenSelected() {
        contextRunner
                .withPropertyValues(
                        "captcha.provider=mcaptcha",
                        "captcha.mcaptcha.url=https://mcaptcha.example.com",
                        "captcha.mcaptcha.site-key=site",
                        "captcha.mcaptcha.secret=secret")
                .run(context -> {
                    assertThat(context).hasSingleBean(CaptchaVerifier.class);
                    MCaptchaVerifier verifier = context.getBean(MCaptchaVerifier.class);
                    assertThat(verifier.provider()).isEqualTo(CaptchaProvider.MCAPTCHA);
                    assertThat(verifier.tokenParameterName()).isEqualTo("mcaptcha__token");
                    assertThat(verifier.widgetUrl()).isEqualTo("https://mcaptcha.example.com/widget/?sitekey=site");
                });
    }

    @Test
    void backsOffWhenDisabled() {
        contextRunner
                .withPropertyValues("captcha.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(CaptchaVerifier.class));
    }

    @Test
    void backsOffWhenUserDefinesVerifier() {
        HCaptchaVerifier custom = new HCaptchaVerifier(
                new CaptchaProperties.HCaptcha(null, "s", CaptchaProperties.HCaptcha.DEFAULT_ENDPOINT, null),
                RestClient.create());
        contextRunner
                .withBean(CaptchaVerifier.class, () -> custom)
                .run(context -> assertThat(context.getBean(CaptchaVerifier.class)).isSameAs(custom));
    }
}
