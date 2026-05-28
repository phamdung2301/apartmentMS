package web.apartment.pms.service;

import web.apartment.pms.model.Invoice;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InvoiceService {
    List<Invoice> findAll();
    List<Invoice> findByStatus(String status);
    List<Invoice> findByRoomId(Long roomId);
    List<Invoice> findByTenantId(Long tenantId);
    Optional<Invoice> findById(Long id);
    Optional<Invoice> findByRoomIdAndMonthAndYear(Long roomId, Integer month, Integer year);
    
    Invoice generateInvoice(Long roomId, Integer month, Integer year);
    Invoice markAsPaid(Long invoiceId);
    
    long countPendingInvoices();
    BigDecimal calculatePendingAmount();
}
