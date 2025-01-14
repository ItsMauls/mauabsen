package com.mauabsen.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mauabsen.project.models.Attendances;
import com.mauabsen.project.models.Employees;
import com.mauabsen.project.models.EmployeeKycs;
import com.mauabsen.project.repositories.AttendanceRepository;
import com.mauabsen.project.repositories.EmployeeRepository;
import com.mauabsen.project.repositories.EmployeeKycRepository;
import com.mauabsen.project.dto.EmployeeKycDto;
import com.mauabsen.project.exceptions.NotFoundException;
import com.mauabsen.project.dto.AttendanceDto;
import com.mauabsen.project.services.FingerprintService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@Transactional
public class AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private EmployeeKycRepository employeeKycRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FingerprintService fingerprintService;

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

    private Employees getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return employeeRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    public AttendanceDto clockIn(String fingerprintTemplate, int matchScore) {
        // Get current logged in employee
        Employees employee = getCurrentEmployee();
        System.out.println("\n=== Clock In Attempt ===");
        System.out.println("Employee: " + employee.getUsername() + " (ID: " + employee.getId() + ")");
            
        EmployeeKycs kyc = employee.getEmployeeKycs();
        if (kyc == null || kyc.getFingerprint_id() == null) {
            System.out.println("Error: No fingerprint registered");
            throw new RuntimeException("No fingerprint registered for this employee");
        }

        // Check if already clocked in today
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (attendanceRepository.findByEmployeeIdAndDate(employee.getId().intValue(), today).isPresent()) {
            throw new RuntimeException("Already clocked in today");
        }

        // Create new attendance
        Attendances attendance = Attendances.builder()
            .employee_id(employee.getId().intValue())
            .time_in(LocalDateTime.now())
            .date(today)
            .status("PRESENT")
            .created_at(LocalDateTime.now())
            .build();

        AttendanceDto dto = AttendanceDto.fromEntity(attendanceRepository.save(attendance));
        dto.setMatchScore(matchScore);
        return dto;
    }

    public AttendanceDto clockOut(String fingerprintTemplate) {
        // Get current logged in employee
        Employees employee = getCurrentEmployee();
        System.out.println("\n=== Clock Out Attempt ===");
        System.out.println("Employee: " + employee.getUsername() + " (ID: " + employee.getId() + ")");
            
        EmployeeKycs kyc = employee.getEmployeeKycs();
        if (kyc == null || kyc.getFingerprint_id() == null) {
            System.out.println("Error: No fingerprint registered");
            throw new RuntimeException("No fingerprint registered for this employee");
        }

        // Verifikasi fingerprint
        int matchScore = matchFingerprint(
            fingerprintTemplate, 
            kyc.getFingerprint_id()
        );

        if (matchScore < 100) {
            System.out.println("Verification failed: Score too low (" + matchScore + ")");
            throw new RuntimeException("Fingerprint does not match. Score: " + matchScore);
        }

        // Get today's attendance
        LocalDateTime today = LocalDate.now().atStartOfDay();
        Attendances attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId().intValue(), today)
            .orElseThrow(() -> new RuntimeException("No clock-in record found"));

        // Update attendance
        attendance.setTime_out(LocalDateTime.now());
        attendance.setUpdated_at(LocalDateTime.now());
        
        AttendanceDto dto = AttendanceDto.fromEntity(attendanceRepository.save(attendance));
        dto.setMatchScore(matchScore);
        return dto;
    }

    private String generateFingerprintHash(String fingerprintTemplate) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Base64.getDecoder().decode(fingerprintTemplate));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate fingerprint hash", e);
        }
    }

    public List<Attendances> getEmployeeAttendances(Integer employeeId) {
        if (!employeeRepository.existsById(employeeId.longValue())) {
            throw new NotFoundException("Employee not found");
        }
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public List<AttendanceDto> getAttendancesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return attendanceRepository.findByDateBetween(startDate, endDate)
            .stream()
            .map(AttendanceDto::fromEntity)
            .collect(Collectors.toList());
    }

    public List<AttendanceDto> getAllAttendances() {
        return attendanceRepository.findAll()
            .stream()
            .map(AttendanceDto::fromEntity)
            .collect(Collectors.toList());
    }

    private int matchFingerprint(String scannedTemplate, String storedTemplate) {
        try {
            System.out.println("\n=== Fingerprint Matching ===");
            System.out.println("Template Lengths - Stored: " + storedTemplate.length() + 
                             ", Scanned: " + scannedTemplate.length());

            // Check if templates start with "Rk1SACAyM" (SecuGen format identifier)
            if (!scannedTemplate.startsWith("Rk1SACAyM") || !storedTemplate.startsWith("Rk1SACAyM")) {
                System.out.println("Invalid template format");
                return 0;
            }

            // Compare the actual fingerprint data (skip the header)
            String scannedData = scannedTemplate.substring(10);
            String storedData = storedTemplate.substring(10);

            // Calculate similarity score
            int matchScore = calculateSimilarity(scannedData, storedData);
            System.out.println("Match Score: " + matchScore);

            return matchScore;
        } catch (Exception e) {
            System.out.println("Error in matchFingerprint: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private int calculateSimilarity(String template1, String template2) {
        try {
            // Decode Base64 templates to get raw bytes
            byte[] data1 = Base64.getDecoder().decode(template1);
            byte[] data2 = Base64.getDecoder().decode(template2);

            // Compare byte arrays
            int matchingBytes = 0;
            int minLength = Math.min(data1.length, data2.length);
            
            for (int i = 0; i < minLength; i++) {
                if (data1[i] == data2[i]) {
                    matchingBytes++;
                }
            }

            // Calculate percentage match and scale to 0-200 range
            double matchPercentage = (double) matchingBytes / minLength;
            return (int) (matchPercentage * 200);
        } catch (Exception e) {
            System.out.println("Error calculating similarity: " + e.getMessage());
            return 0;
        }
    }
}
