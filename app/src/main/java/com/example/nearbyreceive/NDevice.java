package com.example.nearbyreceive;

import java.util.ArrayList;
import java.util.HashMap;

public class NDevice {
    String name;
    String endPointId;
    Boolean isWorker;
    long sentFrameTime;
    int frameProcessingPower;
    Boolean isBusy;
    int battery;
    float cpu;
    int cores;

    public NDevice(String name,String endPointId,boolean isWorker,long sentFrameTime,int frameProcessingPower,boolean isBusy,int battery,float cpu,int cores){
        this.name = name;
        this.endPointId = endPointId;
        this.isWorker = isWorker;
        this.sentFrameTime = sentFrameTime;
        this.frameProcessingPower = frameProcessingPower;
        this.isBusy = isBusy;
        this.battery = battery;
        this.cpu = cpu;
        this.cores = cores;
    }

    public String getName(){
        return name;
    }

    public String getEndPointId(){
        return endPointId;
    }

    public Boolean isWorker() {
        return isWorker;
    }

    public void setWorker(){
        isWorker = true;
    }


}
