package com.meinc.mysecondapp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
   public ArrayAdapter<String> adapter=null;
   public ListView listView=null;
   public ArrayList<String> files=null;
   public EditText currentFile=null;

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

      /** get current filename and display it */
      Intent intent=getIntent();
      String defaultFileName=intent.getStringExtra(MainActivity.CURRENT_FN_KEY);
      currentFile=(EditText)findViewById(R.id.fileNameText);
      currentFile.setText(defaultFileName);

      /** get list of recording files in the directory and display */
      files=new ArrayList<String>();
      listView=(ListView)findViewById(R.id.fileListView);
      getRecordingNames(getFileStorageDir(MainActivity.APP_STORAGE),files);
      adapter=new ArrayAdapter<String>(this,R.layout.file_slot,R.id.recordEntryView,files);
      listView.setAdapter(adapter);
      listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
         @Override
         public void onItemClick(AdapterView<?> parent,View view,int position,long id){
            final String selectedItem=(String)parent.getItemAtPosition(position);
            currentFile.setText(selectedItem);
            //onSelectRecordingName(view,position,id);
         }
      });
   }

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

   public void onSelectRecordingName(View view,int position, long id){
      ListView listView=(ListView)findViewById(R.id.fileListView);
      String selected=listView.toString();
   }
}
