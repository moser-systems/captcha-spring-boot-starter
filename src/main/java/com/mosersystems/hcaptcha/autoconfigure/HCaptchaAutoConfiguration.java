package com.mosersystems.hcaptcha.autoconfigure;

import com.mosersystems.hcaptcha.HCaptchaProperties;
import com.mosersystems.hcaptcha.HCaptchaVerifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Auto-configures an {@link HCaptchaVerifier} unless {@code hcaptcha.enabled=false}.
 */
@AutoConfiguration
@EnableConfigurationProperties(HCaptchaProperties.class)
@ConditionalOnProperty(prefix = "hcaptcha", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HCaptchaAutoConfiguration {

    /**
     * Creates the verifier, using the application's {@link RestClient.Builder} if one is available.
     *
     * @param properties         hCaptcha configuration
     * @param restClientBuilders optional application-provided RestClient builder
     * @return the verifier
     */
    @Bean
    @ConditionalOnMissingBean
    public HCaptchaVerifier hCaptchaVerifier(HCaptchaProperties properties,
                                             ObjectProvider<RestClient.Builder> restClientBuilders) {
        RestClient.Builder builder = restClientBuilders.getIfAvailable(RestClient::builder);
        return new HCaptchaVerifier(properties, builder.build());
    }
}
