package com.danielabirzan.storemanagement.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityCredentials(Credentials user, Credentials admin) {
    public record Credentials(String username, String password) {}
}