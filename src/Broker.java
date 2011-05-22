import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_system;


/**
 * Represents the Broker, the source entity
 * It generates jobs to be executed in the Workers
 * @author rodrigo
 *
 */
public class Broker extends Sim_entity {
	
	private Sim_port inPeer;
	private Sim_port outPeer;

	//connections to the worker (actually, it is with the network, that receives/passes from/to the worker)
	private Sim_port inWorker;
	private Sim_port outWorker;
	
	private boolean sentJob;

	public Broker(String name) {
		super(name);
		
		inPeer = new Sim_port("inPeer");
		outPeer = new Sim_port("outPeer");
		inWorker = new Sim_port("inWorker");
		outWorker = new Sim_port("outWorker");
		
		sentJob = false;
	}
	
	public void body() {
		while (Sim_system.running()) {
			//if the job hasn't be sent to peer queue, send it
			if (!sentJob) {
				sim_schedule(outPeer, 0.0, 1);
				sentJob = true;
			}
			
	          Sim_event e = new Sim_event();
	          //get the next event
	          sim_get_next(e);
	          
	          if (e.from_port(inPeer)) { //peer is responding to the job request
	        	  sim_schedule(outWorker, 0.0, e.get_tag(), e.get_data()); //using the scheduled worker
	          } else if (e.from_port(inWorker)) {
	        	  //job is finished
	        	  //TODO actions related to a finished job
	          }
		}
	}

}
