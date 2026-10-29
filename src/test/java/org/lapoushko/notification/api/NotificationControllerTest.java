package org.lapoushko.notification.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.lapoushko.notification.application.InvalidNotificationException;
import org.lapoushko.notification.application.NotificationService;
import org.lapoushko.notification.domain.DataConstants;
import org.lapoushko.notification.domain.model.Attachment;
import org.lapoushko.notification.domain.model.Notification;
import org.lapoushko.notification.domain.model.NotificationType;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@Import(NotificationControllerTest.FixedClockConfig.class)
class NotificationControllerTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");
    private static final UUID RECIPIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final String VALID_REQUEST = """
            {
              "recipientId": "22222222-2222-2222-2222-222222222222",
              "recipientMail": "user@example.com",
              "recipientPhoneNumber": "+79991234567",
              "notificationTypes": [1, 2],
              "content": "Ваш заказ отправлен",
              "attachments": [
                {"fileName": "invoice.pdf", "fileUri": "https://files/invoice.pdf", "contentType": "application/pdf"}
              ]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void sendNotification_returnsIdOfCreatedNotification() throws Exception {
        String body = mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).sendNotification(captor.capture());
        Notification sent = captor.getValue();
        assertThat(body).isEqualTo("\"" + sent.getId() + "\"");
        assertThat(sent.getSenderId()).isEqualTo(DataConstants.ADMIN_USER_ID);
        assertThat(sent.getRecipientId()).isEqualTo(RECIPIENT_ID);
        assertThat(sent.getNotificationTypes()).containsExactly(NotificationType.EMAIL, NotificationType.SMS);
        assertThat(sent.getAttachments())
                .containsExactly(new Attachment("invoice.pdf", "https://files/invoice.pdf", "application/pdf"));
        assertThat(sent.getDateOfCreate()).isEqualTo(NOW);
    }

    @Test
    void sendNotification_returnsValidationErrorsPerField() throws Exception {
        String request = """
                {
                  "recipientId": "22222222-2222-2222-2222-222222222222",
                  "recipientMail": "not-an-email",
                  "notificationTypes": [],
                  "content": ""
                }
                """;

        mockMvc.perform(post("/api/v1/notifications").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.recipientMail", hasItem("Не валидная почта.")))
                .andExpect(jsonPath("$.errors.notificationTypes").isArray())
                .andExpect(jsonPath("$.errors.content").isArray());

        verifyNoInteractions(notificationService);
    }

    @Test
    void sendNotification_rejectsUnknownNotificationType() throws Exception {
        String request = VALID_REQUEST.replace("[1, 2]", "[3]");

        mockMvc.perform(post("/api/v1/notifications").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationService);
    }

    @Test
    void sendNotification_mapsInvalidNotificationToBadRequest() throws Exception {
        doThrow(new InvalidNotificationException("Поле recipientMail обязательно"))
                .when(notificationService).sendNotification(any());

        mockMvc.perform(post("/api/v1/notifications").contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Поле recipientMail обязательно"));
    }

    @Test
    void sendNotification_mapsBrokerFailureToServiceUnavailable() throws Exception {
        doThrow(new AmqpConnectException(new RuntimeException("connection refused")))
                .when(notificationService).sendNotification(any());

        mockMvc.perform(post("/api/v1/notifications").contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getNotifications_returnsNoContentWhenEmpty() throws Exception {
        when(notificationService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications")).andExpect(status().isNoContent());
    }

    @Test
    void getNotifications_returnsNotificationsWithNumericTypes() throws Exception {
        Notification notification = notification();
        when(notificationService.getAll()).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notification.getId().toString()))
                .andExpect(jsonPath("$[0].notificationTypes[0]").value(1))
                .andExpect(jsonPath("$[0].notificationTypes[1]").value(2))
                .andExpect(jsonPath("$[0].attachments[0].fileName").value("invoice.pdf"))
                .andExpect(jsonPath("$[0].dateOfCreate").value("2026-01-15T10:00:00Z"));
    }

    @Test
    void getNotificationById_returnsNoContentWhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(notificationService.getById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/notifications/{id}", id)).andExpect(status().isNoContent());
    }

    @Test
    void getNotificationById_returnsNotification() throws Exception {
        Notification notification = notification();
        when(notificationService.getById(notification.getId())).thenReturn(Optional.of(notification));

        mockMvc.perform(get("/api/v1/notifications/{id}", notification.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientId").value(RECIPIENT_ID.toString()))
                .andExpect(jsonPath("$.content").value("Ваш заказ отправлен"));
    }

    @Test
    void getNotificationWithStatus_returnsStatusPerChannel() throws Exception {
        Notification notification = notification();
        when(notificationService.getById(notification.getId())).thenReturn(Optional.of(notification));
        when(notificationService.getStatuses(notification.getId(), notification.getNotificationTypes()))
                .thenReturn(Map.of(NotificationType.EMAIL, 3));

        mockMvc.perform(get("/api/v1/notifications/{id}/status", notification.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statuses[0].type").value(1))
                .andExpect(jsonPath("$.statuses[0].status").value(3))
                .andExpect(jsonPath("$.statuses[1].type").value(2))
                .andExpect(jsonPath("$.statuses[1].status").value(0));
    }

    @Test
    void getNotificationWithStatus_returnsNoContentWhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(notificationService.getById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/notifications/{id}/status", id)).andExpect(status().isNoContent());
    }

    private static Notification notification() {
        return new Notification(
                DataConstants.ADMIN_USER_ID,
                RECIPIENT_ID,
                "user@example.com",
                "+79991234567",
                null,
                List.of(NotificationType.EMAIL, NotificationType.SMS),
                "Ваш заказ отправлен",
                List.of(new Attachment("invoice.pdf", "https://files/invoice.pdf", "application/pdf")),
                NOW);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
