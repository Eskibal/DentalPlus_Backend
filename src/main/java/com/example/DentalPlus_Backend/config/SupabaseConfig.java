package com.example.DentalPlus_Backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String url;

    @Value("${supabase.key}")
    private String key;

    @Value("${supabase.bucket.documents}")
    private String documentsBucket;

    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }

    public String getDocumentsBucket() {
        return documentsBucket;
    }
}