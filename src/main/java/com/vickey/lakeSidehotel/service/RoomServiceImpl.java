package com.vickey.lakeSidehotel.service;

import com.vickey.lakeSidehotel.exception.InternalServerException;
import com.vickey.lakeSidehotel.exception.ResourceNotFoundException;
import com.vickey.lakeSidehotel.model.Room;
import com.vickey.lakeSidehotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements  IRoomService{

    private final RoomRepository roomRepo;

    @Override
    public Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws SQLException, IOException {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);

        if(!file.isEmpty()){
            byte[] photoBytes = file.getBytes();
            Blob photoBlob = new SerialBlob(photoBytes);
            room.setPhoto(photoBlob);
        }


        return roomRepo.save(room);
    }

    @Override
    public List<String> getAllRoomTypes() {
        List<String> result =  roomRepo.findDistinctRoomTypes();
        System.out.println(result);
        return result;
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepo.findAll();
    }

    @Override
    public byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException {
        Optional<Room> theRoom = roomRepo.findById(roomId);
        if(theRoom.isEmpty()){
            throw new ResourceNotFoundException("Sorry, Room not found!");
        }
        Blob photoBlob = theRoom.get().getPhoto();
        if(photoBlob != null){
            return photoBlob.getBytes(1, (int) photoBlob.length());
        }
        return null;
    }

    @Override
    public void deleteRoom(Long roomId) {
        Optional<Room> theRoom = roomRepo.findById(roomId);
        if(theRoom.isPresent()){
            roomRepo.deleteById(roomId);
        }
    }

    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found"));

        if (roomType != null) room.setRoomType(roomType);
        if (roomPrice != null) room.setRoomPrice(roomPrice);
        if (photoBytes != null && photoBytes.length > 0) {
            try {
                room.setPhoto(new SerialBlob(photoBytes));
            } catch (SQLException ex) {
                throw new InternalServerException("Fail updating room");
            }
        }
        return roomRepo.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return Optional.of(roomRepo.findById(roomId).get());
    }


}
