package com.mosersystems.captcha.autoconfigure;

import com.mosersystems.captcha.CaptchaProperties;
import com.mosersystems.captcha.CaptchaVerifier;
import com.mosersystems.captcha.hcaptcha.HCaptchaVerifier;
import com.mosersystems.captcha.mcaptcha.MCaptchaVerifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Auto-configures a {@link CaptchaVerifier} for the provider selected by {@code captcha.provider},
 * unless {@code captcha.enabled=false}.
 */
@AutoConfiguration
@EnableConfigurationProperties(CaptchaProperties.class)
@ConditionalOnProperty(prefix = "captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CaptchaAutoConfiguration {

    /**
     * Creates the hCaptcha verifier, the default provider.
     *
     * @param properties         captcha configuration
     * @param restClientBuilders optional application-provided RestClient builder
     * @return the verifier
     */
    @Bean
    @ConditionalOnMissingBean(CaptchaVerifier.class)
    @ConditionalOnProperty(prefix = "captcha", name = "provider", havingValue = "hcaptcha", matchIfMissing = true)
    public HCaptchaVerifier hCaptchaVerifier(CaptchaProperties properties,
                                             ObjectProvider<RestClient.Builder> restClientBuilders) {
        return new HCaptchaVerifier(properties.hcaptcha(), restClient(restClientBuilders));
    }

    /**
     * Creates the mCaptcha verifier.
     *
     * @param properties         captcha configuration
     * @param restClientBuilders optional application-provided RestClient builder
     * @return the verifier
     */
    @Bean
    @ConditionalOnMissingBean(CaptchaVerifier.class)
    @ConditionalOnProperty(prefix = "captcha", name = "provider", havingValue = "mcaptcha")
    public MCaptchaVerifier mCaptchaVerifier(CaptchaProperties properties,
                                             ObjectProvider<RestClient.Builder> restClientBuilders) {
        return new MCaptchaVerifier(properties.mcaptcha(), restClient(restClientBuilders));
    }

    private static RestClient restClient(ObjectProvider<RestClient.Builder> restClientBuilders) {
        return restClientBuilders.getIfAvailable(RestClient::builder).build();
    }
}
