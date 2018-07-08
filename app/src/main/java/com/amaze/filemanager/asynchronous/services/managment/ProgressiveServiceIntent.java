package com.amaze.filemanager.asynchronous.services.managment;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.amaze.filemanager.asynchronous.services.AbstractProgressiveService;

public class ProgressiveServiceIntent<T extends AbstractProgressiveService> extends Intent {
    public ProgressiveServiceIntent(@NonNull Context context, Class<T> clazz) {
        super(context, clazz);
    }
}
