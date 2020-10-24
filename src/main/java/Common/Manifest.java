package Common;

public class Manifest {
    public static final int BLOCKOUT_TIME_MILLIS = 15000; //15 seconds
    public static final int NUM_RETRIES = 5;
    public static final int UDP_PORT = 2300;
    public static final int TCP_PORT = 2301;

    //Heart Beat sender
    public static final int DEFAULT_HB_SENDER_INITIAL_DELAY = 0; //seconds
    public static final int DEFAULT_HB_SENDER_PERIOD = 2; //seconds

    //Heart Beat receiver
    public static final int DEFAULT_HB_RECEIVER_INITIAL_DELAY = 5; //seconds
    public static final int DEFAULT_HB_RECEIVER_CHECKING_PERIOD = 2; //seconds
    public static final long DEFAULT_HB_RECEIVER_EXPIRE = 3000L; //seconds

    public static final String ACTOR_VEHICLE_CONTROL = "vehicle";
    public static final String ACTOR_ENV_PERCEPTION = "env";

    //Fault Generator
    public static final boolean ZEHER_ENABLED = true;
    public static final int ZEHER_INITIAL_DELAY = 1;
    public static final int ZEHER_DELAY_PERIOD = 1 ;

    public enum COMMAND{

        ADD("add"),
        DELETE("del"),
        CLEAR_ALL("clear_all"),
        EXECUTE("exec");
        final String val;

        COMMAND(String val){
            this.val = val;
        }

        public String getVal() {
            return val;
        }

    }

    public enum TYPE{

        RECEIVER("receiver"),
        SENDER("sender");
        final String val;

        TYPE(String val){
            this.val = val;
        }

        public String getVal() {
            return val;
        }
    }

    public enum Constants{
        string_ModuleCrashed("Environment Perception Module compromised"),
        string_LookingForService("Looking for the service"),
        string_loadingEnvPerceptionService("Loading the Environment Perception Service"),
        string_loadingVCService("Loading the Vehicle Control Service"),
        string_succConnectedToEnvPerService("Successfully Connected to the Environment Perception Service"),
        tring_succConnectedToVCService("Successfully Connected to the Vehicle Control Service"),
        string_succLoadingEnvPerService("Successfully loaded Environment Perception Service"),
        string_succLoadingVCService("Successfully loaded Vehicle Control Service");
        final String val;

        Constants(String val){
            this.val = val;
        }

        public String getVal() {
            return val;
        }

    }
}
