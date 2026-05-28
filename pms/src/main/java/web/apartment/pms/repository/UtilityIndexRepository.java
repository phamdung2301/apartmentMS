package web.apartment.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.apartment.pms.model.UtilityIndex;
import java.util.Optional;

@Repository
public interface UtilityIndexRepository extends JpaRepository<UtilityIndex, Long> {
    Optional<UtilityIndex> findByRoomIdAndMonthAndYear(Long roomId, Integer month, Integer year);
    Optional<UtilityIndex> findFirstByRoomIdOrderByYearDescMonthDesc(Long roomId);
}
