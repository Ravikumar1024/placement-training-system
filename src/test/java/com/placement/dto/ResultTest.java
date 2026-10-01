package com.placement.dto;

import com.placement.config.WebAppConfig;
import com.placement.entity.Student;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesOnlyCodeMessageAndDataForSuccess() throws Exception {
        JsonNode response = objectMapper.valueToTree(Result.success("api.success.read.students.list", List.of("student")));

        assertEquals(Set.of("code", "message", "data"), fieldNames(response));
        assertEquals("PTSS001", response.get("code").asText());
        assertEquals("Students retrieved successfully.", response.get("message").asText());
        assertEquals(1, response.get("data").size());
    }

    @Test
    void serializesOnlyCodeMessageAndDataForErrors() throws Exception {
        JsonNode response = objectMapper.valueToTree(Result.error(HttpStatus.CONFLICT.value(), "Delete dependent records first."));

        assertEquals(Set.of("code", "message", "data"), fieldNames(response));
        assertEquals("PTSE005", response.get("code").asText());
        assertEquals("Delete dependent records first.", response.get("message").asText());
        assertEquals(true, response.get("data").isNull());
    }

    @Test
    void classifiesMutationsAndAuthenticationWithSuccessCodes() {
        assertEquals("PTSS002", Result.success("api.success.write.student.created", null).getCode());
        assertEquals("PTSS003", Result.success("api.success.auth.login", null).getCode());
    }

    @Test
    void mapsCommonClientErrorsToStableApplicationCodes() {
        assertEquals("PTSE001", Result.badRequest("Invalid request body.").getCode());
        assertEquals("PTSE002", Result.unauthorized("Invalid username or password.").getCode());
    }

    @Test
    void resolvesLengthValidationMessagesFromApplicationBundle() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        LocalValidatorFactoryBean validator = (LocalValidatorFactoryBean) new WebAppConfig(messageSource).getValidator();
        validator.afterPropertiesSet();

        try {
            String message = validator.validateValue(Student.class, "name", "x").iterator().next().getMessage();
            assertEquals("Name must be between 2 and 100 characters.", message);
        } finally {
            validator.close();
        }
    }

    private Set<String> fieldNames(JsonNode response) {
        Set<String> names = new HashSet<>();
        response.fieldNames().forEachRemaining(names::add);
        return names;
    }
}