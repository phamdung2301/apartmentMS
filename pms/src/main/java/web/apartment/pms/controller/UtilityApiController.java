package web.apartment.pms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.apartment.pms.model.Invoice;
import web.apartment.pms.model.Room;
import web.apartment.pms.service.ContractService;
import web.apartment.pms.service.InvoiceService;
import web.apartment.pms.service.RoomService;
import web.apartment.pms.service.UtilityIndexService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/utility")
public class UtilityApiController {

    private final UtilityIndexService utilityIndexService;
    private final InvoiceService invoiceService;
    private final RoomService roomService;
    private final ContractService contractService;

    public UtilityApiController(UtilityIndexService utilityIndexService,
                                InvoiceService invoiceService,
                                RoomService roomService,
                                ContractService contractService) {
        this.utilityIndexService = utilityIndexService;
        this.invoiceService = invoiceService;
        this.roomService = roomService;
        this.contractService = contractService;
    }

    @PostMapping("/save-reading")
    public ResponseEntity<?> saveReading(@RequestParam Long roomId,
                                         @RequestParam Integer month,
                                         @RequestParam Integer year,
                                         @RequestParam Integer oldElec,
                                         @RequestParam Integer newElec,
                                         @RequestParam Integer oldWater,
                                         @RequestParam Integer newWater) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (newElec < oldElec) {
                response.put("success", false);
                response.put("message", "Chỉ số điện mới không được nhỏ hơn chỉ số cũ!");
                return ResponseEntity.badRequest().body(response);
            }
            if (newWater < oldWater) {
                response.put("success", false);
                response.put("message", "Chỉ số nước mới không được nhỏ hơn chỉ số cũ!");
                return ResponseEntity.badRequest().body(response);
            }

            utilityIndexService.saveReading(roomId, month, year, oldElec, newElec, oldWater, newWater);
            response.put("success", true);
            response.put("message", "Đã lưu chỉ số điện nước thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/generate-invoices")
    public ResponseEntity<?> generateInvoices(@RequestParam Integer month,
                                              @RequestParam Integer year) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Room> rooms = roomService.findAll();
            int count = 0;
            
            for (Room room : rooms) {
                // Generate invoices only for RENTED rooms that have saved utility indices for this month
                if ("RENTED".equals(room.getStatus())) {
                    boolean hasIndex = utilityIndexService.findByRoomIdAndMonthAndYear(room.getId(), month, year).isPresent();
                    if (hasIndex) {
                        invoiceService.generateInvoice(room.getId(), month, year);
                        count++;
                    }
                }
            }

            response.put("success", true);
            response.put("message", "Đã xuất hóa đơn và gửi email thành công cho " + count + " phòng!");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi chốt hóa đơn: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/pay-invoice/{id}")
    public ResponseEntity<?> payInvoice(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            invoiceService.markAsPaid(id);
            response.put("success", true);
            response.put("message", "Đã xác nhận thanh toán hóa đơn!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
