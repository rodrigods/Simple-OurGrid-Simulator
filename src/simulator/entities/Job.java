package simulator.entities;

import eduni.simjava.Sim_system;

public class Job {
	private Double creationTime;
	private Double executionTime;
	
	
	public Job() {
		this.creationTime = Sim_system.clock();
	}


	public void setCreationTime(Double creationTime) {
		this.creationTime = creationTime;
	}

	public Double getCreationTime() {
		return creationTime;
	}

	public void setExecutionTime(Double executionTime) {
		this.executionTime = executionTime;
	}

	public Double getExecutionTime() {
		return executionTime;
	}
}
