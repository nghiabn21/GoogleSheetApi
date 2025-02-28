//package com.GoogleSheetAPI.util;
//
//
//import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
//import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
//import java.awt.Desktop;
//import java.io.IOException;
//import java.net.URI;
//
//public class CustomAuthorizationApp extends AuthorizationCodeInstalledApp {
//    private String authUrl; // Biến để lưu URL
//
//    public CustomAuthorizationApp(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver) {
//        super(flow, receiver, new CustomBrowser()); // Dùng Browser tùy chỉnh
//    }
//
//    public String getAuthUrl() {
//        return authUrl;
//    }
//
//    @Override
//    protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
//        this.authUrl = authorizationUrl.build(); // Lưu URL thay vì hiển thị
//        super.onAuthorization(authorizationUrl);
//    }
//
////     Custom Browser để chặn URL mở trong trình duyệt
//    private static class CustomBrowser implements Browser {
//        @Override
//        public void browse(String url) throws IOException {
//            if (url == null) {
//                System.out.println("🚨 URL bị null!");
//            } else {
//                System.out.println("🔗 Captured OAuth URL: " + url);
//            }
//            // Nếu muốn mở trình duyệt tự động:
//            if (Desktop.isDesktopSupported()) {
//                Desktop.getDesktop().browse(URI.create(url));
//            }
//        }
//    }
//}
