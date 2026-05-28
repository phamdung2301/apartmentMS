package web.apartment.pms.service;

import web.apartment.pms.model.Invoice;
import web.apartment.pms.model.UtilityIndex;

public interface EmailService {
    void sendInvoiceEmail(Invoice invoice, UtilityIndex utilityIndex);
    void sendDebtReminderEmail(Invoice invoice);
}
