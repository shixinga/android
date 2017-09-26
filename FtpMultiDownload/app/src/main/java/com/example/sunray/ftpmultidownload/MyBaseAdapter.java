package com.example.sunray.ftpmultidownload;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sunray on 2017-9-25.
 */

public class MyBaseAdapter extends BaseAdapter {
    List<String> mList;
    LayoutInflater mLayoutInflater;
    IListener mListener;

    public MyBaseAdapter(List<String> mList, Context context) {
        this.mList = mList;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MyViewHolder myViewHolder;
        final int position = i;
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.item_list,null);
            myViewHolder = new MyViewHolder();
            myViewHolder.tv = view.findViewById(R.id.tv_item);
            myViewHolder.bt_start = view.findViewById(R.id.start);
            myViewHolder.bt_end = view.findViewById(R.id.stop);
            view.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder) view.getTag();
        }
        myViewHolder.tv.setText(mList.get(i));
        myViewHolder.bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.startListener(position);
            }
        });
        myViewHolder.bt_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.stopListener(position);
            }
        });
        return view;
    }

    class MyViewHolder {
        TextView tv;
        Button bt_start;
        Button bt_end;
    }

    public void setmListener(IListener mListener) {
        this.mListener = mListener;
    }

    public interface IListener {
        void startListener(int position);
        void stopListener(int position);

    }
}
