import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_system;


/**
 * Represents the network that exists between the Broker and Worker
 * @author rodrigo
 *
 */
public class Network extends Sim_entity {
	
	//links that represents the communication between two elements at the network
	private Sim_port inBroker;
	private Sim_port inWorker;
	private Sim_port outBroker;
	private Sim_port outWorker;
	
	//network delay
	private double delay;
	

	public Network(String name, double delay) {
		super(name);
		this.delay = delay;
		
		inBroker = new Sim_port("inBroker");
		inWorker = new Sim_port("inWorker");
		outBroker = new Sim_port("outBroker");
		outWorker = new Sim_port("outWorker");
	}
	
    public void body() {
        while (Sim_system.running()) {
          Sim_event e = new Sim_event();
          //get the next event
          sim_get_next(e);
          
          if (e.from_port(inBroker)) {
        	  sim_schedule(outWorker, delay, e.get_tag(), e.get_data());
          } else if (e.from_port(inWorker)) {
        	  sim_schedule(outBroker, delay, e.get_tag(), e.get_data());
          }
        }
      }
	
}
