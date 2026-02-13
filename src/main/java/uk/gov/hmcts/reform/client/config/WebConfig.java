package uk.gov.hmcts.reform.client.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        registry.addConverter(new JsonStringToMapConverter());
    }


    private final class JsonStringToMapConverter implements Converter<String, Map<String, String>> {
        @Override
        public Map<String, String> convert(String input) {
            try {
                return objectMapper.readValue(input, new TypeReference<Map<String, String>>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to convert json to map", e);
            }
        }
    }
}
