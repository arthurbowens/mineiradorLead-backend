package com.leadmapspro.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LeadMapsProperties.class)
public class LeadMapsConfiguration {
}
