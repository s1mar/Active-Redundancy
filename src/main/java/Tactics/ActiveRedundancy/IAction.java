package Tactics.ActiveRedundancy;

import java.io.IOException;

public interface IAction<T> {

    T executeAction(T arg) throws IOException, InterruptedException;

}
