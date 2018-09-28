package com.beckhamd.nasaimageryfetcher.job;

import android.support.annotation.NonNull;

import com.beckhamd.nasaimageryfetcher.util.BackgroundUtils;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class BackgroundJob extends Job {
    public static final String TAG = "background_job_tag";

    @Override
    @NonNull
    protected Result onRunJob(@NonNull Params params) {
        BackgroundUtils.getLatestImages(getContext());
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        if (JobManager.instance().getAllJobRequestsForTag(BackgroundJob.TAG).size() < 1) {
            new JobRequest.Builder(BackgroundJob.TAG)
                    .setPeriodic(TimeUnit.HOURS.toMillis(12))
                    .setUpdateCurrent(true)
                    .build()
                    .schedule();
        }
    }
}
