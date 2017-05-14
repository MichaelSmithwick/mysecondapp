package com.meinc.mysecondapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class FileSelect extends AppCompatActivity{

   @Override
   protected void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_file_list);

      Intent intent=getIntent();
      String defaultFileName=intent.getStringExtra(MainActivity.CURRENT_FN_KEY);
      EditText currentFile=(EditText)findViewById(R.id.fileNameText);
      currentFile.setText(defaultFileName);
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
