package com.amaze.filemanager.asynchronous.services.managment;

import android.content.Context;
import android.support.annotation.NonNull;

import com.amaze.filemanager.asynchronous.services.AbstractProgressiveService;

import java.lang.ref.WeakReference;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class manages the serialization of {@link AbstractProgressiveService}s
 * Thread safe
 */
public class SingletonProgressiveServiceManager {

    /**
     * How many threads to run in parallel
     */
    private static final int MULTITHREADING_FACTOR = 1;

    private static final SingletonProgressiveServiceManager instance = new SingletonProgressiveServiceManager();

    public static SingletonProgressiveServiceManager getInstance() {
        return instance;
    }

    private final BlockingQueue<ProgressiveServiceJob> jobs = new ArrayBlockingQueue<>(10);

    private WeakReference<Context> applicationContext = null;

    private SingletonProgressiveServiceManager() {
        ExecutorService executor = Executors.newFixedThreadPool(MULTITHREADING_FACTOR);
        executor.submit(new ProgressiveServiceIntentRunnable());
    }

    public synchronized void setApplicationContext(@NonNull Context context) {
        this.applicationContext = new WeakReference<>(context);
    }

    public void addService(@NonNull ProgressiveServiceJob job) {
        jobs.add(job);
    }

    private static class ProgressiveServiceIntentRunnable implements Runnable {
        @Override
        public void run() {
            final SingletonProgressiveServiceManager manager = SingletonProgressiveServiceManager.getInstance();

            while (!manager.jobs.isEmpty()) {
                try {
                    synchronized (manager) {
                        final Context context = manager.applicationContext.get();

                        if (context == null) {
                            throw new IllegalStateException("Application context is null!");
                        }

                        ProgressiveServiceJob job = manager.jobs.take();
                        context.startService(job.intent);

                        SingletonNotificationManager notification = SingletonNotificationManager.getInstance();
                        notification.addNotifier(job.notifier);
                    }
                } catch (InterruptedException | IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
