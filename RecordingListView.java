package com.meinc.mysecondapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;

import static com.meinc.mysecondapp.R.color.itemSelected;

/**
 * Created by Mike on 5/20/2017.
 */

public class RecordingListView extends AdapterView<Adapter> implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
   private Adapter mAdapter=null;
   private int mTouchStartY=0;
   private int mListTopStart=0;
   private int mListTop=0;
   private EditText mCurrentFile=null;
   private View mPrevView=null;

   public RecordingListView(Context context,AttributeSet attributeSet){
      super(context,attributeSet);
   }

   public void setCurrentFile(EditText currentFile){
      mCurrentFile=currentFile;
   }

   @Override
   public Adapter getAdapter(){
      return mAdapter;
   }

   @Override
   public void setAdapter(Adapter adapter){
      mAdapter=adapter;
      removeAllViewsInLayout();
      requestLayout();
   }

   @Override
   public View getSelectedView(){
      throw new UnsupportedOperationException("Not Supported");
      //return null;
   }

   @Override
   public void setSelection(int position){
      throw new UnsupportedOperationException("Not Supported");
   }

   @Override
   protected void onLayout(boolean changed,int left,int top,int right,int bottom){
      super.onLayout(changed,left,top,right,bottom);
      if(mAdapter==null){
         return;
      }

      if(getChildCount()==0){
         int position=0;
         int bottomEdge=0;
         while(bottomEdge<getHeight() && position<mAdapter.getCount()){
            View newBottomChild=mAdapter.getView(position,null,this);
            addAndMeasureChild(newBottomChild);
            bottomEdge=newBottomChild.getMeasuredHeight();
            position++;
         }
      }

      positionItems();
   }

   private void addAndMeasureChild(View view){
      LayoutParams params=view.getLayoutParams();
      if(params==null){
         params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
      }
      addViewInLayout(view,-1,params,true);

      int itemWidth=getWidth();
      view.measure(MeasureSpec.EXACTLY|itemWidth,MeasureSpec.UNSPECIFIED);
   }

   private void positionItems(){
      //int top=0;
      int top=mListTop;

      for(int i=0;i<getChildCount();i++){
         View child=getChildAt(i);

         int width=child.getMeasuredWidth();
         int height=child.getMeasuredHeight();
         int left=(getWidth()-width)/2;

         child.layout(left,top,left+width,top+height);
         top+=height;
      }
   }

   @Override
   public boolean onTouchEvent(MotionEvent motionEvent){
      if(getChildCount()==0){
         return false;
      }

      switch(motionEvent.getAction()){
         case MotionEvent.ACTION_DOWN:
            mTouchStartY=(int)motionEvent.getY();
            mListTopStart=getChildAt(0).getTop();
            break;
         case MotionEvent.ACTION_MOVE:
            int scrolledDistance=(int)motionEvent.getY()-mTouchStartY;
            mListTop=mListTopStart+scrolledDistance;
            requestLayout();
            break;
         default:
            break;
      }

      return true;
   }

   /**
    * This function is needed to implement OnItemClickListener interface
    * It is called when an item in the list box is selected (clicked).
    * @param parent  The AdapterView where the click happened.
    * @param view  The view within the AdapterView that was clicked (this
    *            will be a view provided by the adapter)
    * @param position  The position of the view in the adapter.
    * @param id  The row id of the item that was clicked.
    */
   @Override
   public void onItemClick(AdapterView<?> parent, View view, int position, long id){
      parent.setBackgroundColor(getResources().getColor(R.color.parentWhite));
      if(mPrevView!=null){
         mPrevView.setBackgroundColor(getResources().getColor(R.color.parentWhite));
      }
      mPrevView=view;
      final String selectedItem=(String)parent.getItemAtPosition(position);
      mCurrentFile.setText(selectedItem);
      view.setBackgroundColor(getResources().getColor(R.color.itemSelected));
   }

   @Override
   public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
      return true; /** long click was consumed */
   }
}

