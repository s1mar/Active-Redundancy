package Common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class Zeher {

    Victim victim;
    int initialDelay = Manifest.ZEHER_INITIAL_DELAY;
    int period =Manifest.ZEHER_DELAY_PERIOD;

    private Zeher(Victim victim){
        this.victim = victim;
        mujheZeherDedo();
    }

    public static void ApplyZeher(Victim victim){
       new Zeher(victim);
    }

    public void mujheZeherDedo(){

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::marnaKabHai,initialDelay,period, TimeUnit.MINUTES);

    }

    private void marnaKabHai(){

        int int_random = ThreadLocalRandom.current().nextInt();
        double double_rand = ThreadLocalRandom.current().nextDouble();
        boolean boolean_rand = ThreadLocalRandom.current().nextBoolean();

        boolean intDivisibleBy2 = int_random%2==0;
        boolean doubleMantissaGreaterThan50 = getMantissa(double_rand)>50;
        boolean shouldCrash = boolean_rand;

        if(shouldCrash){
            victim.shouldDie(true);
        }
    }


    public static double getMantissa(double x) {
        int exponent = Math.getExponent(x);
        return x / Math.pow(2, exponent);
    }

    public interface Victim{
        public void shouldDie(boolean flag);
    }
}
