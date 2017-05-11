package com.meinc.mysecondapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Static members of the MainActivity class may be prefixed with 'M2A' to distinguish them
 * from possible global variable collision  --  This is for human readability not the
 * compiler.
 */
public class MainActivity extends AppCompatActivity{
   private static final String LOG_TAG="com.meinc.mysecondapp";
   private static final int M2A_FREQ=44100;

   private int counter;
   private Thread Rthread;

   // AudioManager permits volume changes, ring changes, and other interface info and changes
   //private AudioManager audioManager;
   private AudioTrack audioTrack;
   private boolean threadStop;
   private File storagePath;

   /**
    * Sets up initial UI
    * @param savedInstanceState
    */
   @Override
   protected void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
   }

   /**
    * Display initial status information on startup and resume
    */
   @Override
   protected void onResume(){
      super.onResume();
      showInfo((TextView)findViewById(R.id.informationBox));
   }

   /**
    * Initializes all variables in the MainActivity object
    * Sets the default data storage path to dogWhistle
    */
   public MainActivity(){
      counter=0;
      Rthread=null;
      //audioManager=null;
      audioTrack=null;
      threadStop=false;
      storagePath=getAudioStorageDir("dogWhistle");
   }

   /**
    * Pushing the Check Permissions button calls this function
    * @param view
    */
   public void checkNow(View view){
      showInfo((TextView)findViewById(R.id.informationBox));
   }

   /**
    * displays information about:
    *    How many times this has been called since app initialization or resume
    *    If this app has permission to use the microphone and record sound input
    *    If external storage is available and writeable
    *    The path to the app data
    * @param textView The TextView to send the data to
    */
   private void showInfo(TextView textView){
      counter++;
      String infoString=counter+"\n"+this.getText(R.string.hello_world)+"\n";
      textView.setText(infoString);

      int permissionCheck=ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
      if(permissionCheck== PackageManager.PERMISSION_GRANTED){
         infoString+=this.getString(R.string.audioRecordPermYes);
      }else{
         infoString+=this.getString(R.string.audioRecordPermNo);
      }

      infoString+="\nExternal Media Available="+isExternalStorageWriteable();
      infoString+="\n"+storagePath.getAbsolutePath();
      textView.setText(infoString);
   }

   /**
    * Determines if external storage can be written (if present and mounted)
    * @return
    */
   private boolean isExternalStorageWriteable(){
      String state=Environment.getExternalStorageState();
      if(Environment.MEDIA_MOUNTED.equals(state)){
         return true;
      }
      return false;
   }

   /**
    * Creates the audio storage directory if it doesn't exist
    * @param audioDirName
    * @return the File object created -- must be closed by caller
    * @note This function is called in constructor so no resources can be used.
    */
   private File getAudioStorageDir(String audioDirName){
      File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),audioDirName);
      if(!file.exists()){
         if(!file.mkdirs()){
            Log.e(LOG_TAG,"Failed to create storage directory location");
         }
      }
      return file;
   }

   /**
    * This function is called when the Record Sound button is pushed
    * @param view
    */
   protected void startRecord(View view){
      threadStop=false;
      loopback();
   }

   /**
    * Add the string to the UI. The string is appended to the current displayed data.
    * @param addString string to add to the current UI
    */
   private void updateMessageBoard(String addString){
      TextView textView=(TextView)findViewById(R.id.informationBox);
      String current=(String)textView.getText();
      current+=addString;
      textView.setText(current);
   }

   protected void loopback(){
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

      final int bufferSize=AudioRecord.getMinBufferSize(M2A_FREQ,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
      final AudioRecord audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,M2A_FREQ,AudioFormat.CHANNEL_IN_MONO,MediaRecorder.AudioEncoder.AMR_NB,bufferSize);
      audioTrack=new AudioTrack(AudioManager.ROUTE_SPEAKER,M2A_FREQ,AudioFormat.CHANNEL_OUT_MONO,MediaRecorder.AudioEncoder.AMR_NB,bufferSize,AudioTrack.MODE_STREAM);

      audioTrack.setPlaybackRate(M2A_FREQ);
      final byte[] buffer=new byte[bufferSize];
      audioRecord.startRecording();
      Log.i(this.getString(R.string.LOG_TAG),this.getString(R.string.audioRecordStart));
      updateMessageBoard("\n");
      updateMessageBoard(this.getString(R.string.audioRecordStart));
      audioTrack.play();
      Log.i(this.getString(R.string.LOG_TAG),this.getString(R.string.audioPlayStart));
      updateMessageBoard("\n");
      updateMessageBoard(this.getString(R.string.audioPlayStart));
      updateMessageBoard("\nthreadStop="+threadStop);
      Rthread=new Thread(new Runnable(){
         @Override
         public void run(){
            final File outFile = new File(storagePath,"record1.m2a");
            try{
               FileOutputStream outFileStream=new FileOutputStream(outFile);
               while(!threadStop){
                  try{
                     audioRecord.read(buffer,0,bufferSize);
                     //audioTrack.write(buffer,0,bufferSize);
                     /** @note the stream will always append data so offset is always 0 */
                     outFileStream.write(buffer,0,bufferSize);
                  }catch(Throwable T){
                     Log.e(LOG_TAG,"Read/Write Failed");
                     T.printStackTrace();
                  }
               }
               audioRecord.stop();
               audioRecord.release();
               try{
                  outFileStream.close();
               }catch(IOException e){
                  e.printStackTrace();
               }

               FileInputStream inFileStream=new FileInputStream(outFile);
               int bytesRead=0;
               while(bytesRead>-1){
                  try{
                     /** @note stream will automatically move read pointer so offset can always be 0 */
                     bytesRead=inFileStream.read(buffer,0,bufferSize);
                     audioTrack.write(buffer,0,bufferSize);
                  }catch(IOException e){
                     e.printStackTrace();
                  }
               }
               audioTrack.flush();
               audioTrack.stop();
               audioTrack.release();
            }catch(FileNotFoundException e){
               Log.e("com.meinc.Error","Unable to open output file");
               e.printStackTrace();
            }
         }
      });
      Rthread.start();
   }

   protected void stopRecord(View view){
      updateMessageBoard("\nthreadStop="+threadStop+"\nstopRecord called");
      threadStop=true;
   }
}

