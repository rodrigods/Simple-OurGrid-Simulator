package simulator.entities;

import eduni.simjava.Sim_event;

public class BufferedEvent {
	private Sim_event event;
	private double timeStamp;
	
	
	public BufferedEvent(Sim_event event, double timeStamp) {
		this.setEvent(event);
		this.setTimeStamp(timeStamp);
	}

	
	public void setEvent(Sim_event event) {
		this.event = event;
	}

	public Sim_event getEvent() {
		return event;
	}

	public void setTimeStamp(double timeStamp) {
		this.timeStamp = timeStamp;
	}

	public double getTimeStamp() {
		return timeStamp;
	}
}
