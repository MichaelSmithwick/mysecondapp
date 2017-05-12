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
import android.widget.EditText;
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
   private boolean playBackGuard=false;

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
   public void showInfo(TextView textView){
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
    * threadStop must be set to false to permit the recording of sound until
    * the 'Stop Recording' button is pushed. The Stop Recording button handler
    * 'stopRecord()' will set 'threadStop' true to stop the recording process
    * and close the recording thread.
    * @param view
    */
   public void startRecord(View view){
      threadStop=false;
      loopback();
   }

   /**
    * Add the string to the UI. The string is appended to the current displayed data.
    * @param addString string to add to the current UI
    */
   public void updateMessageBoard(String addString){
      EditText textView=(EditText)findViewById(R.id.informationBox);
      String current=textView.getText().toString();
      current+=addString;
      textView.setText(current);
   }

   /**
    * This function sets up the microphone input and speaker output,
    * and runs the recording of sound in a thread.
    * The recorded sound is stored in a file. After the user ends the
    * recording the file is read back and played through the speakers.
    */
   protected void loopback(){
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
      // The thread that will capture, store, and replay the sound is created here
      // The Runnable object's run() function is created here but will be called
      // when the thread is started later on.
      Rthread=new Thread(new Runnable(){
         @Override
         public void run(){
            record();
            while(!threadStop);
            playBack();
         }
      });
      // The thread is started here. It calls the run() function defined above.
      Rthread.start();
   }

   /**
    * This function is called when the user selects 'Stop Recording'
    * This function sets the 'threadStop' flag to true, interrupting the
    * recording loop in the loopback thread
    * @param view
    */
   public void stopRecord(View view){
      updateMessageBoard("\nthreadStop="+threadStop+"\nstopRecord called");
      threadStop=true;
   }

   /**
    * Playback the previously selected recording
    * @param view
    */
   public void playBackRecording(View view){
      playBack();
   }

   /**
    * Record to a file previously selected by the user
    */
   public void record(){
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
      final int bufferSize=AudioRecord.getMinBufferSize(M2A_FREQ,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
      final AudioRecord audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,M2A_FREQ,AudioFormat.CHANNEL_IN_MONO,MediaRecorder.AudioEncoder.AMR_NB,bufferSize);
      final byte[] buffer=new byte[bufferSize];
      Thread rThread=new Thread(new Runnable(){
         // The run function opens a data file, captures sound from the microphone
         // and saves it to the opened file.
         // When the user selects the 'Stop Recording' button the 'threadStop'
         // flag is set to true and the capture loop ends.
         // The file is flushed and closed.
         public void run(){
            final File outFile = new File(storagePath,"record1.m2a");
            try{
               FileOutputStream outFileStream=new FileOutputStream(outFile);
               // threadStop is set by stopRecord() function
               while(!threadStop){
                  try{
                     audioRecord.read(buffer,0,bufferSize);
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
               // end audio capture segment
            }catch(FileNotFoundException e){
               Log.e(LOG_TAG,"Unable to open output file");
               e.printStackTrace();
            }
         }
      });

      rThread.start();
  }

   /**
    * Playback the file that has been previously selected by the user
    */
   public void playBack(){
      if(playBackGuard) return;
      playBackGuard=true;
      final int bufferSize=1024;
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
      audioTrack=new AudioTrack(AudioManager.ROUTE_SPEAKER,M2A_FREQ,AudioFormat.CHANNEL_OUT_MONO,MediaRecorder.AudioEncoder.AMR_NB,bufferSize,AudioTrack.MODE_STREAM);
      audioTrack.setPlaybackRate(M2A_FREQ);
      final byte[] buffer=new byte[bufferSize];
      audioTrack.play();

      Thread pThread=new Thread(new Runnable(){
         @Override
         public void run(){
            final File outFile = new File(storagePath,"record1.m2a");
            try{
               FileInputStream inFileStream=new FileInputStream(outFile);
               int bytesRead=0;
               while(bytesRead>-1){
                  try{
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
               Log.e(LOG_TAG,"File Not Found");
               e.printStackTrace();
            }
            playBackGuard=false;
         }
      });

      pThread.start();
   }
}