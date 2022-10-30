package ru.kryu.kchat.kchatservergui;

import java.sql.*;

public class AuthService {
    private Connection connection;
    private Statement statement;
    private PreparedStatement psGetNick;
    private PreparedStatement psUserRegistration;

    public void checkTable() throws SQLException{
        statement.execute("CREATE TABLE IF NOT EXISTS users (\n" +
                "    id    INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    login TEXT    UNIQUE\n" +
                "                  NOT NULL,\n" +
                "    pass  INTEGER    NOT NULL,\n" +
                "    nick  TEXT    UNIQUE\n" +
                "                  NOT NULL\n" +
                ");");
    }

    public boolean userRegistration(String login, String pass, String nick) throws SQLException{
        int passHash = pass.hashCode();
        try {
            psUserRegistration.setString(1, login);
            psUserRegistration.setInt(2, passHash);
            psUserRegistration.setString(3, nick);
            return psUserRegistration.executeUpdate() == 1;
        }catch (SQLException e){
            e.printStackTrace();
            throw new SQLException("Ошибка регистрации пользователя");
        }

    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:mainServer.db");
        statement = connection.createStatement();
        checkTable();
        psGetNick = connection.prepareStatement("SELECT nick FROM users WHERE login = ? AND pass = ?;");
        psUserRegistration = connection.prepareStatement("INSERT INTO users (login, pass, nick) VALUES (?, ?, ?);");
        //userRegistration("l3","p3","Bak");
    }

    public String getNickByLoginAndPass(String login, String pass) {
        try {
            int passHash = pass.hashCode();
            psGetNick.setString(1, login);
            psGetNick.setInt(2, passHash);
            ResultSet rs = psGetNick.executeQuery();
            if (rs.next()) {
                return rs.getString("nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect() throws SQLException {
        psGetNick.close();
        psUserRegistration.close();
        statement.close();
        connection.close();
    }

}
