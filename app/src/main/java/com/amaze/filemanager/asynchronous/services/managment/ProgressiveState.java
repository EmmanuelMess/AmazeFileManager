package com.amaze.filemanager.asynchronous.services.managment;

import android.support.annotation.NonNull;

public class ProgressiveState {
    public final long position;
    public final boolean isHalted;
    public final boolean hasFinished;

    /**
     * For hack, read where used
     */
    public final boolean isDecrypt;


    public ProgressiveState(long position, boolean isHalted, boolean hasFinished, boolean isDecrypt) {
        this.position = position;
        this.isHalted = isHalted;
        this.hasFinished = hasFinished;
        this.isDecrypt = isDecrypt;
    }

    @Override
    @NonNull
    public ProgressiveState clone() {
        return new ProgressiveState(position, isHalted, hasFinished, isDecrypt);
    }

}
