package com.example.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * mp.config.profile で選ばれた MicroProfile Config の値。
 */
@Named("mpConfig")
@ApplicationScoped
public class MpConfigValues {

    @Inject
    @ConfigProperty(name = "mp.config.profile", defaultValue = "prod")
    String profile;

    @Inject
    @ConfigProperty(name = "mp.profile.label")
    String profileLabel;

    @Inject
    @ConfigProperty(name = "mp.message")
    String message;

    public String getProfile() {
        return profile;
    }

    public String getProfileLabel() {
        return profileLabel;
    }

    public String getMessage() {
        return message;
    }
}
