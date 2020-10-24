package Tactics.HeartBeat;

import Common.Manifest;
import Tactics.HeartBeat.Interfaces.IHeartBeatReceiverForSender;
import Tactics.HeartBeat.Interfaces.IHeartBeatSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatSender implements IHeartBeatSender {

    private int heartBeatInitialDelay= Manifest.DEFAULT_HB_SENDER_INITIAL_DELAY; //default val: 0 seconds
    private int heartBeatPeriod = Manifest.DEFAULT_HB_SENDER_PERIOD; // default val: 2 seconds

    private boolean enabled = false;
    private final List<IHeartBeatReceiverForSender> receivers = new ArrayList<>(0);
    private ScheduledExecutorService scheduledExecutorService;


    public HeartbeatSender(int heartBeatInitialDelay, int heartBeatPeriod) {
        this.heartBeatInitialDelay = heartBeatInitialDelay;
        this.heartBeatPeriod = heartBeatPeriod;
    }

    public HeartbeatSender() {}

    @Override
    public void start() {
        enabled = true;
        sendBeat();
    }

    @Override
    public void stop() {
        enabled = false;
        if(scheduledExecutorService!=null)
            scheduledExecutorService.shutdownNow();
    }

    @Override
    public void subscribeReceiver(IHeartBeatReceiverForSender receiver) {
        receivers.add(receiver);
    }

    @Override
    public void unsubscribeReceiver(IHeartBeatReceiverForSender receiver) {
        receivers.remove(receiver);
    }

    @Override
    public void sendBeat() {

     if(scheduledExecutorService == null){scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();}

     scheduledExecutorService.scheduleAtFixedRate(()->{
         System.out.println("pit-pit");
         for(int i = 0; i<receivers.size();++i){
             IHeartBeatReceiverForSender receiver = receivers.get(0);
             if(receiver!=null){
                 receiver.pitAPit(null);
             }

         }
     },heartBeatInitialDelay,heartBeatPeriod,TimeUnit.SECONDS);



    }

    @Override
    public boolean probeStatus() {
        return enabled;
    }
}
