package simulator.entities;

import eduni.simjava.Sim_port;

public class WorkerPorts {
	private Sim_port in;
	private Sim_port out;
	private Worker worker;
	
	
	public WorkerPorts(Sim_port in, Sim_port out, Worker worker) {
		this.setIn(in);
		this.setOut(out);
		this.setWorker(worker);
	}

	
	public void setIn(Sim_port in) {
		this.in = in;
	}

	public Sim_port getIn() {
		return in;
	}

	public void setOut(Sim_port out) {
		this.out = out;
	}

	public Sim_port getOut() {
		return out;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	public Worker getWorker() {
		return worker;
	}
}
