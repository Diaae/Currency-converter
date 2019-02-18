package com.example.diaaebakri.currencyconverter;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;

public class TimeService extends JobService {

    private boolean jobStopped = false;
    jobAsyncTask jobAsyncTask = new jobAsyncTask(this);

    @Override
    public boolean onStartJob(JobParameters params) {
        jobAsyncTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobStopped = true;
        return true;
    }

    private class jobAsyncTask extends AsyncTask<JobParameters, Void, JobParameters> {
        JobService jobService;
        Thread t1;
        ExchangeRateUpdateRunnable updater;

        public jobAsyncTask(JobService service){
            this.jobService = service;
        }


        @Override
        protected JobParameters doInBackground(JobParameters... jobParameters) {
            t1 = new Thread(updater);
            t1.start();
            return jobParameters[0];
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            jobService.jobFinished(jobParameters, false);
        }
    }



}
