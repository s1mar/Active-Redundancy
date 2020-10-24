package Tactics.HeartBeat;

import Common.Manifest;
import Tactics.HeartBeat.Interfaces.IHeartBeatReceiver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatReceiver implements IHeartBeatReceiver {

    private int initialDelay= Manifest.DEFAULT_HB_RECEIVER_INITIAL_DELAY; //default val: 0 seconds
    private int heartBeatCheckingPeriod = Manifest.DEFAULT_HB_RECEIVER_CHECKING_PERIOD; // default val: 2 seconds
    private IFaultMonitor monitor;
    private long expireTime = Manifest.DEFAULT_HB_RECEIVER_EXPIRE; //default value of 3 second, if the time between getting a notification from the sender exceeds expire time,
                                // then it's a fault and the monitor would be notified

    private ScheduledExecutorService executorService;
    private long updateTime; //last time the heart beat was updated

    private String TAG; //for testing purpose; represents which sender is it receiving for
    private int faultDetected = STATUS_NOT_STARTED;
    private String operationalData_Config;

    public HeartbeatReceiver(IFaultMonitor monitor, int initialDelay, int heartBeatCheckingPeriod) {
        this.monitor = monitor;
        this.initialDelay = initialDelay;
        this.heartBeatCheckingPeriod = heartBeatCheckingPeriod;

    }


    public HeartbeatReceiver(IFaultMonitor monitor, String TAG) {
        this.monitor = monitor;
        this.TAG = TAG;
    }

    @Override
    public void start() {
         executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(()->{

            if (System.currentTimeMillis() - updateTime >= expireTime) {
                //notify the monitor that a fault has occurred and shutdown the receiver
                stop();
                notifyFault();

            }

        },initialDelay,heartBeatCheckingPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
            executorService.shutdownNow();
    }

    @Override
    public void notifyFault() {
        faultDetected = STATUS_FAULT_DETECTED;
        monitor.faultDetected();
    }

    @Override
    public void pitAPit(String operationalData) {
            this.operationalData_Config = operationalData;
            updateTime = System.currentTimeMillis();
            //Testing
            System.out.println("dub-dub : "+TAG+" \nConfig Data Received: "+operationalData);
    }

    @Override
    public void reset() {
        stop();
        updateTime = System.currentTimeMillis();
        faultDetected = STATUS_WORKING_SMOOTHLY;
        start();
    }

    @Override
    public int getStatus() {
        return faultDetected;
    }

    @Override
    public String getOperationalData() {
        return operationalData_Config;
    }
}
