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
	//�����¼�
	public void click(View view){
		//src ������Դ����
		//srcPos ��Դ������Ǹ�λ�ÿ�ʼ����.
		//dst Ŀ������
		//dstPos ��Ŀ��������Ǹ�λ�ӿ�ʼд����
		//length ������Ԫ�صĸ���
	  System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
      mHits[mHits.length-1] = SystemClock.uptimeMillis();
      if (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
         Toast.makeText(this, "��ϲ�㣬3�ε���ˡ�", 0).show();
      }
	}
}
