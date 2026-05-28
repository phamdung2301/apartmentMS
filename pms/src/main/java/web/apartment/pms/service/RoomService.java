package web.apartment.pms.service;

import web.apartment.pms.model.Room;
import java.util.List;
import java.util.Optional;

public interface RoomService {
    List<Room> findAll();
    Optional<Room> findById(Long id);
    Room save(Room room);
    void deleteById(Long id);
    long countTotal();
    long countRented();
    long countEmpty();
}
