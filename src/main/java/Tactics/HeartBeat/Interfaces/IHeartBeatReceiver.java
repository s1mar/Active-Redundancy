package Tactics.HeartBeat.Interfaces;

public interface IHeartBeatReceiver extends IHeartBeatReceiverForSender {
    int STATUS_NOT_STARTED = -1;
    int STATUS_FAULT_DETECTED = 1;
    int STATUS_WORKING_SMOOTHLY = 0;

    void start(); //start operation
    void stop();  //cease operation
    void notifyFault(); //notifies to the class for which it is monitoring that a fault has occured in the sender
    int getStatus();
    void reset();
    String getOperationalData();

    public interface IFaultMonitor{
        public void faultDetected();
    }
}
