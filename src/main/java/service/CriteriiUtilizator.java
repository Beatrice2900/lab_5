package service;

import domain.entities.User;

import java.util.function.Predicate;

public class CriteriiUtilizator {
    public static Predicate<User> prenumeIncepeCu(String prefix){
        return user->user.getFirstName()!= null && user.getFirstName().startsWith(prefix);
    }

    public static Predicate<User> numeIncepeCu(String prefix){
        return user->user.getLastName()!=null && user.getLastName().startsWith(prefix);
    }
}
