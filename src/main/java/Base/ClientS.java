package Base;

import Common.CryoUtils;
import Common.Manifest;
import com.esotericsoftware.kryonet.Client;


import java.net.InetAddress;

/*@author s1mar
 */
public abstract class ClientS {

    protected static InetAddress address;
    protected static Client Initialization(String clientName) throws Exception{

            Client client = new Client();
            //registering the models
            CryoUtils.registerAllModelsInTheModelPackage( client.getKryo());

             client.start();
            //Let's discover the server running
             address =  (address==null)?client.discoverHost(Manifest.UDP_PORT, Manifest.BLOCKOUT_TIME_MILLIS):address;

            if (address == null) {
                int retryAttempt = Manifest.NUM_RETRIES;

                while (retryAttempt > 0 && address == null) {
                    System.out.printf("Retry Number %d/%d \n",retryAttempt,Manifest.NUM_RETRIES);
                    address =  client.discoverHost(Manifest.UDP_PORT, Manifest.BLOCKOUT_TIME_MILLIS);
                    retryAttempt--;
                }

                if (address == null) {
                    System.out.printf("Shutting down %s system for unable to discover server",clientName);
                    throw new Exception("Unable to find server");
                }

            }

            //Server found
            System.out.println("Server found at: " + address);

            return client;
        }

    protected static void  connectClient(String clientName, Client clientInstance ) throws Exception{
            //Let's connect to it
            clientInstance.connect(Manifest.BLOCKOUT_TIME_MILLIS, address, Manifest.TCP_PORT, Manifest.UDP_PORT);
            if( clientInstance.isConnected()){

                System.out.println("Successfully connected to the server");
            }
            else {
                System.out.printf("Shutting down %s system for unable to connect to the server",clientName);
                throw  new Exception("Unable to connect to server at TCP : "+ Manifest.TCP_PORT);
            }
    }

    }


