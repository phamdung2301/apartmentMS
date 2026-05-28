package web.apartment.pms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import web.apartment.pms.model.*;
import web.apartment.pms.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RoomService roomService;
    private final ContractService contractService;
    private final UtilityIndexService utilityIndexService;
    private final InvoiceService invoiceService;
    private final IssueReportService issueReportService;

    public AdminController(RoomService roomService,
                           ContractService contractService,
                           UtilityIndexService utilityIndexService,
                           InvoiceService invoiceService,
                           IssueReportService issueReportService) {
        this.roomService = roomService;
        this.contractService = contractService;
        this.utilityIndexService = utilityIndexService;
        this.invoiceService = invoiceService;
        this.issueReportService = issueReportService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, 
                            @RequestParam(value = "month", required = false) Integer month,
                            @RequestParam(value = "year", required = false) Integer year) {
        
        LocalDate now = LocalDate.now();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        int targetYear = (year != null) ? year : now.getYear();

        // Calculate stats
        long totalRooms = roomService.countTotal();
        long rentedRooms = roomService.countRented();
        long emptyRooms = roomService.countEmpty();
        long pendingInvoices = invoiceService.countPendingInvoices();
        BigDecimal pendingAmount = invoiceService.calculatePendingAmount();

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("rentedRooms", rentedRooms);
        model.addAttribute("emptyRooms", emptyRooms);
        model.addAttribute("pendingInvoices", pendingInvoices);
        model.addAttribute("pendingAmount", pendingAmount);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("targetYear", targetYear);

        // Build grid data for the month
        List<Room> rooms = roomService.findAll();
        List<UtilityInputRow> gridRows = new ArrayList<>();

        for (Room room : rooms) {
            String tenantName = "Trống";
            Optional<Contract> activeContractOpt = contractService.findActiveByRoomId(room.getId());
            if (activeContractOpt.isPresent()) {
                tenantName = activeContractOpt.get().getTenant().getFullName();
            }

            // Find current month's reading
            Optional<UtilityIndex> currentReadingOpt = utilityIndexService.findByRoomIdAndMonthAndYear(room.getId(), targetMonth, targetYear);
            
            // Find previous reading for old readings fallback
            int oldElec = 0;
            int oldWater = 0;
            int newElec = 0;
            int newWater = 0;
            boolean hasCurrentReading = currentReadingOpt.isPresent();

            if (hasCurrentReading) {
                UtilityIndex curr = currentReadingOpt.get();
                oldElec = curr.getOldElec();
                newElec = curr.getNewElec();
                oldWater = curr.getOldWater();
                newWater = curr.getNewWater();
            } else {
                // Query the latest overall reading to use as the old reading for this new month
                Optional<UtilityIndex> lastReadingOpt = utilityIndexService.findLatestByRoomId(room.getId());
                if (lastReadingOpt.isPresent()) {
                    oldElec = lastReadingOpt.get().getNewElec();
                    oldWater = lastReadingOpt.get().getNewWater();
                }
            }

            gridRows.add(UtilityInputRow.builder()
                    .roomId(room.getId())
                    .roomNumber(room.getRoomNumber())
                    .tenantName(tenantName)
                    .oldElec(oldElec)
                    .newElec(newElec > 0 ? newElec : null)
                    .oldWater(oldWater)
                    .newWater(newWater > 0 ? newWater : null)
                    .isRented(room.getStatus().equals("RENTED"))
                    .isSaved(hasCurrentReading)
                    .build());
        }

        model.addAttribute("gridRows", gridRows);

        // Count how many unsaved/unbilled rows we can finalize
        // Wait, unsaved invoices is count of rooms with status RENTED and having a utility index for this month but no INVOICE,
        // or having a utility index and invoice is unpaid/draft. Let's count rooms with utility indices for target month
        // that do NOT have a finalized invoice yet.
        long unsavedInvoicesCount = gridRows.stream()
                .filter(row -> row.isRented() && row.isSaved())
                .filter(row -> invoiceService.findByRoomIdAndMonthAndYear(row.getRoomId(), targetMonth, targetYear)
                        .map(i -> i.getStatus().equals("DRAFT")) // if it supports draft, or simply not generated yet
                        .orElse(true))
                .count();
        model.addAttribute("unsavedInvoicesCount", unsavedInvoicesCount);

        return "admin/dashboard";
    }

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("newRoom", new Room());
        return "admin/rooms";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute Room room) {
        roomService.save(room);
        return "redirect:/admin/rooms";
    }

    @GetMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        roomService.deleteById(id);
        return "redirect:/admin/rooms";
    }

    @GetMapping("/contracts")
    public String contracts(Model model, @RequestParam(value = "search", required = false) String search) {
        List<Contract> contracts;
        if (search != null && !search.trim().isEmpty()) {
            contracts = contractService.searchByTenantName(search);
            model.addAttribute("searchQuery", search);
        } else {
            contracts = contractService.findAll();
        }
        model.addAttribute("contracts", contracts);
        model.addAttribute("rooms", roomService.findAll());
        return "admin/contracts";
    }

    @PostMapping("/contracts/create")
    public String createContract(@RequestParam Long roomId,
                                 @RequestParam String tenantName,
                                 @RequestParam String tenantEmail,
                                 @RequestParam String tenantPhone,
                                 @RequestParam String startDate,
                                 @RequestParam String endDate,
                                 @RequestParam BigDecimal deposit) {
        contractService.createContract(
                roomId, tenantName, tenantEmail, tenantPhone,
                LocalDate.parse(startDate), LocalDate.parse(endDate), deposit
        );
        return "redirect:/admin/contracts";
    }

    @GetMapping("/contracts/terminate/{id}")
    public String terminateContract(@PathVariable Long id) {
        contractService.terminateContract(id);
        return "redirect:/admin/contracts";
    }

    @GetMapping("/issues")
    public String issues(Model model) {
        model.addAttribute("issues", issueReportService.findAll());
        return "admin/issues";
    }

    @PostMapping("/issues/status")
    public String updateIssueStatus(@RequestParam Long issueId, @RequestParam String status) {
        issueReportService.updateStatus(issueId, status);
        return "redirect:/admin/issues";
    }



    // Helper DTO for grid mapping
    @lombok.Data
    @lombok.Builder
    public static class UtilityInputRow {
        private Long roomId;
        private String roomNumber;
        private String tenantName;
        private Integer oldElec;
        private Integer newElec;
        private Integer oldWater;
        private Integer newWater;
        private boolean isRented;
        private boolean isSaved;
    }
}
