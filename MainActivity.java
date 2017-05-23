package com.meinc.mysecondapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

/**
 * Static members of the MainActivity class may be prefixed with 'M2A' to distinguish them
 * from possible global variable collision  --  This is for human readability not the
 * compiler.
 */
public class MainActivity extends AppCompatActivity{
   public static final int ERROR_NONE=0;
   public static final int ERROR_AUDIO_PERM=1001;
   public static final int ERROR_FILE_PERM=1002;
   public static final int ERROR_FILE_READ=1003;
   public static final String LOG_TAG="com.meinc.mysecondapp";
   public static final String CURRENT_FN_KEY="com.meinc.mysecondapp.CURRENTFN";
   public static final String APP_STORAGE_KEY="comm.meinc.mysecondapp.APPSTORAGE";
   public static final int GETFILENAME=1001;
   private static final int M2A_FREQ=44100;
   public static final String APP_STORAGE="dogWhistle";
   private static final String DEFAULT_FN="record1.m2a";
   private String CURRENT_FN=DEFAULT_FN;

   private boolean playBackGuard=false; /** permits only single access to playback mechanism */
   private int counter=0; /** Counts how many times the 'Check Permissions' button has been pushed */
   private boolean threadStop=false; /** Stops the sound input loop */
   private File storagePath=null; /** Holds the storage path for the application */

