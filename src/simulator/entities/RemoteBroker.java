package simulator.entities;
import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_port;
import eduni.simjava.distributions.Sim_negexp_obj;


/**
 * Represents the Broker, the source entity
 * It generates jobs to be executed in the Workers
 *
 */
public class RemoteBroker extends Sim_entity {
	
	private static final String DELAY = "RemoteDelaySource";
	
	
	int jobsToGenerate;
	private Sim_port out;
	private Sim_negexp_obj delay;

	
	public RemoteBroker(String name, double meanRemoteJobCreation, int jobsToGenerate) {
		super(name);
		
		out = new Sim_port(Constants.OUT_REMOTE_BROKER);
		add_port(out);
		
		// generates a random number to pauses the object
		delay = new Sim_negexp_obj(DELAY, meanRemoteJobCreation); 
		add_generator(delay);
		
		this.jobsToGenerate = jobsToGenerate;
	}
	
	public void body() {
		for (int i = 0; i < jobsToGenerate; i++) {
			sim_trace(1, Constants.REMOTE+" selected for processing work.");
			sim_schedule(out, 0.0, Constants.REMOTE, new Job());
			sim_pause(delay.sample());
		}
	}

}
