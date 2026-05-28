package web.apartment.pms.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.apartment.pms.model.Contract;
import web.apartment.pms.model.Room;
import web.apartment.pms.model.User;
import web.apartment.pms.repository.ContractRepository;
import web.apartment.pms.repository.RoomRepository;
import web.apartment.pms.repository.UserRepository;
import web.apartment.pms.service.ContractService;
import web.apartment.pms.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ContractServiceImpl(ContractRepository contractRepository,
                               RoomRepository roomRepository,
                               UserRepository userRepository,
                               UserService userService) {
        this.contractRepository = contractRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    @Override
    public Optional<Contract> findById(Long id) {
        return contractRepository.findById(id);
    }

    @Override
    public Optional<Contract> findActiveByRoomId(Long roomId) {
        return contractRepository.findFirstByRoomIdAndStatus(roomId, "ACTIVE");
    }

    @Override
    public Optional<Contract> findActiveByTenantId(Long tenantId) {
        return contractRepository.findFirstByTenantIdAndStatus(tenantId, "ACTIVE");
    }

    @Override
    public Contract createContract(Long roomId, String tenantName, String tenantEmail, String tenantPhone,
                                   LocalDate startDate, LocalDate endDate, BigDecimal deposit) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        // Create or find user
        User tenant = userRepository.findByEmail(tenantEmail)
                .orElseGet(() -> userService.createUserForTenant(tenantName, tenantEmail, tenantPhone));

        // Terminate any previous active contracts on this room
        contractRepository.findFirstByRoomIdAndStatus(roomId, "ACTIVE").ifPresent(c -> {
            c.setStatus("EXPIRED");
            contractRepository.save(c);
        });

        Contract contract = Contract.builder()
                .room(room)
                .tenant(tenant)
                .startDate(startDate)
                .endDate(endDate)
                .deposit(deposit)
                .status("ACTIVE")
                .build();

        room.setStatus("RENTED");
        roomRepository.save(room);

        return contractRepository.save(contract);
    }

    @Override
    public Contract terminateContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        contract.setStatus("EXPIRED");
        Room room = contract.getRoom();
        room.setStatus("EMPTY");
        roomRepository.save(room);

        return contractRepository.save(contract);
    }

    @Override
    public List<Contract> searchByTenantName(String tenantName) {
        return contractRepository.searchContractsByTenantName(tenantName);
    }
}
