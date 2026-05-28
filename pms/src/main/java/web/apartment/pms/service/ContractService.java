package web.apartment.pms.service;

import web.apartment.pms.model.Contract;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContractService {
    List<Contract> findAll();
    Optional<Contract> findById(Long id);
    Optional<Contract> findActiveByRoomId(Long roomId);
    Optional<Contract> findActiveByTenantId(Long tenantId);
    Contract createContract(Long roomId, String tenantName, String tenantEmail, String tenantPhone,
                            LocalDate startDate, LocalDate endDate, BigDecimal deposit);
    Contract terminateContract(Long contractId);
    List<Contract> searchByTenantName(String tenantName);
}
