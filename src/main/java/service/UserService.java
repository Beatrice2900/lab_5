package service;

import domain.entities.Friendship;
import domain.entities.Tuple;
import domain.entities.User;
import domain.exceptions.EntityNotFoundException;
import repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static service.StreamUtils.stream;

/**
 * Service that manages the users of the network.
 */
public class UserService {
    private final Repository<Long, User> userRepo;
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepo;

    /**
     * Creates a user service.
     *
     * @param userRepo       the repository of users
     * @param friendshipRepo the repository of friendships
     */
    public UserService(Repository<Long, User> userRepo, Repository<Tuple<Long, Long>, Friendship> friendshipRepo) {
        this.userRepo = userRepo;
        this.friendshipRepo = friendshipRepo;
    }

    /**
     * Adds a new user to the network.
     *
     * @param id        the id of the user
     * @param firstName the first name of the user
     * @param lastName  the last name of the user
     * @throws domain.exceptions.ValidationException      if the user is not valid
     * @throws domain.exceptions.DuplicateEntityException if the id already exists
     */
    public Long addUser(String firstName, String lastName) {
        User user = new User(null, firstName, lastName);
        userRepo.save(user);
        return user.getId();
    }
    /**
     * Removes a user together with all the friendships he or she is part of.
     *
     * @param id the id of the user to remove
     * @throws EntityNotFoundException if no user has this id
     */
    public void removeUser(Long id) {
        findUser(id).orElseThrow(() -> new EntityNotFoundException("Entity with id " + id + " not found"));

        stream(friendshipRepo.findAll())
                .filter(f -> f.getUserId1().equals(id) || f.getUserId2().equals(id))
                .map(Friendship::getId)
                .collect(Collectors.toList())
                .forEach(friendshipRepo::delete);

        userRepo.delete(id);
    }

    /**
     * Finds a user by id.
     *
     * @param id the id of the user
     * @return the user, or an empty optional if no user has this id
     */
    public Optional<User> findUser(Long id) {
        try {
            return Optional.ofNullable(userRepo.findOne(id));
        } catch (EntityNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the user with the given id.
     *
     * @param id the id of the user
     * @return the user
     * @throws EntityNotFoundException if no user has this id
     */
    public User getUser(Long id) {
        return findUser(id).orElseThrow(() -> new EntityNotFoundException("Entity with id " + id + " not found"));
    }

    /**
     * Returns all the users of the network.
     *
     * @return all the users
     */
    public Iterable<User> getAllUsers() {
        return userRepo.findAll();
    }

    /**
     * Finds the users that satisfy a condition.
     *
     * @param filter the condition
     * @return the matching users
     */
    public List<User> findUsers(Predicate<User> filter) {
        return userRepo.findAll(filter);
    }
}