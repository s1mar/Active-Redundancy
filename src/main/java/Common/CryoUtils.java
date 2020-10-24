package Common;

import Model.DATA;
import Model.MEnvPerception;
import Model.MStatusCheck;
import Model.MVehicleControl;
import com.esotericsoftware.kryo.Kryo;

import java.util.Arrays;
import java.util.List;

public class CryoUtils {

    public static void registerModels(Kryo endPoint, List<Class> modelsToRegister){
        for(Class model : modelsToRegister){
            endPoint.register(model);
        }
        endPoint.register(Object.class);
        endPoint.register(Object[].class);
    }

    public static void registerAllModelsInTheModelPackage(Kryo kryo){

        List<Class> modelsToRegister =  Arrays.asList(new Class[]{MStatusCheck.class, MEnvPerception.class, MVehicleControl.class, DATA.class});
        registerModels(kryo,modelsToRegister);

    }

}
