package web.apartment.pms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import web.apartment.pms.model.Invoice;
import web.apartment.pms.repository.InvoiceRepository;

import java.util.List;

@Service
@Slf4j
public class SchedulerService {

    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;

    public SchedulerService(InvoiceRepository invoiceRepository, EmailService emailService) {
        this.invoiceRepository = invoiceRepository;
        this.emailService = emailService;
    }

    // Cron job triggers at 00:00 on the 5th of every month.
    // Configuration key is 'pms.scheduler.cron'
    @Scheduled(cron = "${pms.scheduler.cron:0 0 0 5 * *}")
    public void runDebtReminderJob() {
        log.info("Starting scheduled debt reminder cron job...");
        
        List<Invoice> unpaidInvoices = invoiceRepository.findByStatus("UNPAID");
        log.info("Found {} unpaid invoices to remind.", unpaidInvoices.size());

        for (Invoice invoice : unpaidInvoices) {
            try {
                emailService.sendDebtReminderEmail(invoice);
            } catch (Exception e) {
                log.error("Failed to send debt reminder email for Invoice ID: {}", invoice.getId(), e);
            }
        }
        
        log.info("Scheduled debt reminder cron job completed.");
    }
}
