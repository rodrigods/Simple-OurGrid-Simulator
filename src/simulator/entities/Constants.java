package simulator.entities;

public interface Constants {
	//services
	public static final String LOCAL_BROKER = "REMOTE_BROKER";
	public static final String REMOTE_BROKER = "LOCAL_BROKER";
	public static final String WORKER = "WORKER";
	
	//broker job types
	public static final int LOCAL = 1;
	public static final int REMOTE = 2;
	
	//ports
	public static final String OUT_LOCAL_BROKER = "outRemoteBroker";
	public static final String OUT_REMOTE_BROKER = "outRemoteBroker";
	public static final String IN_WORKER = "inWorker";
}
