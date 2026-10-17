package com.example.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

/**
 * application-${mp.config.profile}.properties を java.util.Properties で1回読む。
 * 同じキーの環境変数があれば、ファイルの値より優先する。
 * app.message の環境変数名は APP_MESSAGE。
 */
@Named("appProperties")
@ApplicationScoped
public class AppProperties {

    @Inject
    AppEnv appEnv;

    private String profileLabel;
    private String message;

    @PostConstruct
    void load() {
        String name = "application-" + appEnv.getValue() + ".properties";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IllegalStateException(name + " がクラスパスにありません");
            }
            Properties properties = new Properties();
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            profileLabel = value(properties, "app.profile.label");
            message = value(properties, "app.message");
        } catch (IOException e) {
            throw new UncheckedIOException(name + " を読めません", e);
        }
    }

    public String getProfileLabel() {
        return profileLabel;
    }

    public String getMessage() {
        return message;
    }

    private static String value(Properties properties, String key) {
        String envName = key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
        String env = System.getenv(envName);
        if (env != null) {
            return env;
        }
        return properties.getProperty(key);
    }
}
