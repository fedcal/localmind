package com.localmind.infrastructure.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "localmind.email")
public class EmailProperties {

    private boolean enabled = false;
    private ImapProperties imap = new ImapProperties();
    private SmtpProperties smtp = new SmtpProperties();
    private String username;
    private String password;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ImapProperties getImap() {
        return imap;
    }

    public void setImap(ImapProperties imap) {
        this.imap = imap;
    }

    public SmtpProperties getSmtp() {
        return smtp;
    }

    public void setSmtp(SmtpProperties smtp) {
        this.smtp = smtp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static class ImapProperties {
        private String host;
        private int port = 993;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class SmtpProperties {
        private String host;
        private int port = 587;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
