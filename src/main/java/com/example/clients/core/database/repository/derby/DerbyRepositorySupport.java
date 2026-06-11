package com.example.clients.core.database.repository.derby;

import com.example.clients.core.database.Database;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

abstract class DerbyRepositorySupport {

    protected final Database database;

    DerbyRepositorySupport(Database database) {
        this.database = database;
    }

    protected void setUuid(PreparedStatement statement, int index, UUID value) throws SQLException {
        statement.setString(index, value == null ? null : value.toString());
    }

    protected UUID getUuid(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null ? null : UUID.fromString(value);
    }

    protected void setDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        statement.setDate(index, value == null ? null : Date.valueOf(value));
    }

    protected LocalDate getDate(ResultSet resultSet, String column) throws SQLException {
        Date value = resultSet.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    protected void setTimestamp(PreparedStatement statement, int index, LocalDateTime value) throws SQLException {
        statement.setTimestamp(index, value == null ? null : Timestamp.valueOf(value));
    }

    protected LocalDateTime getTimestamp(ResultSet resultSet, String column) throws SQLException {
        Timestamp value = resultSet.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    protected RuntimeException repositoryException(String message, SQLException e) {
        return new RuntimeException(message, e);
    }
}
