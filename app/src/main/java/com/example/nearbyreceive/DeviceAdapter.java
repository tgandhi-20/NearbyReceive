package com.example.nearbyreceive;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;



public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>{
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public Button workerButton;




        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.Device_Name);
            workerButton = (Button) itemView.findViewById(R.id.worker_button);
            workerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.workerButtonOnClick(view,getAdapterPosition());
                }
            });
        }


    }

    public MyAdapterListener onClickListener;
    private List<NDevice> nDeviceList;

    public DeviceAdapter(List<NDevice> list,MyAdapterListener listener){
        nDeviceList = list;
        onClickListener = listener;
    }



    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.device_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DeviceAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        NDevice device = nDeviceList.get(position);

        // Set item views based on your views and data model
        TextView textView = holder.nameTextView;
        textView.setText(device.getName());
        Button button = holder.workerButton;
        button.setText(device.isWorker() ? "Worker" : "Camera");

    }

    @Override
    public int getItemCount() {
        return nDeviceList.size();
    }


}


