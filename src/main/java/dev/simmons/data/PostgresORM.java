package dev.simmons.data;

import dev.simmons.annotation.DBEntity;
import dev.simmons.annotation.DbField;
import dev.simmons.annotation.PrimaryKey;
import dev.simmons.exceptions.NoSuchEntityException;
import dev.simmons.utilities.connection.PostgresConnection;
import dev.simmons.utilities.logging.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class PostgresORM<T> implements DataWrapperORM<T>{

    protected List<Field> fields;
    protected Field primaryKey;
    protected String table;

    protected final String createSql;
    protected final String getByIdSql;
    protected final String getAllSql;
    protected final String updateSql;
    protected final String deleteSql;

    public PostgresORM() {
        Class c = (Class)(((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        if (c.isAnnotationPresent(DBEntity.class)) {
            table = ((DBEntity)c.getAnnotation(DBEntity.class)).value();
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

        fields = Arrays.asList(c.getDeclaredFields());
        for (int i = 0; i < fields.size(); i++) {
            DbField dbField = fields.get(i).getAnnotation(DbField.class);
            if (fields.get(i).isAnnotationPresent(PrimaryKey.class)) {
                index = getByIdStatement.indexOf("&&");
                getByIdStatement.replace(index, index + 2, dbField.name());

                index = updateStatement.indexOf("&&");
                updateStatement.replace(index, index + 2, dbField.name());

                index = deleteStatement.indexOf("&&");
                deleteStatement.replace(index, index + 2, dbField.name());
            } else {
                index = createStatement.indexOf("--");
                createStatement.replace(index, index + 2, dbField.name() + ", --");

                index = updateStatement.indexOf("<>");
                updateStatement.replace(index, index + 2, dbField.name() + " = ?, <>");
            }
        }

        index = createStatement.indexOf("--");
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
            for (int i = 0; i < fields.size(); i++) {
                if (fields.get(i).isAnnotationPresent(PrimaryKey.class)) {
                    idField = fields.get(i);
                    continue;
                }

                Object obj = fields.get(i).get(entity);

                switch(fields.get(i).getAnnotation(DbField.class).type().name()) {
                    case "BIG_INT":
                        if (obj == null) {
                            statement.setNull(index, Types.BIGINT);
                        } else {
                            statement.setLong(index, (Long) obj);
                        }
                        break;
                    case "INT":
                        if (obj == null) {
                            statement.setNull(index, Types.INTEGER);
                        } else {
                            statement.setInt(index, (Integer) obj);
                        }
                        break;
                    case "STRING":
                        if (obj == null) {
                            statement.setNull(index, Types.VARCHAR);
                        } else {
                            statement.setString(index, obj.toString());
                        }
                        break;
                    case "FLOAT":
                        if (obj == null) {
                            statement.setNull(index, Types.FLOAT);
                        } else {
                            statement.setFloat(index, (Float) obj);
                        }
                        break;
                }

                index++;
            }

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "Failed to create " + table.toUpperCase() + " using values: " + entity);
            }

            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            int id = rs.getInt(1);
            if (idField == null) {
                Logger.log(Logger.Level.WARNING, "No id field found in field list.");
                return null;
            }
            idField.setInt(entity, id);

            return entity;
        } catch (IllegalAccessException | IllegalArgumentException iae) {
            Logger.log(Logger.Level.ERROR, iae);
        } catch (SQLException se) {
            throw se;
        }

        return null;
    }

    @Override
    public T getEntityById(int id) throws SQLException {
        try (Connection conn = PostgresConnection.getConnection()) {

        } catch (SQLException se) {
            throw se;
        }
        return null;
    }

    @Override
    public List<T> getAllEntities() throws SQLException {
        return null;
    }

    @Override
    public T replaceEntity(T entity) throws SQLException {
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
        } catch (SQLException se) {
            throw se;
        }
    }

    public enum DataTypes {
        BIG_INT, INT, STRING, FLOAT
    }
}
