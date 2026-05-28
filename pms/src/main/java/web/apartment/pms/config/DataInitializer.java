package web.apartment.pms.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import web.apartment.pms.model.*;
import web.apartment.pms.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final UtilityIndexRepository utilityIndexRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoomRepository roomRepository,
                           ContractRepository contractRepository,
                           UtilityIndexRepository utilityIndexRepository,
                           InvoiceRepository invoiceRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.contractRepository = contractRepository;
        this.utilityIndexRepository = utilityIndexRepository;
        this.invoiceRepository = invoiceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Users
        if (userRepository.count() == 0) {
            // Admin
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .role("ADMIN")
                    .fullName("Admin User")
                    .phone("0999999999")
                    .email("admin@pms.com")
                    .build();
            userRepository.save(admin);

            // Tenants
            User tenant1 = User.builder()
                    .username("tenant1")
                    .password(passwordEncoder.encode("123456"))
                    .role("TENANT")
                    .fullName("Nguyễn Văn A")
                    .phone("0912345678")
                    .email("tenant1@gmail.com")
                    .build();
            User tenant2 = User.builder()
                    .username("tenant2")
                    .password(passwordEncoder.encode("123456"))
                    .role("TENANT")
                    .fullName("Trần Thị B")
                    .phone("0912345679")
                    .email("tenant2@gmail.com")
                    .build();
            User tenant3 = User.builder()
                    .username("tenant3")
                    .password(passwordEncoder.encode("123456"))
                    .role("TENANT")
                    .fullName("Phạm Văn C")
                    .phone("0912345680")
                    .email("tenant3@gmail.com")
                    .build();
            User tenant4 = User.builder()
                    .username("tenant4")
                    .password(passwordEncoder.encode("123456"))
                    .role("TENANT")
                    .fullName("Lê Thị D")
                    .phone("0912345681")
                    .email("tenant4@gmail.com")
                    .build();
            User tenant5 = User.builder()
                    .username("tenant5")
                    .password(passwordEncoder.encode("123456"))
                    .role("TENANT")
                    .fullName("Võ Văn E")
                    .phone("0912345682")
                    .email("tenant5@gmail.com")
                    .build();

            userRepository.save(tenant1);
            userRepository.save(tenant2);
            userRepository.save(tenant3);
            userRepository.save(tenant4);
            userRepository.save(tenant5);

            // 2. Seed Rooms (P101 - P105 + some empty rooms to total 48 / 42 stats)
            Room r101 = Room.builder().roomNumber("101").price(BigDecimal.valueOf(3500000)).status("RENTED").description("Phòng lầu 1 thoáng mát").build();
            Room r102 = Room.builder().roomNumber("102").price(BigDecimal.valueOf(3800000)).status("RENTED").description("Phòng lầu 1 có ban công").build();
            Room r103 = Room.builder().roomNumber("103").price(BigDecimal.valueOf(4000000)).status("RENTED").description("Phòng lầu 2 máy lạnh").build();
            Room r104 = Room.builder().roomNumber("104").price(BigDecimal.valueOf(3200000)).status("RENTED").description("Phòng lầu 2 cơ bản").build();
            Room r105 = Room.builder().roomNumber("105").price(BigDecimal.valueOf(4500000)).status("RENTED").description("Phòng lầu 3 VIP").build();

            roomRepository.save(r101);
            roomRepository.save(r102);
            roomRepository.save(r103);
            roomRepository.save(r104);
            roomRepository.save(r105);

            // Seed dummy rooms to show realistic stats matching mockup (Total: 48, Rented: 42, Empty: 6)
            for (int i = 6; i <= 42; i++) {
                Room r = Room.builder()
                        .roomNumber("Room" + (100 + i))
                        .price(BigDecimal.valueOf(3000000 + (i % 5) * 200000))
                        .status("RENTED")
                        .description("Phòng mô phỏng")
                        .build();
                roomRepository.save(r);
            }
            for (int i = 43; i <= 48; i++) {
                Room r = Room.builder()
                        .roomNumber("Room" + (100 + i))
                        .price(BigDecimal.valueOf(3000000 + (i % 5) * 200000))
                        .status("EMPTY")
                        .description("Phòng trống mô phỏng")
                        .build();
                roomRepository.save(r);
            }

            // 3. Seed Contracts
            LocalDate start = LocalDate.now().minusMonths(3);
            LocalDate end = start.plusYears(1);

            Contract c1 = Contract.builder().room(r101).tenant(tenant1).startDate(start).endDate(end).deposit(BigDecimal.valueOf(3500000)).status("ACTIVE").build();
            Contract c2 = Contract.builder().room(r102).tenant(tenant2).startDate(start).endDate(end).deposit(BigDecimal.valueOf(3800000)).status("ACTIVE").build();
            Contract c3 = Contract.builder().room(r103).tenant(tenant3).startDate(start).endDate(end).deposit(BigDecimal.valueOf(4000000)).status("ACTIVE").build();
            Contract c4 = Contract.builder().room(r104).tenant(tenant4).startDate(start).endDate(end).deposit(BigDecimal.valueOf(3200000)).status("ACTIVE").build();
            Contract c5 = Contract.builder().room(r105).tenant(tenant5).startDate(start).endDate(end).deposit(BigDecimal.valueOf(4500000)).status("ACTIVE").build();

            contractRepository.save(c1);
            contractRepository.save(c2);
            contractRepository.save(c3);
            contractRepository.save(c4);
            contractRepository.save(c5);

            // Also seed dummy contracts for Rented rooms
            for (int i = 6; i <= 42; i++) {
                Room r = roomRepository.findByRoomNumber("Room" + (100 + i)).orElse(null);
                if (r != null) {
                    User dummyTenant = User.builder()
                            .username("dummy_tenant" + i)
                            .password(passwordEncoder.encode("123456"))
                            .role("TENANT")
                            .fullName("Khách Thuê " + i)
                            .phone("09888888" + String.format("%02d", i))
                            .email("tenant" + i + "@gmail.com")
                            .build();
                    userRepository.save(dummyTenant);

                    Contract c = Contract.builder()
                            .room(r)
                            .tenant(dummyTenant)
                            .startDate(start)
                            .endDate(end)
                            .deposit(r.getPrice())
                            .status("ACTIVE")
                            .build();
                    contractRepository.save(c);
                }
            }

            // 4. Seed Historical Utility Indices (previous month)
            // Month 4, Year 2026 (so old reading for month 5 will be the new reading of month 4)
            utilityIndexRepository.save(UtilityIndex.builder().room(r101).month(4).year(2026).oldElec(1200).newElec(1250).oldWater(480).newWater(500).build());
            utilityIndexRepository.save(UtilityIndex.builder().room(r102).month(4).year(2026).oldElec(2000).newElec(2100).oldWater(720).newWater(750).build());
            utilityIndexRepository.save(UtilityIndex.builder().room(r103).month(4).year(2026).oldElec(1750).newElec(1800).oldWater(570).newWater(600).build());
            utilityIndexRepository.save(UtilityIndex.builder().room(r104).month(4).year(2026).oldElec(900).newElec(950).oldWater(380).newWater(400).build());
            utilityIndexRepository.save(UtilityIndex.builder().room(r105).month(4).year(2026).oldElec(2300).newElec(2350).oldWater(870).newWater(900).build());

            // 5. Seed some Unpaid invoices to show realistic stats matching mockup (12 pending invoices, amount: 45.2M VND)
            // We can seed 12 invoices for the previous month (April) that are UNPAID
            BigDecimal pendingSum = BigDecimal.ZERO;
            for (int i = 6; i < 18; i++) {
                Room r = roomRepository.findByRoomNumber("Room" + (100 + i)).orElse(null);
                Contract c = contractRepository.findFirstByRoomIdAndStatus(r.getId(), "ACTIVE").orElse(null);
                if (r != null && c != null) {
                    BigDecimal amt = r.getPrice().add(BigDecimal.valueOf(500000)); // room rent + utilities
                    Invoice inv = Invoice.builder()
                            .room(r)
                            .contract(c)
                            .month(4)
                            .year(2026)
                            .totalAmount(amt)
                            .status("UNPAID")
                            .build();
                    invoiceRepository.save(inv);
                    pendingSum = pendingSum.add(amt);
                }
            }
            System.out.println("Data Initializer completed. Seeded total " + pendingSum + " VND in pending invoices.");
        }
    }
}
