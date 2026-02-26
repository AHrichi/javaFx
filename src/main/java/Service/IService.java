package Service;

import java.sql.SQLException;
import java.util.List;

public interface IService <T>{
    boolean ajouter(T t) throws SQLException;
    boolean supprimer(T t) throws SQLException;
    boolean modifier(T t) throws SQLException;
    T findbyId(int id) throws SQLException;
    List<T> readAll() throws SQLException;
}
