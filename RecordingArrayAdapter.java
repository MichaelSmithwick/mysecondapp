package com.meinc.mysecondapp;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Mike on 5/16/2017.
 *
 * This class is for future use. This class will implement a custom view that will be used by
 * each recording name item in the selection list
 */

public class RecordingArrayAdapter<T> extends ArrayAdapter<T>{
   public RecordingArrayAdapter(@NonNull Context context,@LayoutRes int resource,@IdRes int textViewResourceId,@NonNull List objects){
      super(context,resource,textViewResourceId,objects);
   }

   @Override
   public String toString(){
      return super.toString();
   }

   @NonNull
   @Override
   public View getView(int position,@Nullable View convertView,@NonNull ViewGroup parent){
      return super.getView(position,convertView,parent);
   }
}
