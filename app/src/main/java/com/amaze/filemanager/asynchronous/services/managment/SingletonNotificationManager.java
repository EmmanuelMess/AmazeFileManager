package com.amaze.filemanager.asynchronous.services.managment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SingletonNotificationManager {

    private static final SingletonNotificationManager instance = new SingletonNotificationManager();

    public static SingletonNotificationManager getInstance() {
        return instance;
    }

    private final List<AbstractProgressiveServiceNotifier> notifiers = Collections.synchronizedList(new ArrayList<>());

    private SingletonNotificationManager() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(new UpdateNotifiersThread(), 0, 900, TimeUnit.MILLISECONDS);
    }

    //package-private on purpose
    void addNotifier(AbstractProgressiveServiceNotifier notifier) {
        notifiers.add(notifier);
    }

    private static class UpdateNotifiersThread extends Thread {
        @Override
        public void run() {
            SingletonNotificationManager manager = SingletonNotificationManager.getInstance();
            synchronized (manager.notifiers) {
                for (int i = 0; i < manager.notifiers.size(); i++) {
                    AbstractProgressiveServiceNotifier notifier = manager.notifiers.get(i);

                    notifier.updateState();

                    if(notifier.isFinished()) {
                        manager.notifiers.remove(i);
                    }
                }
            }
        }
    }

}
