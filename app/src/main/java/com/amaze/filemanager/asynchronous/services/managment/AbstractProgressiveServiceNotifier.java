package com.amaze.filemanager.asynchronous.services.managment;

import com.amaze.filemanager.asynchronous.services.AbstractProgressiveService;

public abstract class AbstractProgressiveServiceNotifier {
    protected boolean isFinished;

    private final AbstractProgressiveService service;
    private boolean hasStarted;

    public AbstractProgressiveServiceNotifier(AbstractProgressiveService service) {
        this.service = service;
    }

    public boolean isFinished() {
        return isFinished;
    }

    //package-private on purpose
    void updateState() {
        if(!hasStarted) {
            start();
            hasStarted = true;
        }

        ProgressiveState state = service.getState();

        if(!state.hasFinished) {
            update(state);
        } else {
            finish();
        }
    }

    protected abstract void start();

    protected abstract void update(ProgressiveState state) ;

    protected abstract void finish();

    public static class Helper {

    }

}
