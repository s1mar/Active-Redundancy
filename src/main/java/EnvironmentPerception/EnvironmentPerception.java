package EnvironmentPerception;

import Base.ClientS;
import Common.Manifest;
import Common.Zeher;
import Model.DATA;
import Model.MEnvPerception;
import Tactics.HeartBeat.HeartbeatSender;
import Tactics.HeartBeat.Interfaces.IHeartBeatReceiverForSender;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EnvironmentPerception extends ClientS {


    static Map<Integer, IHeartBeatReceiverForSender> receiverMap = new HashMap<>();
    private static String currentOperationalConfig;
    private static final String CRYPT_ALGORITHM = "AES";


    public static String generateDummyConfiguration(){
        // Generate random id, for example 283952-V8M32
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder((100000 + rnd.nextInt(900000)) + "-");
        for (int i = 0; i < 5; i++)
            sb.append(chars[rnd.nextInt(chars.length)]);

        return sb.toString();

    }

    public static void main(String[] args){

        try {
            Cipher cipherEncrypt = Cipher.getInstance(CRYPT_ALGORITHM);
            Cipher cipherDecrypt = Cipher.getInstance(CRYPT_ALGORITHM);
            Key key = KeyGenerator.getInstance(CRYPT_ALGORITHM).generateKey();
            cipherEncrypt.init(Cipher.ENCRYPT_MODE,key);
            cipherDecrypt.init(Cipher.DECRYPT_MODE,key);

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

            //dummy code that updates the configuration string with a new one. In this demo it basically represents the current state of the module
            //This will be used by my Active Redundancy module to relaunch this module with the last saved configuration state
            executorService.scheduleAtFixedRate(()->{
                 currentOperationalConfig = generateDummyConfiguration();

            },1000L,1000L, TimeUnit.MILLISECONDS);


            //Initialize and establish connection
           Client client = Initialization(EnvironmentPerception.class.getSimpleName());

            HeartbeatSender sender = new HeartbeatSender(); //this will relay the status of this module to the supervisor
            sender.start(); //start emitting


            client.addListener(new Listener() {


                @Override
                public void connected(Connection connection) {
                    super.connected(connection);
                    client.sendUDP(new MEnvPerception());
                }

                @Override
                public void received(Connection connection, Object object) {

                    if (object instanceof DATA) {
                        DATA payloadReceived = (DATA) object;
                        if (payloadReceived.typeDestination.equals(Manifest.TYPE.SENDER.getVal())) {

                            if (payloadReceived.typeSource.equals(Manifest.TYPE.RECEIVER.getVal())) {

                                Boolean isRegisterRequest = (Boolean) payloadReceived.datum[0];
                                Integer registerID = (Integer) payloadReceived.datum[1];


                                if(payloadReceived.datum.length>2 && payloadReceived.datum[2] instanceof  String) {
                                    String configData = (String) payloadReceived.datum[2];
                                    try {
                                        cipherDecrypt.doFinal(configData.getBytes());
                                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (isRegisterRequest) {

                                    IHeartBeatReceiverForSender receiver = new IHeartBeatReceiverForSender() {
                                        @Override
                                        public void pitAPit(String operationalData) {
                                            final int id = registerID;
                                            DATA instruction = new DATA();
                                            instruction.typeSource = Manifest.TYPE.SENDER.getVal();
                                            instruction.typeDestination = Manifest.TYPE.RECEIVER.getVal();


                                            try {

                                                String encConfig = Arrays.toString(cipherEncrypt.doFinal(currentOperationalConfig.getBytes()));
                                                instruction.datum = new Object[]{id,encConfig};
                                            } catch (GeneralSecurityException e) {
                                                e.printStackTrace();
                                            }

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

            connectClient(EnvironmentPerception.class.getSimpleName(),client);

        }catch (Exception ex){
            ex.printStackTrace();
        }


        //To disable random fault generator, set @Manifest.ZEHER_ENABLED = false;
        Zeher.ApplyZeher((shouldDie)->{
            if(shouldDie && Manifest.ZEHER_ENABLED){

                System.out.println(Manifest.Constants.string_ModuleCrashed.getVal());
                Runtime.getRuntime().halt(0);
                Object[] o = null;

                while (true) {
                    o = new Object[] {o};
                }
            }
        });

    }



}
