package com.beckhamd.nasaimageryfetcher.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class BackgroundJobCreator implements JobCreator {
    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case BackgroundJob.TAG:
                return new BackgroundJob();
            default:
                return null;
        }
    }
}
