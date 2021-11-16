package com.example.monitoringplatform.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.application.recyclerviewproject.roomOverview_item;
import com.example.monitoringplatform.R;

import java.util.ArrayList;


public class roomOverviewAdapter extends RecyclerView.Adapter<roomOverviewAdapter.ExampleViewHolder> {
    private ArrayList<roomOverview_item> mList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener=listener;

    }

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        public ImageView mIconRoom;
        public ImageView mIconTemperature;
        public ImageView mIconHumidity;
        public ImageView mIconWind;
        public TextView mRoom;
        public TextView mTemp;
        public TextView mHum;
        public TextView mWind;
        public TextView mPmv;

        public ExampleViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mIconRoom = itemView.findViewById(R.id.room_icon);
            mIconTemperature = itemView.findViewById(R.id.temperature);
            mIconHumidity = itemView.findViewById(R.id.humidity);
            mIconWind = itemView.findViewById(R.id.wind);
            mRoom = itemView.findViewById(R.id.room_name);
            mTemp = itemView.findViewById(R.id.temperature_value);
            mHum = itemView.findViewById(R.id.humidity_value);
            mWind = itemView.findViewById(R.id.wind_value);
            mPmv = itemView.findViewById(R.id.PMV);


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

    public roomOverviewAdapter(ArrayList<roomOverview_item> exampleList) {
        mList = exampleList;
    }

    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_overview_item, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v,mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(ExampleViewHolder holder, int position) {
        roomOverview_item currentItem = mList.get(position);

        holder.mIconRoom.setImageResource(currentItem.getIconRoom());
        holder.mIconTemperature.setImageResource(currentItem.getIconTemperature());
        holder.mIconHumidity.setImageResource(currentItem.getIconHumidity());
        holder.mIconWind.setImageResource(currentItem.getIconWind());
        holder.mRoom.setText(currentItem.getRoom());
        holder.mTemp.setText(currentItem.getTemp());
        holder.mHum.setText(currentItem.getHum());
        holder.mWind.setText(currentItem.getWind());
        holder.mPmv.setText(currentItem.getPmv());
        
        if (currentItem.getPmv().equals("WARM")){
            holder.mPmv.setTextColor(Color.parseColor("#FFFF9800"));

        }else if (currentItem.getPmv().equals("GOOD")){
            holder.mPmv.setTextColor(Color.parseColor("#5CC615"));
        }else if(currentItem.getPmv().equals("HOT")){
            holder.mPmv.setTextColor(Color.parseColor("#FFE91E63"));

        }
        else if(currentItem.getPmv().equals("COOL")){
            holder.mPmv.setTextColor(Color.parseColor("#FF01BCAA"));

        }
        else if(currentItem.getPmv().equals("COLD")){
            holder.mPmv.setTextColor(Color.parseColor("#FF0146BC"));

        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}