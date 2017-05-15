package com.meinc.mysecondapp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class FileSelect extends AppCompatActivity{

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
    * Get Intent with current filename and display the name
    * Display list of available files to select
    */
   @Override
   protected void onResume(){
      super.onResume();
      ArrayList<String> files=new ArrayList<String>();
      int direction=View.FOCUS_FORWARD;
      Intent intent=getIntent();
      String defaultFileName=intent.getStringExtra(MainActivity.CURRENT_FN_KEY);
      EditText currentFile=(EditText)findViewById(R.id.fileNameText);
      currentFile.setText(defaultFileName);
      ListView listView=(ListView)findViewById(R.id.fileListView);
      files.add("Mike");
      files.add("Looked");
      files.add("Here");
      ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.file_slot,files);
      listView.setAdapter(adapter);
      //listView.addFocusables(views,direction);
   }

   public File getFileStorageDir(String audioDirName){
      File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),audioDirName);
      if(!file.exists()){
         if(!file.mkdirs()){
            Log.e(MainActivity.LOG_TAG,"Failed to create storage directory location");
         }
      }
      return file;
   }

   public void onCancel(View view){
      setResult(MainActivity.RESULT_CANCELED);
      finish();
   }

   public void onOk(View view){
      Intent intent=new Intent(this,MainActivity.class);
      EditText currentFile=(EditText)findViewById(R.id.fileNameText);
      String defaultFileName=currentFile.getText().toString();
      intent.putExtra(MainActivity.CURRENT_FN_KEY,defaultFileName);
      setResult(MainActivity.RESULT_OK,intent);
      finish();
   }
}
