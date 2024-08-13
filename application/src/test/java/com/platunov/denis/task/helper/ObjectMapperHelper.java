package com.platunov.denis.task.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObjectMapperHelper {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public String writeValue(@NonNull Object value) {
        return objectMapper.writeValueAsString(value);
    }

    @SneakyThrows
    public <T> T readValue(@NonNull String value, Class<T> clazz) {
        return objectMapper.readValue(value, clazz);
    }
}