   public String errorMessage="";
   public int errorNumber=ERROR_NONE;

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
      threadStop=false;
      storagePath=getAudioStorageDir(APP_STORAGE);
   }

   /**
    * Pushing the 'Check Permissions' button calls this function
    * @param view The view that called the function? i.e. the Button??
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
      updateMessageBoard("\n"+CURRENT_FN);
   }

   /**
    * Called when the user 'Set/Select Recording' button
    * This activity uses the GETFILENAME code for return values
    * @param view
    */
   public void selectActiveFile(View view){
      if(isExternalStorageWriteable()){
         Intent intent=new Intent(this,FileSelect.class);
         intent.putExtra(CURRENT_FN_KEY,CURRENT_FN);
         intent.putExtra(APP_STORAGE_KEY,APP_STORAGE);
         startActivityForResult(intent,GETFILENAME);
      }
   }

   /**
    * Called on return from the file selection activity
    * If RESULT_CANCELED is returned do nothing.
    * If RESULT_OK is returned set the current filename to the returned filename
    * @param requestCode This will be the number sent to the activity to identify it
    * @param resultCode The result of the operation, RESULT_OK or RESULT_CANCELED
    * @param intent The intent holding the returned filename
    */
   @Override
   protected void onActivityResult(int requestCode,int resultCode,Intent intent){
      super.onActivityResult(requestCode,resultCode,intent);
      if(requestCode==GETFILENAME){
         if(resultCode==RESULT_OK){
            CURRENT_FN=intent.getStringExtra(CURRENT_FN_KEY);
         }
         if(resultCode==RESULT_CANCELED){
            // do nothing
         }
         updateMessageBoard("\n"+CURRENT_FN);
      }
   }

   /**
    * Determines if external storage can be written (if present and mounted)
    * @return
    */
   private boolean isExternalStorageWriteable(){
      String state=Environment.getExternalStorageState();
      return Environment.MEDIA_MOUNTED.equals(state);
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
    * Add the string to the UI. The string is appended to the current displayed data.
    * @param addString string to add to the current UI
    */
   private void updateMessageBoard(String addString){
      EditText textView=(EditText)findViewById(R.id.informationBox);
      String current=textView.getText().toString();
      current+=addString;
      textView.setText(current);
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
      errorClear();
      threadStop=false;
      loopback();
      Log.e(LOG_TAG,"Calling errorDialog()1:errNumber["+errorNumber+"],errorMessage["+errorMessage+"]");
      errorDialog();
   }

   /**
    * This function sets up the microphone input and speaker output,
    * and runs the recording of sound in a thread.
    * The recorded sound is stored in a file. After the user ends the
    * recording the file is read back and played through the speakers.
    */
   private void loopback(){
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
      Thread Rthread=new Thread(new Runnable(){
         @Override
         public void run(){
            xRecord(CURRENT_FN);
            xPlayBack(CURRENT_FN,3*1024);
         }
      });
      Rthread.start();
   }

   /**
    * Opens the microphone and records input sound.
    * Saves it in the application data directory using the filename provided
    * @param filename The filename for the file storing the audio input data
    */
   private void xRecord(String filename){
      final int bufferSize=AudioRecord.getMinBufferSize(M2A_FREQ,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
      final AudioRecord audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,M2A_FREQ,AudioFormat.CHANNEL_IN_MONO,MediaRecorder.AudioEncoder.AMR_NB,bufferSize);
      try{
         audioRecord.startRecording();
      }catch(Exception e){
         e.getStackTrace();
         errorNumber=ERROR_AUDIO_PERM;
         errorMessage=e.getMessage();
         Log.e(LOG_TAG,e.getMessage());
         return;
      }
      final File outFile = new File(storagePath,filename);
      try{
         final byte[] buffer=new byte[bufferSize];
         FileOutputStream outFileStream=new FileOutputStream(outFile);
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
      }catch(FileNotFoundException e){
         Log.e("com.meinc.Error","Unable to open output file");
         e.printStackTrace();
      }
   }

   /**
    * Plays back  audio from the a file with the given filename stored in the application
    * data directory. Allocates a buffer of size buffersize for transferring data between
    * the file and audio output
    * @param filename The name of the audio file to read
    * @param bufferSize  The size of the transfer buffer to use (the size should be divisable by 3)
    */
   private void xPlayBack(String filename,int bufferSize){
      if(playBackGuard) return;
      playBackGuard=true;
      AudioTrack audioTrack=new AudioTrack(AudioManager.ROUTE_SPEAKER,M2A_FREQ,AudioFormat.CHANNEL_OUT_MONO,MediaRecorder.AudioEncoder.AMR_NB,bufferSize,AudioTrack.MODE_STREAM);
      audioTrack.setPlaybackRate(M2A_FREQ);
      audioTrack.play();
      final File inFile = new File(storagePath,filename);
      try{
         final byte[] buffer=new byte[bufferSize];
         FileInputStream inFileStream=new FileInputStream(inFile);
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
         Log.e(LOG_TAG,"Unable to open input file");
         e.printStackTrace();
         errorMessage=e.getMessage();
         errorNumber=ERROR_FILE_READ;
      }
      playBackGuard=false;
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
      errorClear();
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
      Thread pbThread=new Thread(new Runnable(){
         public void run(){
            xPlayBack(CURRENT_FN,3*1024);
         }
      });
      pbThread.start();
      Log.e(LOG_TAG,"Calling errorDialog()2:errNumber["+errorNumber+"],errorMessage["+errorMessage+"]");
      errorDialog();
   }

   public void errorClear(){
      errorMessage="";
      errorNumber=ERROR_NONE;
   }

   public void errorDialog(){
      Log.e(LOG_TAG,"Calling errorDialog():errNumber["+errorNumber+"],errorMessage["+errorMessage+"]");
      String message="";
      String title="";

      switch(errorNumber){
         case ERROR_NONE:
            return;
         case ERROR_FILE_PERM:
            title="Permission Violation";
            message="FILE PERMISSION\nPermissions Not Set\nTurn on File System access\n--\n"+errorMessage;
            break;
         case ERROR_FILE_READ:
            title="File Read Error";
            message="Unable to read file\nPossible Permission Problem\nOr file doesn't exist\n--\n"+errorMessage;
            break;
         case ERROR_AUDIO_PERM:
            title="Permission Violation";
            message="RECORD AUDIO\nPermissions Not Set\nTurn on Audio for this App\n--\n"+errorMessage;
            break;
         default:
            break;
      }
      new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok",null)
            .show();
   }
}