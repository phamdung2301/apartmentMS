package web.apartment.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.apartment.pms.model.Contract;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Optional<Contract> findFirstByRoomIdAndStatus(Long roomId, String status);
    Optional<Contract> findFirstByTenantIdAndStatus(Long tenantId, String status);
    List<Contract> findByTenantId(Long tenantId);
    List<Contract> findByRoomId(Long roomId);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Contract c WHERE LOWER(c.tenant.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Contract> searchContractsByTenantName(@org.springframework.data.repository.query.Param("name") String name);
}
