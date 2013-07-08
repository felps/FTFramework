package ftec;

import java.rmi.Remote;
import java.rmi.RemoteException;
import defaultTypes.*;

public interface FtecInterface extends Remote{

	public Long startFtecThread(String configFile, Task submittedTask, int uid)
		throws RemoteException;

}
