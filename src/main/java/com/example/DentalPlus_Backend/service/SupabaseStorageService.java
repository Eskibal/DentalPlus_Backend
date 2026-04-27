package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.config.SupabaseConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SupabaseStorageService {

    private final SupabaseConfig supabaseConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public SupabaseStorageService(SupabaseConfig supabaseConfig) {
        this.supabaseConfig = supabaseConfig;
    }

    public String uploadPdf(MultipartFile file, String folder) throws IOException {
        if (!isPdfValid(file)) {
            throw new IllegalArgumentException("Invalid PDF file");
        }

        String filePath = buildFilePath(folder, file.getOriginalFilename());

        String endpoint = supabaseConfig.getUrl()
                + "/storage/v1/object/"
                + supabaseConfig.getDocumentsBucket()
                + "/"
                + filePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseConfig.getKey());
        headers.setBearerAuth(supabaseConfig.getKey());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Supabase upload failed");
            }

            return filePath;
        } catch (RestClientException e) {
            throw new RuntimeException("Error uploading PDF to Supabase", e);
        }
    }

    public void deletePdf(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        String endpoint = supabaseConfig.getUrl()
                + "/storage/v1/object/"
                + supabaseConfig.getDocumentsBucket()
                + "/"
                + filePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseConfig.getKey());
        headers.setBearerAuth(supabaseConfig.getKey());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    endpoint,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );
        } catch (RestClientException e) {
            throw new RuntimeException("Error deleting PDF from Supabase", e);
        }
    }

    public static boolean isPdfValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && contentType.equalsIgnoreCase("application/pdf");
    }

    private String buildFilePath(String folder, String originalFilename) {
        String safeFolder = (folder == null || folder.isBlank()) ? "general" : folder.trim();
        String safeName = (originalFilename == null || originalFilename.isBlank())
                ? "document.pdf"
                : originalFilename.trim().replaceAll("\\s+", "_");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return safeFolder + "/" + timestamp + "_" + safeName;
    }
}