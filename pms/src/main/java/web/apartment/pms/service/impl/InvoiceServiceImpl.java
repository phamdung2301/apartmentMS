package web.apartment.pms.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.apartment.pms.model.Contract;
import web.apartment.pms.model.Invoice;
import web.apartment.pms.model.Room;
import web.apartment.pms.model.UtilityIndex;
import web.apartment.pms.repository.ContractRepository;
import web.apartment.pms.repository.InvoiceRepository;
import web.apartment.pms.repository.RoomRepository;
import web.apartment.pms.repository.UtilityIndexRepository;
import web.apartment.pms.service.EmailService;
import web.apartment.pms.service.InvoiceService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final UtilityIndexRepository utilityIndexRepository;
    private final RoomRepository roomRepository;
    private final EmailService emailService;

    @Value("${pms.utility.price-per-kwh:3500}")
    private double pricePerKwh;

    @Value("${pms.utility.price-per-m3:15000}")
    private double pricePerM3;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              ContractRepository contractRepository,
                              UtilityIndexRepository utilityIndexRepository,
                              RoomRepository roomRepository,
                              EmailService emailService) {
        this.invoiceRepository = invoiceRepository;
        this.contractRepository = contractRepository;
        this.utilityIndexRepository = utilityIndexRepository;
        this.roomRepository = roomRepository;
        this.emailService = emailService;
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    @Override
    public List<Invoice> findByStatus(String status) {
        return invoiceRepository.findByStatus(status);
    }

    @Override
    public List<Invoice> findByRoomId(Long roomId) {
        return invoiceRepository.findByRoomIdOrderByYearDescMonthDesc(roomId);
    }

    @Override
    public List<Invoice> findByTenantId(Long tenantId) {
        return invoiceRepository.findByContractTenantIdOrderByYearDescMonthDesc(tenantId);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Override
    public Optional<Invoice> findByRoomIdAndMonthAndYear(Long roomId, Integer month, Integer year) {
        return invoiceRepository.findByRoomIdAndMonthAndYear(roomId, month, year);
    }

    @Override
    public Invoice generateInvoice(Long roomId, Integer month, Integer year) {
        Contract contract = contractRepository.findFirstByRoomIdAndStatus(roomId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("No active contract found for Room ID: " + roomId));

        UtilityIndex utilityIndex = utilityIndexRepository.findByRoomIdAndMonthAndYear(roomId, month, year)
                .orElseThrow(() -> new IllegalArgumentException("No utility index found for Room ID " + roomId + " in " + month + "/" + year));

        // Calculate amount
        BigDecimal roomPrice = contract.getRoom().getPrice();
        
        int elecConsumed = Math.max(0, utilityIndex.getNewElec() - utilityIndex.getOldElec());
        BigDecimal elecCost = BigDecimal.valueOf(elecConsumed * pricePerKwh);

        int waterConsumed = Math.max(0, utilityIndex.getNewWater() - utilityIndex.getOldWater());
        BigDecimal waterCost = BigDecimal.valueOf(waterConsumed * pricePerM3);

        BigDecimal totalAmount = roomPrice.add(elecCost).add(waterCost);

        // Check if invoice already exists
        Invoice invoice = invoiceRepository.findByRoomIdAndMonthAndYear(roomId, month, year)
                .orElseGet(() -> Invoice.builder()
                        .room(contract.getRoom())
                        .contract(contract)
                        .month(month)
                        .year(year)
                        .build());

        invoice.setTotalAmount(totalAmount);
        invoice.setStatus("UNPAID"); // Finalized status is UNPAID as per specifications
        invoice.setPaymentDate(null);
        
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Async Email Notification
        emailService.sendInvoiceEmail(savedInvoice, utilityIndex);

        return savedInvoice;
    }

    @Override
    public Invoice markAsPaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        invoice.setStatus("PAID");
        invoice.setPaymentDate(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    @Override
    public long countPendingInvoices() {
        return invoiceRepository.countByStatus("UNPAID");
    }

    @Override
    public BigDecimal calculatePendingAmount() {
        BigDecimal sum = invoiceRepository.sumTotalAmountByStatus("UNPAID");
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
