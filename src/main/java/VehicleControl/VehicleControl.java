package VehicleControl;

import Base.ClientS;
import Common.Manifest;
import Model.DATA;
import Model.MEnvPerception;
import Model.MVehicleControl;
import Tactics.HeartBeat.HeartbeatSender;
import Tactics.HeartBeat.Interfaces.IHeartBeatReceiverForSender;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;
import java.util.Map;


public class VehicleControl extends ClientS {

    static Map<Integer, IHeartBeatReceiverForSender> receiverMap = new HashMap<>();

    public static void main(String[] args){

        try {
            //Initialize and establish connection
            Client client = Initialization(VehicleControl.class.getSimpleName());

            HeartbeatSender sender = new HeartbeatSender(); //this will relay the status of this module to the supervisor
            sender.start(); //start emitting


            client.addListener(new Listener() {


                @Override
                public void connected(Connection connection) {
                    super.connected(connection);
                    client.sendUDP(new MVehicleControl());
                }

                @Override
                public void received(Connection connection, Object object) {

                    if (object instanceof DATA) {
                        DATA payloadReceived = (DATA) object;
                        if (payloadReceived.typeDestination.equals(Manifest.TYPE.SENDER.getVal())) {

                            if (payloadReceived.typeSource.equals(Manifest.TYPE.RECEIVER.getVal())) {

                                Boolean isRegisterRequest = (Boolean) payloadReceived.datum[0];
                                Integer registerID = (Integer) payloadReceived.datum[1];
                                if (isRegisterRequest) {

                                    IHeartBeatReceiverForSender receiver = new IHeartBeatReceiverForSender() {
                                        @Override
                                        public void pitAPit(String args) {
                                            final int id = registerID;
                                            DATA instruction = new DATA();
                                            instruction.typeSource = Manifest.TYPE.SENDER.getVal();
                                            instruction.typeDestination = Manifest.TYPE.RECEIVER.getVal();
                                            instruction.datum = new Object[]{id};
                                            client.sendUDP(instruction);
                                        }
                                    };

                                    //subscribing the register
                                    sender.subscribeReceiver(receiver);
                                } else {
                                    //Unregistering the register
                                    IHeartBeatReceiverForSender receiverToRemove = receiverMap.get(registerID);
                                    sender.unsubscribeReceiver(receiverToRemove);
                                    receiverMap.remove(registerID);
                                }

                            }

                        }

                    }
                }
            });


            connectClient(VehicleControl.class.getSimpleName(),client);




        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
