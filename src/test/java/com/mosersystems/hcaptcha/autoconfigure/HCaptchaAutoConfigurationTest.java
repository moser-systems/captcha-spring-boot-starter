package com.mosersystems.hcaptcha.autoconfigure;

import com.mosersystems.hcaptcha.HCaptchaProperties;
import com.mosersystems.hcaptcha.HCaptchaVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class HCaptchaAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HCaptchaAutoConfiguration.class));

    @Test
    void createsVerifierAndBindsProperties() {
        contextRunner
                .withPropertyValues("hcaptcha.site-key=site", "hcaptcha.secret=secret", "hcaptcha.hostname=example.com")
                .run(context -> {
                    assertThat(context).hasSingleBean(HCaptchaVerifier.class);
                    HCaptchaProperties properties = context.getBean(HCaptchaProperties.class);
                    assertThat(properties.enabled()).isTrue();
                    assertThat(properties.siteKey()).isEqualTo("site");
                    assertThat(properties.secret()).isEqualTo("secret");
                    assertThat(properties.hostname()).isEqualTo("example.com");
                    assertThat(properties.endpoint()).isEqualTo(HCaptchaProperties.DEFAULT_ENDPOINT);
                });
    }

    @Test
    void backsOffWhenDisabled() {
        contextRunner
                .withPropertyValues("hcaptcha.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(HCaptchaVerifier.class));
    }

    @Test
    void backsOffWhenUserDefinesVerifier() {
        HCaptchaVerifier custom = new HCaptchaVerifier(
                new HCaptchaProperties(true, null, "s", HCaptchaProperties.DEFAULT_ENDPOINT, null), RestClient.create());
        contextRunner
                .withBean(HCaptchaVerifier.class, () -> custom)
                .run(context -> assertThat(context.getBean(HCaptchaVerifier.class)).isSameAs(custom));
    }
}
