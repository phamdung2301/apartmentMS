package web.apartment.pms.service;

import web.apartment.pms.model.IssueReport;
import java.util.List;
import java.util.Optional;

public interface IssueReportService {
    List<IssueReport> findAll();
    List<IssueReport> findByTenantId(Long tenantId);
    Optional<IssueReport> findById(Long id);
    IssueReport createIssueReport(Long roomId, Long tenantId, String description);
    IssueReport updateStatus(Long id, String status);
}
