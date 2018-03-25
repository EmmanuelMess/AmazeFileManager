package com.amaze.filemanager.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ChangedPreferencesUpdater {

    private static ChangedPreferencesUpdater changedPreferencesUpdater;

    /**
     * @param create ONLY USE IF YOU CAN CHANGE PREFERENCES
     */
    public static ChangedPreferencesUpdater getInstance(boolean create) {
        if(create && changedPreferencesUpdater == null) {
            changedPreferencesUpdater = new ChangedPreferencesUpdater();//lazy creation
        }
        return changedPreferencesUpdater;
    }

    private HashSet<String> changedKeys = new HashSet<>();

    private ChangedPreferencesUpdater() {}

    public Set<String> getChangedKeys() {
        return changedKeys;
    }

    public void addChangedKey(String key) {
        changedKeys.add(key);
    }

    public void destroyInternalReference() {
        changedPreferencesUpdater = null;
    }

}
