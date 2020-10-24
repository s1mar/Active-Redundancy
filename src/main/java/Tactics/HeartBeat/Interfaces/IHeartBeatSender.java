package Tactics.HeartBeat.Interfaces;

import Tactics.HeartBeat.Interfaces.IHeartBeatReceiver;

public interface IHeartBeatSender {
    void start(); //start operation
    void stop();  //stop operation
    boolean probeStatus(); //to see if it's enabled or not
    void subscribeReceiver(IHeartBeatReceiverForSender receiver); // subscribe receivers to notify
    void unsubscribeReceiver(IHeartBeatReceiverForSender receiver); //unsubscribe receivers to notify
    void sendBeat(); //mimics the heart-beat sending routine, notifying observers that it's module is alive
}
