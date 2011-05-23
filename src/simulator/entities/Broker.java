package simulator.entities;
import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_port;
import eduni.simjava.distributions.Sim_negexp_obj;


/**
 * Represents the Broker, the source entity
 * It generates jobs to be executed in the Workers
 *
 */
public class Broker extends Sim_entity {
	
	private static final String DELAY = "DelaySource";
	private static final String LOCAL_OR_REMOTE = "LocalOrRemoteSource";
	
	
	int jobsToGenerate;
	private Sim_port out;
	private Sim_negexp_obj delay;
	private Sim_negexp_obj localOrRemote;

	
	public Broker(String name, double meanJobCreation, double meanLocalOrRemote, int jobsToGenerate) {
		super(name);
		
		out = new Sim_port(Constants.OUT_BROKER);
		add_port(out);
		
		// generates a random number to pauses the object
		delay = new Sim_negexp_obj(DELAY, meanJobCreation); 
		add_generator(delay);
		
		// generates a random number to decide if the broker is local or remote
		localOrRemote = new Sim_negexp_obj(LOCAL_OR_REMOTE, meanLocalOrRemote); 
		add_generator(localOrRemote);
		
		this.jobsToGenerate = jobsToGenerate;
	}
	
	public void body() {
		for (int i = 0; i < jobsToGenerate; i++) {
			if (localOrRemote.sample() > 0.5) {
				sim_trace(1, Constants.LOCAL+" selected for processing work.");
				sim_schedule(out, 0.0, Constants.LOCAL);
			} else {
				sim_trace(1, Constants.REMOTE+" selected for processing work.");
				sim_schedule(out, 0.0, Constants.REMOTE);
			}
			
			sim_pause(delay.sample());
		}
	}

}
