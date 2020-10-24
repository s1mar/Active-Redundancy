package Base;

import Common.CryoUtils;
import Common.Manifest;
import Model.MStatusCheck;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import static Common.Manifest.UDP_PORT;

public abstract class ServerS {

    protected static Server mServer;
    protected static void Initialization(String name) throws Exception {

            //starting the server that will act as the communication backbone
            mServer = new Server();
            //registering the models
            CryoUtils.registerAllModelsInTheModelPackage(mServer.getKryo());
            //starting the server
            mServer.start();
            //binding the server to a port
            mServer.bind(Manifest.TCP_PORT, UDP_PORT);


    }

}
