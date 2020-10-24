package Tactics.ActiveRedundancy;

import Tactics.HeartBeat.Interfaces.IHeartBeatReceiver;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @s1mar
 */
public class ActiveRedundancy {

    private int timesToRevive = 1; //at-least 1 revival
    private IHeartBeatReceiver targetToKeepAlive;
    private long pollingPeriodInMillis = 500L; // half a second
    private IAction<Void> actionToRevive;
    private IAction<Boolean> actionForHealthCheck;
    private IAction<String> postHealthCheck; //Optional
    private boolean reincarnationAttempted = false;
    private String TAG = "NotSet";
    private ActiveRedundancy(){}
    private boolean reviveWithLastCapturedConfig;

    public void run(){
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(()->{

            try{
                if(!reincarnationAttempted){
                    if(timesToRevive>0 && targetToKeepAlive.getStatus()>0){
                        //Reincarnate
                        System.out.println(TAG+" attached AR module is attempting revival");
                        actionToRevive.executeAction(null);
                        reincarnationAttempted = true;
                        System.out.println(TAG+ "AR module retries left: "+timesToRevive--);
                    }else if(timesToRevive == 0){
                        scheduledExecutorService.shutdownNow();
                        System.out.println("Ran out of times of revival, AR shutdown for module name: "+TAG);
                    }

                }else {
                    //check health
                    if(actionForHealthCheck.executeAction(null)){
                        //Resume revival check activity
                        String revivalArgs = reviveWithLastCapturedConfig?targetToKeepAlive.getOperationalData():null;
                        postHealthCheck.executeAction(revivalArgs);
                        targetToKeepAlive.reset();
                        reincarnationAttempted = false;

                    }
                }

            }catch (Exception ex){
                ex.printStackTrace();
            }

        },pollingPeriodInMillis,pollingPeriodInMillis, TimeUnit.MILLISECONDS);

    }

    public static class Builder{
        ActiveRedundancy obj;
        public Builder(IHeartBeatReceiver receiverOfTarget){
            obj = new ActiveRedundancy();
            obj.targetToKeepAlive = receiverOfTarget;
        }
        public Builder setTimesToRevive(int timesToRevive){
            obj.timesToRevive = timesToRevive;
            return this;
        }
        public Builder setRevivalAction(IAction<Void> action){
                obj.actionToRevive = action;
                return this;
        }
        public Builder setPollingPeriod(long pollingPeriodInMillis){
            obj.pollingPeriodInMillis = pollingPeriodInMillis;
            return this;
        }

        public Builder setHealthCheckAction(IAction<Boolean> action){
            obj.actionForHealthCheck = action;
            return this;
        }
        public Builder setTAG(String Tag){
            obj.TAG = Tag;
            return this;
        }
        public Builder setActionPostHealthCheck(IAction<String> action){
            obj.postHealthCheck = action;
            return this;
        }
        public Builder reviveWithLastCapturedOperationalData(boolean flag){
            obj.reviveWithLastCapturedConfig = flag;
            return this;
        }
        public ActiveRedundancy build(){
            if(obj.actionToRevive == null || obj.targetToKeepAlive == null || obj.actionForHealthCheck == null){
                return null;
            }
            return obj;
        }

    }


}
