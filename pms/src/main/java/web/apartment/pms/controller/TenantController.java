package web.apartment.pms.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import web.apartment.pms.model.*;
import web.apartment.pms.repository.UserRepository;
import web.apartment.pms.service.ContractService;
import web.apartment.pms.service.InvoiceService;
import web.apartment.pms.service.IssueReportService;
import web.apartment.pms.service.RoomService;
import web.apartment.pms.service.UtilityIndexService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tenant")
public class TenantController {

    private final UserRepository userRepository;
    private final ContractService contractService;
    private final InvoiceService invoiceService;
    private final IssueReportService issueReportService;
    private final UtilityIndexService utilityIndexService;
    
    @Value("${pms.vietqr.bank-id:MB}")
    private String bankId;

    @Value("${pms.vietqr.account-no:0999999999}")
    private String accountNo;

    @Value("${pms.vietqr.account-name:NGUYEN VAN A}")
    private String accountName;

    public TenantController(UserRepository userRepository,
                            ContractService contractService,
                            InvoiceService invoiceService,
                            IssueReportService issueReportService,
                            UtilityIndexService utilityIndexService) {
        this.userRepository = userRepository;
        this.contractService = contractService;
        this.invoiceService = invoiceService;
        this.issueReportService = issueReportService;
        this.utilityIndexService = utilityIndexService;
    }

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User tenant = getLoggedInUser();
        model.addAttribute("tenant", tenant);

        Optional<Contract> contractOpt = contractService.findActiveByTenantId(tenant.getId());
        
        if (contractOpt.isPresent()) {
            Contract contract = contractOpt.get();
            Room room = contract.getRoom();
            model.addAttribute("contract", contract);
            model.addAttribute("room", room);

            // Fetch Invoices
            List<Invoice> invoices = invoiceService.findByTenantId(tenant.getId());
            model.addAttribute("invoices", invoices);

            // Fetch Issue Reports
            List<IssueReport> issues = issueReportService.findByTenantId(tenant.getId());
            model.addAttribute("issues", issues);

            // Fetch real utility consumption history for the chart
            List<UtilityIndex> utilityHistory = utilityIndexService.findHistoryByRoomId(room.getId());
            model.addAttribute("utilityHistory", utilityHistory);

            model.addAttribute("noActiveContract", false);
        } else {
            model.addAttribute("noActiveContract", true);
        }

        return "tenant/dashboard";
    }

    @PostMapping("/issues/report")
    public String reportIssue(@RequestParam String description) {
        User tenant = getLoggedInUser();
        Optional<Contract> contractOpt = contractService.findActiveByTenantId(tenant.getId());
        
        if (contractOpt.isPresent()) {
            Contract contract = contractOpt.get();
            issueReportService.createIssueReport(contract.getRoom().getId(), tenant.getId(), description);
        }
        
        return "redirect:/tenant/dashboard?reportSuccess=true";
    }

    @GetMapping("/payment-qr/{invoiceId}")
    @ResponseBody
    public String getPaymentQrUrl(@PathVariable Long invoiceId) {
        Invoice invoice = invoiceService.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        
        // Format of VietQR dynamic URL:
        // https://img.vietqr.io/image/<BANK_ID>-<ACCOUNT_NO>-compact.png?amount=<AMOUNT>&addInfo=<INFO>&accountName=<NAME>
        
        String roomNo = invoice.getRoom().getRoomNumber();
        String addInfo = String.format("PHONG%s_TIENPHONG_THANG%d", roomNo, invoice.getMonth());
        
        // Encode URL parameters
        String encodedName = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        String encodedInfo = URLEncoder.encode(addInfo, StandardCharsets.UTF_8);
        
        return String.format(
            "https://img.vietqr.io/image/%s-%s-compact.png?amount=%s&addInfo=%s&accountName=%s",
            bankId, accountNo, invoice.getTotalAmount().toPlainString(), encodedInfo, encodedName
        );
    }
}
