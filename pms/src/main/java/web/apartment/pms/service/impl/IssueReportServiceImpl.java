package web.apartment.pms.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.apartment.pms.model.IssueReport;
import web.apartment.pms.model.Room;
import web.apartment.pms.model.User;
import web.apartment.pms.repository.IssueReportRepository;
import web.apartment.pms.repository.RoomRepository;
import web.apartment.pms.repository.UserRepository;
import web.apartment.pms.service.IssueReportService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IssueReportServiceImpl implements IssueReportService {

    private final IssueReportRepository issueReportRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public IssueReportServiceImpl(IssueReportRepository issueReportRepository,
                                  RoomRepository roomRepository,
                                  UserRepository userRepository) {
        this.issueReportRepository = issueReportRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<IssueReport> findAll() {
        return issueReportRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<IssueReport> findByTenantId(Long tenantId) {
        return issueReportRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Override
    public Optional<IssueReport> findById(Long id) {
        return issueReportRepository.findById(id);
    }

    @Override
    public IssueReport createIssueReport(Long roomId, Long tenantId, String description) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + tenantId));

        IssueReport report = IssueReport.builder()
                .room(room)
                .tenant(tenant)
                .description(description)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return issueReportRepository.save(report);
    }

    @Override
    public IssueReport updateStatus(Long id, String status) {
        IssueReport report = issueReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue report not found: " + id));
        report.setStatus(status);
        return issueReportRepository.save(report);
    }
}
