package simulator.entities;
import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_port;
import eduni.simjava.distributions.Sim_negexp_obj;


/**
 * Represents the Broker, the source entity
 * It generates jobs to be executed in the Workers
 *
 */
public class LocalBroker extends Sim_entity {
	
	private static final String DELAY = "LocalDelaySource";
	
	
	int jobsToGenerate;
	private Sim_port out;
	private Sim_negexp_obj delay;

	
	public LocalBroker(String name, double meanLocalJobCreation, int jobsToGenerate) {
		super(name);
		
		out = new Sim_port(Constants.OUT_LOCAL_BROKER);
		add_port(out);
		
		// generates a random number to pauses the object
		delay = new Sim_negexp_obj(DELAY, meanLocalJobCreation); 
		add_generator(delay);
		
		this.jobsToGenerate = jobsToGenerate;
	}
	
	public void body() {
		for (int i = 0; i < jobsToGenerate; i++) {
			sim_trace(1, Constants.LOCAL+" selected for processing work.");
			sim_schedule(out, 0.0, Constants.LOCAL, new Job());
			sim_pause(delay.sample());
		}
	}

}
