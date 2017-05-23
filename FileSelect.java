package com.meinc.mysecondapp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileSelect extends AppCompatActivity{
   public RecordingArrayAdapter<String> mAdapter=null;
   public ListView mListView=null;
   public ArrayList<String> mFiles=null;
   public RecordingListView mRecordingListView=null;
   public EditText mCurrentFile=null;

   /**
    * Call super and capture instance state
    * @param savedInstanceState
    */
   @Override
   protected void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_file_list);
   }

   /**
    * [1] Get Intent with current filename and display the name
    * [2] Get and display list of available files to select
    * [3] Setup a 'Click' listener to process user selections
    */
   @Override
   protected void onResume(){
      super.onResume();

      /** get current filename (passed from main app) and display it */
      Intent intent=getIntent();
      String defaultFileName=intent.getStringExtra(MainActivity.CURRENT_FN_KEY);
      mCurrentFile=(EditText)findViewById(R.id.fileNameText);
      mCurrentFile.setText(defaultFileName);

      /** get the list of recording files in the app directory */
      mFiles=new ArrayList<String>();
      getRecordingNames(getFileStorageDir(MainActivity.APP_STORAGE),mFiles);

      /** display the list of files in the directory */
      mListView=(ListView)findViewById(R.id.fileListView);
      mAdapter=new RecordingArrayAdapter<String>(this,R.layout.file_slot,R.id.recordEntryView,mFiles);
      mListView.setAdapter(mAdapter);

      /** create a click listener to process when the user touches on a filename
       *  The RecordingListView class implements the OnItemClickListener interface
       *  RecordingListView.onItemClick() services the click       */
      mRecordingListView=new RecordingListView(this,null);
      mRecordingListView.setCurrentFile(mCurrentFile);
      mListView.setOnItemClickListener(mRecordingListView);
      mListView.setOnItemLongClickListener(mRecordingListView);
   }

   /**
    * get all files in directory that have '.m2a' as the last 4 characters
    * @param file The File object with the directory to scan
    * @param files A String array to contain the filenames
    */
   public void getRecordingNames(File file,ArrayList<String> files){
      String[] filelist=file.list(new FilenameFilter(){
         @Override
         public boolean accept(File dir,String name){
            return name.matches("^(.*)\\.m2a$");
         }
      });

      int i=0;
      while(i<filelist.length){
         files.add(filelist[i]);
         i++;
      }
   }

   /**
    * If the application data path doesn't exist then create it.
    * Return a File object for the path.
    * @param audioDirName  The path to the data
    * @return File object for the path
    */
   public File getFileStorageDir(String audioDirName){
      File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),audioDirName);
      if(!file.exists()){
         if(!file.mkdirs()){
            Log.e(MainActivity.LOG_TAG,"Failed to create storage directory location");
         }
      }
      return file;
   }

   /**
    * If the user cancels the file selection then do nothing but return
    * @param view The View for the Cancel button
    */
   public void onCancel(View view){
      setResult(MainActivity.RESULT_CANCELED);
      finish();
   }

   /**
    * Get the selected recording name and pass it back to the parent before exiting.
    * @param view The View for the Ok button
    */
   public void onOk(View view){
      Intent intent=new Intent(this,MainActivity.class);
      EditText currentFile=(EditText)findViewById(R.id.fileNameText);
      String defaultFileName=currentFile.getText().toString();
      intent.putExtra(MainActivity.CURRENT_FN_KEY,defaultFileName);
      setResult(MainActivity.RESULT_OK,intent);
      finish();
   }
}
