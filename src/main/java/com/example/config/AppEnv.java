package com.example.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * mp.config.profile は MicroProfile Config が起動時に1回解決する。
 * システムプロパティ、環境変数 MP_CONFIG_PROFILE、未設定なら prod の順。
 * 画面では #{appEnv.value} として CSS のライブラリ名に使う。
 */
@Named("appEnv")
@ApplicationScoped
public class AppEnv {

    @Inject
    @ConfigProperty(name = "mp.config.profile", defaultValue = "prod")
    String value;

    public String getValue() {
        return value;
    }
}
