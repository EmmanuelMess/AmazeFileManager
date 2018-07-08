package com.amaze.filemanager.asynchronous.services.managment;

public class ProgressiveServiceJob {

    public final ProgressiveServiceIntent intent;
    public final AbstractProgressiveServiceNotifier notifier;

    public ProgressiveServiceJob(ProgressiveServiceIntent intent, AbstractProgressiveServiceNotifier notifier) {
        this.intent = intent;
        this.notifier = notifier;
    }
}
