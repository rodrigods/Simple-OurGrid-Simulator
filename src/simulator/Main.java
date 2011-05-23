package simulator;

import simulator.entities.Broker;
import simulator.entities.Constants;
import simulator.entities.Worker;
import eduni.simjava.Sim_stat;
import eduni.simjava.Sim_system;

public class Main {
	
	private static void init() {
		Sim_system.initialise();

		//TODO get those configurations from a configuration
		new Broker(Constants.BROKER, 60, 0.5, 100);
		new Worker(Constants.WORKER, 5, 1, 2, 1);
	}
	
	private static void linkPorts() {
		Sim_system.link_ports(Constants.BROKER, Constants.OUT_BROKER, Constants.WORKER, Constants.IN_WORKER);
	}
	
	public static void main(String[] args) {
		init();
		linkPorts();
		
		Sim_system.set_trace_detail(false, false, false);
		
		Sim_system.set_transient_condition(Sim_system.TIME_ELAPSED, 1000);
		
		Sim_system.set_termination_condition(Sim_system.INTERVAL_ACCURACY, Sim_system.IND_REPLICATIONS, 0.90, 0.05,
				Constants.WORKER, Sim_stat.UTILISATION);
		
		Sim_system.set_report_detail(false, false);
		Sim_system.generate_graphs("report.sjg");
		Sim_system.run();
	}
}
