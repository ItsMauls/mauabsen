package com.mauabsen.project.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

@Service
public class FingerprintService {
    
    @Autowired
    private ObjectMapper objectMapper;

    private void disableSSLVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int matchFingerprint(String template1, String template2) {
        disableSSLVerification();
        try {
            // Log template yang akan dibandingkan
            System.out.println("\n=== Fingerprint Matching Details ===");
            System.out.println("Registered Template (first 100 chars): " + 
                (template2.length() > 100 ? template2.substring(0, 100) + "..." : template2));
            System.out.println("Scanned Template (first 100 chars): " + 
                (template1.length() > 100 ? template1.substring(0, 100) + "..." : template1));
            System.out.println("Template Lengths - Registered: " + template2.length() + 
                ", Scanned: " + template1.length());

            String uri = "https://localhost:8443/SGIMatchScore";
            String params = String.format("template1=%s&template2=%s&templateFormat=ISO",
                URLEncoder.encode(template1, "UTF-8"),
                URLEncoder.encode(template2, "UTF-8"));

            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = params.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    String response = br.readLine();
                    System.out.println("Raw SecuGen Response: " + response);
                    JsonNode jsonResponse = objectMapper.readTree(response);
                    
                    // Check for error code first
                    if (jsonResponse.has("ErrorCode")) {
                        int errorCode = jsonResponse.get("ErrorCode").asInt();
                        if (errorCode != 0) {
                            System.out.println("SecuGen Error Code: " + errorCode);
                            System.out.println("Error Description: " + getErrorDescription(errorCode));
                            System.out.println("================================\n");
                            return 0;
                        }
                    }

                    // If no error, get matching score
                    if (jsonResponse.has("MatchingScore")) {
                        int matchScore = jsonResponse.get("MatchingScore").asInt();
                        System.out.println("Match Score Result: " + matchScore);
                        System.out.println("Match Status: " + (matchScore >= 100 ? "MATCHED" : "NOT MATCHED"));
                        System.out.println("================================\n");
                        return matchScore;
                    }
                    
                    System.out.println("Invalid response format");
                    System.out.println("================================\n");
                    return 0;
                }
            }
            System.err.println("HTTP Error: " + conn.getResponseCode() + " " + conn.getResponseMessage());
            System.out.println("================================\n");
            return 0;
        } catch (Exception e) {
            System.err.println("Error matching fingerprint: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.out.println("================================\n");
            return 0;
        }
    }

    private String getErrorDescription(int errorCode) {
        switch (errorCode) {
            case 10001: return "Invalid template format";
            case 10002: return "Invalid template data";
            case 10003: return "Invalid template size";
            case 10004: return "Templates do not match format specifications";
            case 10005: return "Matching process failed";
            default: return "Unknown error";
        }
    }
} 