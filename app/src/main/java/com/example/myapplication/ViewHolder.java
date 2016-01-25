package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 自定义多用ViewHodler
 */
public class ViewHolder {

	private final SparseArray<View> mViews;
	private View mConvertView;
	
	private int position;
	public ViewHolder(Context context, ViewGroup parent, int layoutId,
			int position) {
		// TODO Auto-generated constructor stub
		this.mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
				false);

		mConvertView.setTag(this);
		
		this.position = position;
	}

	public static ViewHolder get(Context context, View convertView,
			ViewGroup parent, int layoutId, int position) {
		if (convertView == null) {
			return new ViewHolder(context, parent, layoutId, position);
		}
		return (ViewHolder) convertView.getTag();
	}

	public <T extends View> T getView(int viewId) {
		// TODO Auto-generated method stub
		View view = mViews.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	public View getConvertView() {
		return mConvertView;
	}
	
	/**
	 * 给TextView及其子类设置文字内容
	 * @param viewId
	 * @param text
	 * @return
	 */
	public ViewHolder setText(int viewId, String text) {
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}
	
	/**
	 * 修改字體顏色
	 * @param viewId
	 * @param text
	 * @param color
	 * @return
	 */
	public ViewHolder setText(int viewId,String text,int color) {
		TextView view = getView(viewId);
		view.setTextColor(color);
		view.setText(text);
		return this;
	}

	public ViewHolder setHtmlText(int viewId,String text){
		TextView view = getView(viewId);
		view.setText(Html.fromHtml(text));
		return this;
	}

	public ViewHolder setTextBackground(int viewId,int drawableId){
		TextView view = getView(viewId);
		view.setBackgroundResource(drawableId);
		return this;
	}
	
	/** 
     * 为ImageView及其子类设置图片 
     * @param viewId 
     * @param drawableId 
     * @return 
     */  
    public ViewHolder setImageResource(int viewId, int drawableId)  
    {  
        ImageView view = getView(viewId);  
        view.setImageResource(drawableId);  
  
        return this;  
    }  
  
    public ViewHolder setImageBitmap(int viewId, Bitmap bm)
    {  
        ImageView view = getView(viewId);  
        view.setImageBitmap(bm);  
        return this;  
    }  
  
    public ViewHolder setChecked(int viewId,boolean check){
    	CompoundButton button = getView(viewId);
    	button.setChecked(check);
    	return this;
    }
    
    public ViewHolder setListener(int viewId,OnClickListener listener){
    	View view = getView(viewId);
    	view.setOnClickListener(listener);
    	return this;
    };
    
    public int getPosition() {
		return position;
	}
}
