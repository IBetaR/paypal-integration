package com.ibetar.paypalintegration.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpUtilHelpers {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Value("${prod.url}")
    private String productionUrl;

    @Value("${dev.url}")
    private String localUrl;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    public String getAppUrl() {
        LOGGER.info("Current active profile: {}", activeProfile);
        return activeProfile.equals("dev") ? localUrl : productionUrl;
    }

}