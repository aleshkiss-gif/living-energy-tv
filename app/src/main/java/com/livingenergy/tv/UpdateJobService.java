package com.livingenergy.tv;
import android.app.job.*;
public class UpdateJobService extends JobService {
 private Thread worker;
 @Override public boolean onStartJob(JobParameters p){if(!UpdateManager.prefs(this).getBoolean("automatic",true))return false;worker=new Thread(()->{UpdateManager.fetch(getApplicationContext());jobFinished(p,false);},"WallpaperUpdate");worker.start();return true;}
 @Override public boolean onStopJob(JobParameters p){if(worker!=null)worker.interrupt();return true;}
}
