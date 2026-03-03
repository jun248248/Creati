package com.creati.model;

import java.util.concurrent.atomic.AtomicReference;

import com.creati.ui.main.Services;

public final class AppState {

    private static final AppState INSTANCE = new AppState();

    private final AtomicReference<User> currentUserRef = new AtomicReference<>();
    private final AtomicReference<String> selectedLogIdRef = new AtomicReference<>();

    private AppState() {}

    public static AppState get() {
        return INSTANCE;
    }

    public User getCurrentUser() {
        return currentUserRef.get();
    }

    public void setCurrentUser(User user) {
        currentUserRef.set(user);
    }

    
    public String getSelectedLogId() {
        return selectedLogIdRef.get();
    }

    public void setSelectedLogId(String logId) {
        selectedLogIdRef.set(logId);
    }

    public void clearSelectedLog() {
        selectedLogIdRef.set(null);
    }

    
    public LogPost getSelectedLog() {
        String id = getSelectedLogId();
        if (id == null || id.isBlank()) return null;
        return Services.LOGS.getById(id);
    }
}
