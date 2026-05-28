package web.apartment.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import web.apartment.pms.model.Invoice;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStatus(String status);
    List<Invoice> findByRoomIdOrderByYearDescMonthDesc(Long roomId);
    List<Invoice> findByContractTenantIdOrderByYearDescMonthDesc(Long tenantId);
    Optional<Invoice> findByRoomIdAndMonthAndYear(Long roomId, Integer month, Integer year);
    
    long countByStatus(String status);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = :status")
    BigDecimal sumTotalAmountByStatus(String status);
}
