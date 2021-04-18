package com.example.monitoringplatform;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.application.recyclerviewproject.parameter_item;
import com.example.application.recyclerviewproject.roomOverview_item;

import java.util.ArrayList;


public class parameterAdapter extends RecyclerView.Adapter<parameterAdapter.ExampleViewHolder> {
    private ArrayList<parameter_item> mList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener=listener;

    }

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        public ImageView mIcon;
        public TextView mParamter;
        public TextView mValue;
        public TextView mTime;


        public ExampleViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.parameter_icon);
            mParamter = itemView.findViewById(R.id.parameter_name);
            mValue = itemView.findViewById(R.id.parameter_value);
            mTime = itemView.findViewById(R.id.parameter_time);


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

    public parameterAdapter(ArrayList<parameter_item> exampleList) {
        mList = exampleList;
    }

    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.parameter_item, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v,mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ExampleViewHolder holder, int position) {
        parameter_item currentItem = mList.get(position);

        holder.mIcon.setImageResource(currentItem.getIcon());
        holder.mParamter.setText(currentItem.getParameter());
        holder.mValue.setText(currentItem.getValue());
        holder.mTime.setText(currentItem.getTime());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}