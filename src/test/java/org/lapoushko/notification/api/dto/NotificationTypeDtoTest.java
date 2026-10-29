package org.lapoushko.notification.api.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NotificationTypeDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesAsNumericCode() throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(List.of(
                NotificationTypeDto.UNKNOWN, NotificationTypeDto.EMAIL, NotificationTypeDto.SMS, NotificationTypeDto.PUSH));

        assertThat(json).isEqualTo("[0,1,2,4]");
    }

    @ParameterizedTest
    @ValueSource(strings = {"4", "\"4\"", "\"Push\"", "\"PUSH\""})
    void deserializesFromCodeOrName(String json) throws JsonProcessingException {
        assertThat(objectMapper.readValue(json, NotificationTypeDto.class)).isEqualTo(NotificationTypeDto.PUSH);
    }

    @ParameterizedTest
    @ValueSource(strings = {"3", "\"Fax\""})
    void rejectsUnknownValue(String json) {
        assertThatThrownBy(() -> objectMapper.readValue(json, NotificationTypeDto.class))
                .isInstanceOf(JsonProcessingException.class)
                .hasMessageContaining("Неизвестный тип уведомления");
    }
}
