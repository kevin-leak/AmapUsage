package com.example.amapusage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.DataViewHolder> {
    private Context mContext;
    private ArrayList<String> mList;

    public LocationAdapter() {
    }

    public LocationAdapter(Context mContext, ArrayList<String> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    //用于创建ViewHolder
    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item, null);
        //使用代码设置宽高（xml布局设置无效时）
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        DataViewHolder holder = new DataViewHolder(view);
        return holder;
    }

    //绑定数据
    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        holder.tv_data.setText(mList.get(position));
    }

    //数据总数
    @Override
    public int getItemCount() {
        return mList.size();
    }

    //创建ViewHolder
    public static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView tv_data;

        public DataViewHolder(View itemView) {
            super(itemView);
            tv_data = (TextView) itemView.findViewById(R.id.tv_recycle);
            tv_data.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(App.Companion.getAppContext(), "-----", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}