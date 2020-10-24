package Supervisor;

import Base.ServerS;
import Common.Manifest;
import EnvironmentPerception.EnvironmentPerception;
import Helper.JavaProcess;
import Model.DATA;
import Model.MEnvPerception;
import Model.MVehicleControl;
import Tactics.ActiveRedundancy.ActiveRedundancy;
import Tactics.HeartBeat.HeartbeatReceiver;
import Tactics.HeartBeat.Interfaces.IHeartBeatReceiver;
import Tactics.HeartBeat.Interfaces.IHeartBeatReceiverForSender;
import VehicleControl.VehicleControl;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;


import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.lang.System.out;
//It's a singleton because I only want one Supervisor instance orchestrating everything
public class Supervisor extends ServerS {

    private static boolean vehicleControlConnected = false;
    private static boolean envPerceptionConnected = false;

    private static final Map<String,Integer> mMapModuleConnectionID = new HashMap<String,Integer>();
    private static final Map<Integer,String> mMapConnectionModule = new HashMap<Integer, String>();

    private static final Map<Integer,Object> mMapCallbackModule = new HashMap<>(); //which callback is associated with module

    private static volatile Supervisor mInstance;

    protected Supervisor() {

        //protecting against Reflection
        if(mInstance!=null){
            throw new RuntimeException("Use getInstance() only");
        }
    }

    public static Supervisor getInstance() {

        if(mInstance == null){
            synchronized (Supervisor.class){
                mInstance = new Supervisor();
            }
        }

        return mInstance;
    }

