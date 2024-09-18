package org.example.work1;

public class UserSession {
    private static UserSession instance;
    private int userId;

    private UserSession(int userId) {
        this.userId = userId;
    }

    public static UserSession getInstance(int userId) {
        if (instance == null) {
            instance = new UserSession(userId);
        }
        return instance;
    }

    public static UserSession getInstance() {
        return instance;
    }

    public int getUserId() {
        return userId;
    }
}

