package com.example.web;

import com.example.config.AppEnv;
import com.example.config.AppProperties;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("")
public class HomeServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(HomeServlet.class);

    @Inject
    AppEnv appEnv;

    @Inject
    AppProperties appProperties;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String envName = appEnv.getValue();
        log.debug("app-env-log-check level=DEBUG appEnv={} profileLabel={} appMessage={}",
                envName, appProperties.getProfileLabel(), appProperties.getMessage());
        log.info("app-env-log-check level=INFO appEnv={} profileLabel={} appMessage={}",
                envName, appProperties.getProfileLabel(), appProperties.getMessage());

        org.apache.logging.log4j.Level rootLevel = org.apache.logging.log4j.LogManager.getRootLogger().getLevel();

        req.setAttribute("sysProp", System.getProperty("APP_ENV"));
        req.setAttribute("envVar", System.getenv("APP_ENV"));
        req.setAttribute("mpProfileSysProp", System.getProperty("mp.config.profile"));
        req.setAttribute("mpProfileEnvVar", System.getenv("MP_CONFIG_PROFILE"));
        req.setAttribute("labelEnvVar", System.getenv("APP_PROFILE_LABEL"));
        req.setAttribute("messageEnvVar", System.getenv("APP_MESSAGE"));
        req.setAttribute("debugEnabled", log.isDebugEnabled());
        req.setAttribute("rootLevel", rootLevel == null ? "" : rootLevel.name());
        req.getRequestDispatcher("/index.xhtml").forward(req, resp);
    }
}
