package com.swigg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TotpConfig {

    @Value("${totp.window:10}")
    private int window;

    @Value("${totp.time-step:30}")
    private int timeStep;

    @Value("${totp.length:6}")
    private int length;

    @Value("${totp.secret-length:32}")
    private int secretLength;

    @Value("${totp.algorithm:HmacSHA1}")
    private String algorithm;

    public int getWindow() {
        return window;
    }

    public int getTimeStep() {
        return timeStep;
    }

    public int getLength() {
        return length;
    }

    public int getSecretLength() {
        return secretLength;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
