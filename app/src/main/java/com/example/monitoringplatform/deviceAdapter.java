package com.example.monitoringplatform;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.application.recyclerviewproject.device_item;
import com.example.monitoringplatform.R;

import java.util.ArrayList;

public class deviceAdapter extends RecyclerView.Adapter<deviceAdapter.ExampleViewHolder> {
    private ArrayList<device_item> mList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener=listener;

    }

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mName;
        public TextView mParam;
        public TextView mLast;

        public ExampleViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imagedevice);
            mName = itemView.findViewById(R.id.devicename);
            mParam = itemView.findViewById(R.id.parameter);
            mLast = itemView.findViewById(R.id.last);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener !=null){
                        int position=getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }

                }
            });
        }
    }

    public deviceAdapter(ArrayList<device_item> exampleList) {
        mList = exampleList;
    }

    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v,mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ExampleViewHolder holder, int position) {
        device_item currentItem = mList.get(position);

        holder.mImageView.setImageResource(currentItem.getImageResource());
        holder.mName.setText(currentItem.getText1());
        holder.mParam.setText(currentItem.getText2());
        holder.mLast.setText(currentItem.getText3());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}