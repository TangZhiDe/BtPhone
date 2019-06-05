package com.nforetek.bt.phone.tools;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


/**
 * 
 * 右侧的字母索引View 
 * 
 * @author tzd
 *
 */

public class SideBar extends View {
	
	//触摸事件
	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	
	 // 26个字母  
    public static String[] b = { "A", "B", "C", "D", "E", "F", "G", "H", "I",  
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",  
            "W", "X", "Y", "Z", "#" };  
    //选中
    private int choose = -1;
	
    private Paint paint = new Paint();
    
    private TextView mTextDialog;
    private int mHeight = 0;
	private String[] noContent;

	public void setList(String[] noContent){
		this.noContent = noContent;
	}

	/**
     * 为SideBar显示字母的TextView
     * 
     * @param mTextDialog
     */
    public void setTextView(TextView mTextDialog){
    	this.mTextDialog = mTextDialog;
    }
    

	public SideBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SideBar(Context context) {
		super(context);
	}

	
	public void setChoose(int choose) {
		this.choose = choose;
		invalidate();
	}
	/**
	 * 
	 * 重写的onDraw的方法
	 * 
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int height = getHeight();//获取对应的高度
		if(mHeight == 0){
			mHeight = height;
		}
		int width = getWidth();//获取对应的宽度
		int singleHeight = mHeight/b.length;//获取每一个字母的高度
		for (int i = 0; i < b.length; i++) {
			paint.setColor(Color.parseColor("#656b77"));
			paint.setTypeface(Typeface.DEFAULT_BOLD);
			paint.setAntiAlias(true);
			paint.setTextSize(14);
			//选中的状态
			if (i == choose) {
				paint.setColor(Color.parseColor("#3399ff"));
				paint.setFakeBoldText(true);//设置是否为粗体文字
			}


			//x坐标等于=中间-字符串宽度的一半
			float xPos = width / 2- paint.measureText(b[i])/2;
			float yPos = singleHeight*i + singleHeight;
			canvas.drawText(b[i], xPos, yPos, paint);

			paint.reset();//重置画笔
			Log.e("SideBar", "xPos-----"+xPos+"----yPos----"+yPos);
		}

	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		
		final int action = event.getAction();
		final float y = event.getY();//点击y坐标
		final float x = event.getX();//点击y坐标
		final int oldChoose = choose;
		
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		final int c = (int)(y / mHeight * b.length);//点击y坐标所占高度的比例*b数组的长度就等于点击b中的个数
		
		switch (action) {
		case MotionEvent.ACTION_UP:
			setBackgroundDrawable(new ColorDrawable(0x00000000));//设置背景颜色
			invalidate();
			if (mTextDialog != null) {
				mTextDialog.setVisibility(View.INVISIBLE);
			}
			break;

		default:
//			setBackgroundResource(R.drawable.sidebar_background);
			if (oldChoose != c) {
				if (c>=0 && c<b.length) {
					if(noContent != null){
						for (int i=0;i<noContent.length;i++){
							if(noContent[i].equals(b[c])){
								if (listener != null) {
									listener.onTouchingLetterChanged(b[c]);
								}
								if (mTextDialog != null) {
									int singleHeight = mHeight/b.length;//获取每一个字母的高度
									float yPos = singleHeight*c + singleHeight;
									mTextDialog.setY(yPos);
									mTextDialog.setText(b[c]);
									mTextDialog.setVisibility(View.VISIBLE);
								}

								choose = c;
								invalidate();
							}
						}
					}
				}
			}
			break;
		}
		return true;
	}

	/**
	 * 向外松开的方法
	 * 
	 * @param onTouchingLetterChangedListener
	 */
	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}
	
	/**
	 * 
	 * 接口
	 * 
	 * @author 
	 *
	 */
	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s);
	}

}