package com.elfaddoui.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final HomeDefaults home = new HomeDefaults();
    private final Uploads uploads = new Uploads();
    private final Mail mail = new Mail();
    private final Admin admin = new Admin();
    private final Payment payment = new Payment();

    public HomeDefaults getHome() {
        return home;
    }

    public Uploads getUploads() {
        return uploads;
    }

    public Mail getMail() {
        return mail;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Payment getPayment() {
        return payment;
    }

    public static class HomeDefaults {
        private String locationLabel = "Tunis, Centre Ville";
        private String etaLabel = "Livraison en 35 min";

        public String getLocationLabel() {
            return locationLabel;
        }

        public void setLocationLabel(String locationLabel) {
            this.locationLabel = locationLabel;
        }

        public String getEtaLabel() {
            return etaLabel;
        }

        public void setEtaLabel(String etaLabel) {
            this.etaLabel = etaLabel;
        }
    }

    public static class Uploads {
        private String directory = "./uploads";
        private String publicBaseUrl = "http://localhost:8080/uploads";

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }
    }

    public static class Mail {
        private boolean enabled;
        private String from = "no-reply@elfaddoui.local";
        private String otpSubject = "Votre code OTP de reinitialisation";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getOtpSubject() {
            return otpSubject;
        }

        public void setOtpSubject(String otpSubject) {
            this.otpSubject = otpSubject;
        }
    }

    public static class Admin {
        private String fullName = "Store Admin";
        private String email = "";
        private String password = "";

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Payment {
        /**
         * Base URL of the checkout page that the mobile app opens in a browser (Safari/Chrome).
         * In dev, this should point to the backend mock page (e.g. http://127.0.0.1:8080/pay).
         * In prod, override to the publicly accessible checkout domain (e.g. https://checkout.elfaddoui.tn/pay).
         */
        private String checkoutBaseUrl = "http://127.0.0.1:8080/pay";

        public String getCheckoutBaseUrl() {
            return checkoutBaseUrl;
        }

        public void setCheckoutBaseUrl(String checkoutBaseUrl) {
            this.checkoutBaseUrl = checkoutBaseUrl;
        }
    }
}
