package web.apartment.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.apartment.pms.model.IssueReport;
import java.util.List;

@Repository
public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {
    List<IssueReport> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<IssueReport> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    List<IssueReport> findAllByOrderByCreatedAtDesc();
}
