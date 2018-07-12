package com.xplmc.example.esdatafeeder.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * system common configuration
 *
 * @author luke
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class MyConfiguration {

    private static final String DEFAULT_AUDITOR = "luke";

    /**
     * default auditor luke
     *
     * @return return default auditor
     */
    @Bean("auditorAware")
    public AuditorAware auditorAware() {
        return () -> Optional.of(DEFAULT_AUDITOR);
    }

}
