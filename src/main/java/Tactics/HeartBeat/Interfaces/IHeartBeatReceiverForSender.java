package Tactics.HeartBeat.Interfaces;

public interface IHeartBeatReceiverForSender {

    void pitAPit(String operationalData); //used by the sender to inform the receiver that it is alive
}
