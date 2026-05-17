package com.invoiceapp.recurring;

import com.invoiceapp.notification.FirebasePushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled tasks for recurring invoice lifecycle:
 * 1) Send push notifications for due recurring invoices
 * 2) Expire unconfirmed notifications after 24 hours
 *
 * RULE: Sin confirmación push = sin factura generada.
 * Si usuario no responde en 24hs → status "expired", NO se genera.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringScheduler {

    private final RecurringInvoiceRepository recurringRepository;
    private final FirebasePushService pushService;

    @Value("${app.recurring.expiration-hours:24}")
    private int expirationHours;

    /**
     * Runs every hour. Finds recurring invoices due for notification and sends push.
     */
    @Scheduled(cron = "0 0 * * * *") // every hour
    @Transactional
    public void sendDueNotifications() {
        Instant now = Instant.now();
        List<RecurringInvoice> dueList = recurringRepository.findDueForNotification(now);

        log.info("Recurring scheduler: {} invoices due for notification", dueList.size());

        for (RecurringInvoice ri : dueList) {
            try {
                pushService.sendRecurringReminder(
                        ri.getCompany().getUser().getId(),
                        ri.getId(),
                        ri.getClient().getName(),
                        ri.getCompany().getName()
                );
                ri.setLastNotificationStatus("sent");
                recurringRepository.save(ri);
                log.info("Push sent for recurring {} (client: {})", ri.getId(), ri.getClient().getName());
            } catch (Exception e) {
                log.error("Failed to send push for recurring {}: {}", ri.getId(), e.getMessage());
            }
        }
    }

    /**
     * Runs every hour. Expires notifications that were sent but not confirmed within 24 hours.
     * CRITICAL RULE: expired = NO invoice generated.
     */
    @Scheduled(cron = "0 30 * * * *") // every hour at :30
    @Transactional
    public void expireUnconfirmedNotifications() {
        Instant expiredBefore = Instant.now().minus(expirationHours, ChronoUnit.HOURS);
        List<RecurringInvoice> expiredList = recurringRepository.findExpiredNotifications(expiredBefore);

        log.info("Recurring scheduler: {} notifications expired", expiredList.size());

        for (RecurringInvoice ri : expiredList) {
            ri.setLastNotificationStatus("expired");
            // Do NOT generate invoice — this is the core rule
            // Advance to next cycle
            ri.setNextGenerationDate(
                    ri.getNextGenerationDate().plus(getFrequencyDays(ri.getFrequency()), ChronoUnit.DAYS)
            );
            recurringRepository.save(ri);
            log.info("Recurring {} expired — no invoice generated", ri.getId());
        }
    }

    private long getFrequencyDays(String frequency) {
        return switch (frequency) {
            case "weekly" -> 7;
            case "biweekly" -> 14;
            case "monthly" -> 30;
            case "annual" -> 365;
            default -> 30;
        };
    }
}
