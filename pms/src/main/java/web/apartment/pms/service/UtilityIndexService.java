package web.apartment.pms.service;

import web.apartment.pms.model.UtilityIndex;
import java.util.List;
import java.util.Optional;

public interface UtilityIndexService {
    Optional<UtilityIndex> findLatestByRoomId(Long roomId);
    Optional<UtilityIndex> findByRoomIdAndMonthAndYear(Long roomId, Integer month, Integer year);
    List<UtilityIndex> findHistoryByRoomId(Long roomId);
    UtilityIndex saveReading(Long roomId, Integer month, Integer year,
                             Integer oldElec, Integer newElec,
                             Integer oldWater, Integer newWater);
}
