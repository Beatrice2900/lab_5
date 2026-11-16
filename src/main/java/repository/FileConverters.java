package repository;

import domain.entities.Friendship;
import domain.entities.User;
import domain.exceptions.EntityNotFoundException;
import domain.exceptions.RepositoryException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class FileConverters {

    public static User parseUser(String line){
        String[] parts = line.split(";", -1);
        if(parts.length !=3){
            throw new RepositoryException("Expected 3 fields but found " + parts.length);
        }
        try{
            Long id = Long.parseLong(parts[0].trim());
            return new User(id, parts[1].trim(), parts[2].trim());
        }catch(NumberFormatException e) {
            throw new RepositoryException("Invalid id: " + parts[0], e);
        }
    }
    public static String userToLine(User user){
        return user.getId() + ";" + user.getFirstName() + ";" + user.getLastName();
    }

    public static Friendship parseFriendship(String line, Repository<Long, User> userRepo){
        String[] parts = line.split(";", -1);
        if (parts.length != 3) {
            throw new RepositoryException("Expected 3 fields but found " + parts.length);
        }
        Long id1;
        Long id2;
        LocalDateTime date;
        try {
            id1 = Long.parseLong(parts[0].trim());
            id2 = Long.parseLong(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new RepositoryException("Invalid user id in line: " + line, e);
        }
        try {
            date = LocalDateTime.parse(parts[2].trim());
        } catch (DateTimeParseException e) {
            throw new RepositoryException("Invalid date: " + parts[2], e);
        }
        try {
            userRepo.findOne(id1);
            userRepo.findOne(id2);
        } catch (EntityNotFoundException e) {
            throw new RepositoryException("Friendship refers to a user that does not exist: " + line, e);
        }
        return new Friendship(id1, id2, date);
    }

    public static String friendshipToLine(Friendship f){
        return f.getUserId1() + ";" + f.getUserId2() + ";" + f.getDate();
    }
}
