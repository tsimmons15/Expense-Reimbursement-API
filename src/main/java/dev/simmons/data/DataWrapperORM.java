package dev.simmons.data;

import java.sql.SQLException;
import java.util.List;

public interface DataWrapperORM<T> {
    T createEntity(T entity) throws SQLException; // creates an employee

    T getEntityById(int id) throws SQLException; // Get an employee by ID

    List<T> getAllEntities() throws SQLException;// get all instances of the employee

    T replaceEntity(T entity) throws SQLException;// update an instance

    boolean deleteEntity(int id) throws SQLException;
}
