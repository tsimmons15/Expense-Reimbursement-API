package dev.simmons.data;

import dev.simmons.annotation.DBEntity;
import dev.simmons.annotation.DbField;
import dev.simmons.annotation.PrimaryKey;
import dev.simmons.entities.Expense;
import dev.simmons.exceptions.NoSuchEntityException;
import dev.simmons.utilities.connection.PostgresConnection;
import dev.simmons.utilities.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PostgresORM<T> implements DataWrapperORM<T>{

    private final Class<T> typeClass;
    protected Field[] fields;
    protected Map<String, Method> getters;
    protected Map<String, Method> setters;
    protected String table;

    protected final String createSql;
    protected final String getByIdSql;
    protected final String getAllSql;
    protected final String updateSql;
    protected final String deleteSql;

    public PostgresORM(Class<T> clazz) {
        this.typeClass = clazz;

        if (clazz.isAnnotationPresent(DBEntity.class)) {
            table = clazz.getAnnotation(DBEntity.class).value();
        }

        StringBuilder createStatement = new StringBuilder("insert into ?? (--) values (++);");
        StringBuilder getByIdStatement = new StringBuilder("select * from ?? where && = ?;");
        StringBuilder getAllStatement = new StringBuilder("select * from ??;");
        StringBuilder updateStatement = new StringBuilder("update ?? set <> where && = ?;");
        StringBuilder deleteStatement = new StringBuilder("delete from ?? where && = ?;");

        int index = createStatement.indexOf("??");
        createStatement.replace(index, index + 2, table);

        index = getByIdStatement.indexOf("??");
        getByIdStatement.replace(index, index + 2, table);

        index = getAllStatement.indexOf("??");
        getAllStatement.replace(index, index + 2, table);

        index = updateStatement.indexOf("??");
        updateStatement.replace(index, index + 2, table);

        index = deleteStatement.indexOf("??");
        deleteStatement.replace(index, index + 2, table);

        fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            DbField dbField = field.getAnnotation(DbField.class);
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                index = getByIdStatement.indexOf("&&");
                getByIdStatement.replace(index, index + 2, dbField.name());

                index = updateStatement.indexOf("&&");
                updateStatement.replace(index, index + 2, dbField.name());

                index = deleteStatement.indexOf("&&");
                deleteStatement.replace(index, index + 2, dbField.name());
            } else {
                index = createStatement.indexOf("--");
                createStatement.replace(index, index + 2, dbField.name() + ", --");

                index = createStatement.indexOf("++");
                createStatement.replace(index, index+2, "?, ++");

                index = updateStatement.indexOf("<>");
                updateStatement.replace(index, index + 2, dbField.name() + " = ?, <>");
            }
        }

        index = createStatement.indexOf("--");
        createStatement.replace(index - 2, index + 2, "");

        index = createStatement.indexOf("++");
        createStatement.replace(index - 2, index + 2, "");

        index = updateStatement.indexOf("<>");
        updateStatement.replace(index - 2, index + 2, "");

        createSql = createStatement.toString();
        getByIdSql = getByIdStatement.toString();
        getAllSql = getAllStatement.toString();
        updateSql = updateStatement.toString();
        deleteSql = deleteStatement.toString();
    }

    @Override
    public T createEntity(T entity) throws SQLException {
        try (Connection conn = PostgresConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(createSql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            Field idField = null;
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    idField = field;
                    continue;
                }

                Object obj = getGetter(this.typeClass, field.getName()).invoke(entity);

                prepareStatementField(field, statement, obj, index);

                index++;
            }

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "Failed to create for " + table.toUpperCase() + " using values: " + entity);
            }

            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            int id = rs.getInt(1);
            if (idField == null) {
                Logger.log(Logger.Level.WARNING, "No id field found in field list.");
                return null;
            }

            Method idSetter = getSetter(this.typeClass, idField);
            idSetter.invoke(entity, id);

            return entity;
        } catch (IllegalAccessException | IllegalArgumentException |
                NoSuchMethodException | InvocationTargetException iae) {
            Logger.log(Logger.Level.ERROR, iae);
        }

        return null;
    }

    @Override
    public T getEntityById(int id) throws SQLException {
        try (Connection conn = PostgresConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(getByIdSql);
            statement.setInt(1, id);

            ResultSet rs = statement.executeQuery();
            T entity = createNewEntity();

            rs.next();
            for (Field field : fields) {
                fillFieldFromResult(rs, entity, field);
            }

            return entity;
        } catch (InvocationTargetException | IllegalAccessException |
                InstantiationException | NoSuchMethodException e) {
            Logger.log(Logger.Level.ERROR, "Attempted to getEntityById " +
                    "for Entity that did not conform to expectations.");
        }

        return null;
    }

    @Override
    public List<T> getAllEntities() throws SQLException {
        try (Connection conn = PostgresConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(getAllSql);

            ResultSet rs = statement.executeQuery();
            List<T> list = new ArrayList<>();

            while (rs.next()) {
                T entity = createNewEntity();
                for (Field field : fields) {
                    fillFieldFromResult(rs, entity, field);
                }
                list.add(entity);
            }

            return list;
        } catch (InvocationTargetException | IllegalAccessException |
                InstantiationException | NoSuchMethodException e) {
            Logger.log(Logger.Level.ERROR, "Attempted to getEntityById " +
                    "for Entity that did not conform to expectations.");
        }
        return null;
    }

    @Override
    public T replaceEntity(T entity) throws SQLException {
        try (Connection conn = PostgresConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(updateSql);
            int index = 1;
            Field idField = null;
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    idField = field;
                    continue;
                }

                Object obj = getGetter(this.typeClass, field.getName()).invoke(entity);

                prepareStatementField(field, statement, obj, index);

                index++;
            }

            // This is the id, we set it last so we don't need to worry about figuring out the index
            int idValue = (Integer)getGetter(this.typeClass, idField.getName()).invoke(entity);
            prepareStatementField(idField, statement, idValue, index);

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "Failed to update for " + table.toUpperCase() + " using values: " + entity);
                return null;
            }

            return entity;
        } catch (IllegalAccessException | IllegalArgumentException |
                NoSuchMethodException | InvocationTargetException iae) {
            Logger.log(Logger.Level.ERROR, "Issue with the generic-type given to PostgresORM: issues with method or field access.");
            Logger.log(Logger.Level.ERROR, iae);
        }

        return null;
    }

    @Override
    public boolean deleteEntity(int id) throws SQLException {
        try (Connection conn = PostgresConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(deleteSql);
            statement.setInt(1, id);

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "Failed to delete " + table.toUpperCase() + " matching (id: " + id + ").");
                throw new NoSuchEntityException("Failed to delete " + table.toUpperCase() + " matching (id: " + id + ").");
            }

            return true;
        }
    }

    private T createNewEntity() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return this.typeClass.getDeclaredConstructor().newInstance();
    }

    private void prepareStatementField(Field field, PreparedStatement statement, Object obj, int index) throws SQLException {
        switch(field.getType().getName().toLowerCase()) {
            case "long":
            case "java.lang.long":
                if (obj == null) {
                    statement.setNull(index, Types.BIGINT);
                } else {
                    statement.setLong(index, (Long) obj);
                }
                break;
            case "int":
            case "java.lang.integer":
                if (obj == null) {
                    statement.setNull(index, Types.INTEGER);
                } else {
                    statement.setInt(index, (Integer) obj);
                }
                break;
            case "java.lang.string":
            case "dev.simmons.entities.expense$status":
                if (obj == null) {
                    statement.setNull(index, Types.VARCHAR);
                } else {
                    statement.setString(index, obj.toString());
                }
                break;
            case "float":
            case "java.lang.float":
                if (obj == null) {
                    statement.setNull(index, Types.FLOAT);
                } else {
                    statement.setFloat(index, (Float) obj);
                }
                break;
        }
    }

    private void fillFieldFromResult(ResultSet rs, T entity, Field field) throws SQLException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String columnName = field.getAnnotation(DbField.class).name();
        Method setter = getSetter(this.typeClass, field);
        Object obj = null;
        switch (field.getType().getName().toLowerCase()) {
            case "long":
            case "java.lang.long":
                obj = rs.getLong(columnName);
                break;
            case "int":
            case "java.lang.integer":
                obj = rs.getInt(columnName);
                break;
            case "java.lang.string":
                obj = rs.getString(columnName);
                break;
            case "float":
            case "java.lang.float":
                obj = rs.getFloat(columnName);
                break;
            case "dev.simmons.entities.expense$status":
                obj = Expense.Status.valueOf(rs.getString(columnName));
                break;
        }
        setter.invoke(entity, obj);
    }

    private Method getGetter(Class<T> clazz, String fieldName) throws NoSuchMethodException {
        return clazz.getDeclaredMethod("get" + fixCasing(fieldName));
    }
    private Method getSetter(Class<T> clazz, Field field) throws NoSuchMethodException {
        return clazz.getDeclaredMethod("set" + fixCasing(field.getName()), field.getType());
    }

    // The field.getName() preserves the casing used in declaration.
    // May the Gods have mercy on my soul if I start randomly naming my getters and setters....
    private String fixCasing(String original) {
        return Character.toUpperCase(original.charAt(0)) + original.substring(1);
    }
}
