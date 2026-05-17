package com.invoiceapp.notification;

import com.invoiceapp.auth.User;
import com.invoiceapp.auth.UserRepository;
import com.invoiceapp.company.Company;
import com.invoiceapp.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Firebase Cloud Messaging push service.
 *
 * In production, this would use Firebase Admin SDK to send actual push notifications.
 * For MVP, we log the notification and persist it in the notifications table.
 * The actual Firebase integration requires:
 * 1. firebase-service-account.json in the classpath
 * 2. FirebaseApp.initializeApp() at startup
 * 3. FirebaseMessaging.getInstance().send(message) per token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebasePushService {

    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    /**
     * Send a recurring invoice reminder push notification.
     * Creates a Notification record and attempts to send via FCM.
     */
    @Transactional
    public void sendRecurringReminder(UUID userId, UUID recurringInvoiceId,
                                       String clientName, String companyName) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User {} not found for push notification", userId);
            return;
        }

        String title = "Factura recurrente pendiente";
        String body = String.format("Tienes una factura recurrente para %s (%s) lista para generar. " +
                "Confirma o cancela en las próximas 24 horas.", clientName, companyName);
        String actionUrl = "/recurring-invoices/" + recurringInvoiceId + "/confirm";

        // Persist notification
        Notification notification = Notification.builder()
                .user(user)
                .type("recurring_reminder")
                .title(title)
                .body(body)
                .status("sent")
                .actionUrl(actionUrl)
                .sentAt(Instant.now())
                .build();
        notificationRepository.save(notification);

        // Send via FCM to all user devices
        List<FcmToken> tokens = fcmTokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            log.warn("No FCM tokens found for user {}. Notification saved but not pushed.", userId);
            return;
        }

        for (FcmToken fcmToken : tokens) {
            try {
                // TODO: Replace with actual Firebase send
                // Message message = Message.builder()
                //     .setToken(fcmToken.getToken())
                //     .setNotification(com.google.firebase.messaging.Notification.builder()
                //         .setTitle(title).setBody(body).build())
                //     .putData("action", "recurring_confirm")
                //     .putData("recurringInvoiceId", recurringInvoiceId.toString())
                //     .build();
                // FirebaseMessaging.getInstance().send(message);

                log.info("Push sent to device {} for user {} (recurring: {})",
                        fcmToken.getDeviceInfo(), userId, recurringInvoiceId);
            } catch (Exception e) {
                log.error("Failed to send push to token {}: {}", fcmToken.getToken(), e.getMessage());
            }
        }
    }

    /**
     * Register a new FCM token for push notifications.
     */
    @Transactional
    public void registerToken(UUID userId, String token, String deviceInfo) {
        if (fcmTokenRepository.existsByUserIdAndToken(userId, token)) {
            log.debug("FCM token already registered for user {}", userId);
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        FcmToken fcmToken = FcmToken.builder()
                .user(user)
                .token(token)
                .deviceInfo(deviceInfo)
                .build();
        fcmTokenRepository.save(fcmToken);
        log.info("FCM token registered for user {} (device: {})", userId, deviceInfo);
    }

    /**
     * Unregister an FCM token (e.g. on logout).
     */
    @Transactional
    public void unregisterToken(UUID userId, String token) {
        fcmTokenRepository.deleteByUserIdAndToken(userId, token);
    }
}