    public static void main(String[] args){
        try {


            JavaProcess.exec(EnvironmentPerception.class,null);
            JavaProcess.exec(VehicleControl.class,null);

            //Initialize and start the server
            Initialization(Supervisor.class.getSimpleName());



            //Listening for requests
            mServer.addListener(new Listener() {
                @Override
                public void received(Connection connection, Object object) {
                    super.received(connection, object);

                    if (object instanceof MEnvPerception) {
                        mMapModuleConnectionID.put(Manifest.ACTOR_ENV_PERCEPTION, connection.getID());
                        mMapConnectionModule.put(connection.getID(), Manifest.ACTOR_ENV_PERCEPTION);
                        moduleConnected(Manifest.ACTOR_ENV_PERCEPTION);
                        envPerceptionConnected = true;
                        out.println("Env Perception ON");
                    } else if (object instanceof MVehicleControl) {
                        mMapModuleConnectionID.put(Manifest.ACTOR_VEHICLE_CONTROL, connection.getID());
                        mMapConnectionModule.put(connection.getID(), Manifest.ACTOR_VEHICLE_CONTROL);
                        moduleConnected(Manifest.ACTOR_VEHICLE_CONTROL);
                        vehicleControlConnected = true;
                        out.println("Vehicle Control ON");
                    }else if (object instanceof DATA){

                        DATA payloadReceived = (DATA)object;
                        if(payloadReceived.typeSource.equals(Manifest.TYPE.SENDER.getVal())){

                            if(payloadReceived.typeDestination.equals(Manifest.TYPE.RECEIVER.getVal())){

                                int receiverID = (Integer) payloadReceived.datum[0];
                                IHeartBeatReceiverForSender receiver = (IHeartBeatReceiver)(mMapCallbackModule.get(receiverID));
                                String operationalData_Config = payloadReceived.datum.length>1 && payloadReceived.datum[1] instanceof String?(String)payloadReceived.datum[1]:"nil";
                                receiver.pitAPit(operationalData_Config);
                            }

                        }
                    }
                }

                @Override
                public void connected(Connection connection) {
                    super.connected(connection);
                }

                @Override
                public void disconnected(Connection connection) {
                    super.disconnected(connection);
                    try {
                        String endpointDisconnected = mMapConnectionModule.get(connection.getID());
                        mMapModuleConnectionID.remove(endpointDisconnected);
                        //moduleDisconnected(endpointDisconnected);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            promptEnterKey();


            if (initialStatusCheck()) {

                out.println("Operations launched");

                HeartbeatReceiver heartbeatReceiverForEnvPerception = new HeartbeatReceiver(() -> {
                    out.println("Environment Perception Compromised!");
                    moduleDisconnected(Manifest.ACTOR_ENV_PERCEPTION);
                },Manifest.ACTOR_ENV_PERCEPTION);

                //Registering our ENV Heartbeat Receiver with the respective heartbeat sender

                //mapping it with it's hashcode, so that when we get a request through the server, we know what to call
                int hashCodeOfTheReceiverInstance = heartbeatReceiverForEnvPerception.hashCode();
                mMapCallbackModule.put(hashCodeOfTheReceiverInstance,heartbeatReceiverForEnvPerception);

                //Registering our receiver with the Env Perception sender
                emitPulseToConnectEnvPerceptionWithReceiver(hashCodeOfTheReceiverInstance,null);

                HeartbeatReceiver heartbeatReceiverForVehicleControl = new HeartbeatReceiver(()->{
                    out.println("Vehicle Control Compromised");
                    moduleDisconnected(Manifest.ACTOR_VEHICLE_CONTROL);
                },Manifest.ACTOR_VEHICLE_CONTROL);

                //Registering our VehicleControl Heartbeat Receiver with the respective heartbeat sender

                //mapping it with it's hashcode, so that when we get a request through the server, we know what to call
                int hashCodeOfTheReceiverInstance_Vehicle = heartbeatReceiverForVehicleControl.hashCode();
                mMapCallbackModule.put(hashCodeOfTheReceiverInstance_Vehicle,heartbeatReceiverForVehicleControl);

                //Registering our receiver with the sender
                emitPulseToConnectVehiPerceptionWithReceiver(hashCodeOfTheReceiverInstance_Vehicle);

                //start the receivers
                heartbeatReceiverForEnvPerception.start();
                heartbeatReceiverForVehicleControl.start();


                //Active Redundancy setup

                //AR for Environment Perception Module
                ActiveRedundancy activeRedundancy_EnvPerception =  new ActiveRedundancy
                        .Builder(heartbeatReceiverForEnvPerception)
                        .setTimesToRevive(5)
                        .setRevivalAction((Void)->{
                            JavaProcess.exec(EnvironmentPerception.class,null);
                            return null;
                        }).setHealthCheckAction((Void)-> envPerceptionConnected)
                        .setActionPostHealthCheck((configData)->{
                            emitPulseToConnectEnvPerceptionWithReceiver(hashCodeOfTheReceiverInstance,configData);
                            return null;
                        })
                        .reviveWithLastCapturedOperationalData(true)
                        .setTAG(EnvironmentPerception.class.getSimpleName())
                        .build();

                //Activate for enhanced availability
                activeRedundancy_EnvPerception.run();

            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }


    }

    static void emitPulseToConnectEnvPerceptionWithReceiver(int hashCodeReceiver, String configData){
        //Registering our receiver with the sender
        DATA instructionEmitPulse = new DATA();
        instructionEmitPulse.command = Manifest.COMMAND.EXECUTE.getVal();
        instructionEmitPulse.typeSource = Manifest.TYPE.RECEIVER.getVal();
        instructionEmitPulse.typeDestination = Manifest.TYPE.SENDER.getVal();
        instructionEmitPulse.datum = new Object[]{true,hashCodeReceiver,configData}; //this will basically instruct the sender as to associate which observer with what in this module
        mServer.sendToUDP(mMapModuleConnectionID.get(Manifest.ACTOR_ENV_PERCEPTION),instructionEmitPulse);
        if(configData!=null && !configData.trim().isEmpty()){
            out.println("Initializing Env Perception with last saved config: "+configData);
        }
    }

    static void emitPulseToConnectVehiPerceptionWithReceiver(int hashCodeReceiver){
        DATA instructionEmitPulse_V = new DATA();
        instructionEmitPulse_V.command = Manifest.COMMAND.EXECUTE.getVal();
        instructionEmitPulse_V.typeSource = Manifest.TYPE.RECEIVER.getVal();
        instructionEmitPulse_V.typeDestination = Manifest.TYPE.SENDER.getVal();
        instructionEmitPulse_V.datum = new Object[]{true,hashCodeReceiver}; //this will basically instruct the sender as to associate which observer with what in this module

        mServer.sendToUDP(mMapModuleConnectionID.get(Manifest.ACTOR_VEHICLE_CONTROL),instructionEmitPulse_V);

    }

    static void promptEnterKey(){
        System.out.println("Wait for the other 2 modules to come online and then Press \"ENTER\" to begin operation...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    static boolean initialStatusCheck() throws InterruptedException{

        //Connect to the environment perception module
        //out.println("Checking to see if the Environment Perception module is connected");
        boolean flag = true;
        if(!vehicleControlConnected){
            out.println("Vehicle Control module not connected");
            flag = false;
        }

        if(!envPerceptionConnected){
            String strToPrint = vehicleControlConnected?"Complete System Failure!":"EnvPerception is not connected, shifting Vehicle Control to manual mode";
            out.println(strToPrint);
            flag = false;
        }
        return flag;
    }


    static void moduleConnected(String moduleName){
        if(moduleName!=null && !moduleName.trim().isEmpty()) {
            if (moduleName.equals(Manifest.ACTOR_VEHICLE_CONTROL)) {
                out.println("Vehicle Control module connected");
                vehicleControlConnected = true;

            } else if (moduleName.equals(Manifest.ACTOR_ENV_PERCEPTION)) {
                out.println("EnvPerception connected");
                envPerceptionConnected = true;
            }
        }
    }

    static void moduleDisconnected(String moduleName){
        if(moduleName!=null && !moduleName.trim().isEmpty()) {
            if (moduleName.equals(Manifest.ACTOR_VEHICLE_CONTROL)) {
                out.println("Vehicle Control module not connected");
                vehicleControlConnected = false;
            } else if (moduleName.equals(Manifest.ACTOR_ENV_PERCEPTION)) {
                out.println("EnvPerception is not connected, shifting Vehicle Control to manual mode");
                envPerceptionConnected = false;
            }
            out.print("\nEnforcing manual driving\n");
        }
    }

}
