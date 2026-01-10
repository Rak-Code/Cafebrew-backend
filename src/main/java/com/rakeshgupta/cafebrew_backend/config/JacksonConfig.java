package com.rakeshgupta.cafebrew_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Jackson configuration for consistent date/time serialization.
 * Serializes LocalDateTime with 'Z' suffix to indicate UTC timezone,
 * ensuring frontend can correctly parse and display times.
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter ISO_UTC_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // Serialize LocalDateTime with 'Z' suffix to indicate UTC
        javaTimeModule.addSerializer(LocalDateTime.class, 
            new LocalDateTimeSerializer(ISO_UTC_FORMATTER));
        
        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}
