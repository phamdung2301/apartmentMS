package web.apartment.pms.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.apartment.pms.model.Room;
import web.apartment.pms.repository.RoomRepository;
import web.apartment.pms.service.RoomService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    @Override
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }

    @Override
    public long countTotal() {
        return roomRepository.count();
    }

    @Override
    public long countRented() {
        return roomRepository.countByStatus("RENTED");
    }

    @Override
    public long countEmpty() {
        return roomRepository.countByStatus("EMPTY");
    }
}
