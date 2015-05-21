package com.itheima.doubleclick;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	long[] mHits = new long[3];
	//三击事件
	public void click(View view){
		//src 拷贝的源数组
		//srcPos 从源数组的那个位置开始拷贝.
		//dst 目标数组
		//dstPos 从目标数组的那个位子开始写数据
		//length 拷贝的元素的个数
	  System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
      mHits[mHits.length-1] = SystemClock.uptimeMillis();
      if (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
         Toast.makeText(this, "恭喜你，3次点击了。", 0).show();
      }
	}
}
