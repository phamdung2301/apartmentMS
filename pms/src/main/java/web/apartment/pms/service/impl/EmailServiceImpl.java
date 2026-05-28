package web.apartment.pms.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import web.apartment.pms.model.Invoice;
import web.apartment.pms.model.UtilityIndex;
import web.apartment.pms.service.EmailService;

import java.math.BigDecimal;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:your-email@gmail.com}")
    private String fromEmail;

    @Value("${pms.utility.price-per-kwh:3500}")
    private double pricePerKwh;

    @Value("${pms.utility.price-per-m3:15000}")
    private double pricePerM3;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("mailExecutor")
    @Override
    public void sendInvoiceEmail(Invoice invoice, UtilityIndex utilityIndex) {
        log.info("Starting async invoice email sending for Room: {} to email: {}", 
                 invoice.getRoom().getRoomNumber(), invoice.getContract().getTenant().getEmail());
        
        String recipient = invoice.getContract().getTenant().getEmail();
        String subject = "Hóa Đơn Tiền Phòng - Phòng " + invoice.getRoom().getRoomNumber() + " - Tháng " + invoice.getMonth() + "/" + invoice.getYear();
        
        int elecDiff = utilityIndex.getNewElec() - utilityIndex.getOldElec();
        double elecCost = elecDiff * pricePerKwh;
        int waterDiff = utilityIndex.getNewWater() - utilityIndex.getOldWater();
        double waterCost = waterDiff * pricePerM3;

        String htmlContent = String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif; color: #333;'>" +
            "  <div style='max-width: 600px; margin: 0 auto; border: 1px solid #ddd; padding: 20px; border-radius: 8px;'>" +
            "    <h2 style='color: #fd5d14; border-bottom: 2px solid #fd5d14; padding-bottom: 10px;'>HÓA ĐƠN TIỀN PHÒNG</h2>" +
            "    <p>Xin chào <strong>%s</strong>,</p>" +
            "    <p>Dưới đây là chi tiết hóa đơn tiền phòng tháng %d/%d của phòng <strong>%s</strong>:</p>" +
            "    <table style='width: 100%%; border-collapse: collapse; margin-top: 15px;'>" +
            "      <thead>" +
            "        <tr style='background-color: #f8f9fa;'>" +
            "          <th style='border: 1px solid #dee2e6; padding: 8px; text-align: left;'>Khoản mục</th>" +
            "          <th style='border: 1px solid #dee2e6; padding: 8px; text-align: center;'>Chỉ số cũ</th>" +
            "          <th style='border: 1px solid #dee2e6; padding: 8px; text-align: center;'>Chỉ số mới</th>" +
            "          <th style='border: 1px solid #dee2e6; padding: 8px; text-align: right;'>Tiêu thụ</th>" +
            "          <th style='border: 1px solid #dee2e6; padding: 8px; text-align: right;'>Thành tiền</th>" +
            "        </tr>" +
            "      </thead>" +
            "      <tbody>" +
            "        <tr>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px;'>Tiền phòng cơ bản</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: center;'>-</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: center;'>-</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: right;'>-</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: right;'>%,.2f VND</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px;'>Tiền điện (%,.0f đ/kWh)</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: center;'>%d</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: center;'>%d</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: right;'>%d kWh</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: right;'>%,.2f VND</td>" +
            "        </tr>" +
            "        <tr>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px;'>Tiền nước (%,.0f đ/m³)</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: center;'>%d</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: center;'>%d</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: right;'>%d m³</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: right;'>%,.2f VND</td>" +
            "        </tr>" +
            "        <tr style='font-weight: bold; background-color: #e9ecef;'>" +
            "          <td colspan='4' style='border: 1px solid #dee2e6; padding: 8px; text-align: right;'>Tổng cộng:</td>" +
            "          <td style='border: 1px solid #dee2e6; padding: 8px; text-align: right; color: #fd5d14;'>%,.2f VND</td>" +
            "        </tr>" +
            "      </tbody>" +
            "    </table>" +
            "    <p style='margin-top: 20px;'>Vui lòng truy cập hệ thống Mini-PMS để quét mã <strong>VietQR</strong> thực hiện thanh toán chuyển khoản nhanh chóng.</p>" +
            "    <hr style='border: 0; border-top: 1px solid #eee; margin-top: 30px;' />" +
            "    <p style='font-size: 12px; color: #888; text-align: center;'>Thư được gửi tự động từ hệ thống Mini-PMS. Vui lòng không trả lời thư này.</p>" +
            "  </div>" +
            "</body>" +
            "</html>",
            invoice.getContract().getTenant().getFullName(),
            invoice.getMonth(), invoice.getYear(),
            invoice.getRoom().getRoomNumber(),
            invoice.getContract().getRoom().getPrice(),
            pricePerKwh, utilityIndex.getOldElec(), utilityIndex.getNewElec(), elecDiff, elecCost,
            pricePerM3, utilityIndex.getOldWater(), utilityIndex.getNewWater(), waterDiff, waterCost,
            invoice.getTotalAmount()
        );

        sendHtmlMail(recipient, subject, htmlContent);
    }

    @Async("mailExecutor")
    @Override
    public void sendDebtReminderEmail(Invoice invoice) {
        log.info("Starting async debt reminder email sending for Room: {} to email: {}", 
                 invoice.getRoom().getRoomNumber(), invoice.getContract().getTenant().getEmail());

        String recipient = invoice.getContract().getTenant().getEmail();
        String subject = "[CẢNH BÁO NỢ] Nhắc Nợ Tiền Phòng " + invoice.getRoom().getRoomNumber() + " - Tháng " + invoice.getMonth() + "/" + invoice.getYear();

        String htmlContent = String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif; color: #333;'>" +
            "  <div style='max-width: 600px; margin: 0 auto; border: 2px solid #dc3545; padding: 20px; border-radius: 8px;'>" +
            "    <h2 style='color: #dc3545; border-bottom: 2px solid #dc3545; padding-bottom: 10px;'>CẢNH BÁO NỢ TIỀN PHÒNG</h2>" +
            "    <p>Xin chào <strong>%s</strong>,</p>" +
            "    <p>Hệ thống Mini-PMS ghi nhận hóa đơn tiền phòng tháng <strong>%d/%d</strong> của phòng <strong>%s</strong> vẫn ở trạng thái <strong style='color:#dc3545;'>CHƯA THANH TOÁN</strong>.</p>" +
            "    <p><strong>Số tiền nợ quá hạn:</strong> <strong style='font-size: 18px; color: #dc3545;'>%,.2f VND</strong></p>" +
            "    <p>Vui lòng thanh toán hóa đơn trong thời gian sớm nhất bằng cách truy cập vào hệ thống để quét mã chuyển khoản nhanh VietQR.</p>" +
            "    <p>Nếu bạn đã thanh toán, vui lòng phản hồi lại với chủ nhà trọ hoặc bỏ qua email này.</p>" +
            "    <hr style='border: 0; border-top: 1px solid #eee; margin-top: 30px;' />" +
            "    <p style='font-size: 12px; color: #888; text-align: center;'>Thư được gửi tự động từ hệ thống Mini-PMS Scheduler.</p>" +
            "  </div>" +
            "</body>" +
            "</html>",
            invoice.getContract().getTenant().getFullName(),
            invoice.getMonth(), invoice.getYear(),
            invoice.getRoom().getRoomNumber(),
            invoice.getTotalAmount()
        );

        sendHtmlMail(recipient, subject, htmlContent);
    }

    private void sendHtmlMail(String recipient, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email successfully sent to {}", recipient);
        } catch (Exception e) {
            log.warn("Could not send email to {} due to mail configuration issue. Logging mock email content instead.", recipient);
            log.info("\n=== MOCK EMAIL SENT ===\nRecipient: {}\nSubject: {}\nContent Preview:\n{}\n=======================", 
                     recipient, subject, htmlContent.substring(0, Math.min(htmlContent.length(), 350)) + "...");
        }
    }
}
