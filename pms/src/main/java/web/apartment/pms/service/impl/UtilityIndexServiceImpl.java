package web.apartment.pms.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.apartment.pms.model.Room;
import web.apartment.pms.model.UtilityIndex;
import web.apartment.pms.repository.RoomRepository;
import web.apartment.pms.repository.UtilityIndexRepository;
import web.apartment.pms.service.UtilityIndexService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UtilityIndexServiceImpl implements UtilityIndexService {

    private final UtilityIndexRepository utilityIndexRepository;
    private final RoomRepository roomRepository;

    public UtilityIndexServiceImpl(UtilityIndexRepository utilityIndexRepository, RoomRepository roomRepository) {
        this.utilityIndexRepository = utilityIndexRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public Optional<UtilityIndex> findLatestByRoomId(Long roomId) {
        return utilityIndexRepository.findFirstByRoomIdOrderByYearDescMonthDesc(roomId);
    }

    @Override
    public Optional<UtilityIndex> findByRoomIdAndMonthAndYear(Long roomId, Integer month, Integer year) {
        return utilityIndexRepository.findByRoomIdAndMonthAndYear(roomId, month, year);
    }

    @Override
    public UtilityIndex saveReading(Long roomId, Integer month, Integer year,
                                     Integer oldElec, Integer newElec,
                                     Integer oldWater, Integer newWater) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        UtilityIndex utilityIndex = utilityIndexRepository.findByRoomIdAndMonthAndYear(roomId, month, year)
                .orElseGet(() -> UtilityIndex.builder()
                        .room(room)
                        .month(month)
                        .year(year)
                        .build());

        utilityIndex.setOldElec(oldElec != null ? oldElec : utilityIndex.getOldElec());
        utilityIndex.setNewElec(newElec != null ? newElec : utilityIndex.getNewElec());
        utilityIndex.setOldWater(oldWater != null ? oldWater : utilityIndex.getOldWater());
        utilityIndex.setNewWater(newWater != null ? newWater : utilityIndex.getNewWater());
        utilityIndex.setCreatedAt(LocalDateTime.now());

        return utilityIndexRepository.save(utilityIndex);
    }
}
