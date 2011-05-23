package simulator.entities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_stat;
import eduni.simjava.Sim_system;
import eduni.simjava.distributions.Sim_negexp_obj;

/**
 * Represents the Worker, the server entity
 *
 */
public class Worker extends Sim_entity {
	
	private static final String DOWNLOAD_DELAY = "DownloadDelaySource";
	private static final String EXECUTION_DELAY = "ExecutionDelaySource";
	private static final String UPLOAD_DELAY = "UploadDelaySource";
	
	
	private Sim_port in;
	private Sim_negexp_obj downloadDelay;
	private Sim_negexp_obj executionDelay;
	private Sim_negexp_obj uploadDelay;
	private Sim_stat stat; 
	private int idleWorkers;
	private List<BufferedEvent> bufferedEvents = new ArrayList<BufferedEvent>();
	
	
	public Worker(String name, double meanDownload, double meanExecution, double meanUpload, int idleWorkers) {
		super(name);
		
		this.idleWorkers = idleWorkers;
		
		in = new Sim_port(Constants.IN_WORKER); //new jobs
		add_port(in);
		
		// generates a random number to represent network download delay
		downloadDelay = new Sim_negexp_obj(DOWNLOAD_DELAY, meanDownload); 
		add_generator(downloadDelay);
		
		// generates a random number to represent the execution delay
		executionDelay = new Sim_negexp_obj(EXECUTION_DELAY, meanExecution); 
		add_generator(executionDelay);
		
		// generates a random number to represent network upload delay
		uploadDelay = new Sim_negexp_obj(UPLOAD_DELAY, meanUpload); 
		add_generator(uploadDelay);
		
		//statistics
		stat = new Sim_stat();
		stat.add_measure(Sim_stat.UTILISATION);
		stat.add_measure(Sim_stat.SERVICE_TIME);
		stat.add_measure(Sim_stat.INTERVAL_BASED);
		stat.add_measure(Sim_stat.WAITING_TIME);
		set_stat(stat);
	}

	public void body() {
		//outputs
		double executionsAverageTime = 0;
		double localCompletedExecutions = 0;
		double remoteCompletedExecutions = 0;
		
		while (Sim_system.running()) {
			//verify if there is buffered events
			BufferedEvent bufferedEvent = bufferedEvents.isEmpty() ? null : bufferedEvents.remove(0);
			Sim_event e = null;
			
			if (bufferedEvent == null) {
				e = new Sim_event();
				sim_get_next(e);
			} else {
				e = bufferedEvent.getEvent();
			}
			
			if (idleWorkers > 0) {
				idleWorkers--; //the worker will be allocated
				double before = Sim_system.clock();
				
				sim_pause(downloadDelay.sample()); //download delay
				sim_process(executionDelay.sample()); //execution delay
				sim_pause(uploadDelay.sample()); //upload delay
				
				//XXX is the same? sim_process(downloadDelay.sample() + executionDelay.sample() + uploadDelay.sample()); 
				
				//end the process
				sim_completed(e);
				idleWorkers++; //the worker completed the job, go to idle again
				
				if (e.get_tag() == Constants.LOCAL) {
					localCompletedExecutions++;
				} else {
					remoteCompletedExecutions++;
				}
				
				double now = Sim_system.clock();
				
				if (bufferedEvent == null) {
					executionsAverageTime += now - before;
				} else {
					executionsAverageTime += (now - before) + (now - bufferedEvent.getTimeStamp());
				}
			} else {
				bufferedEvents.add(new BufferedEvent(e, Sim_system.clock()));
			}
			
			sortBufferedEvents();
			
		}

		System.out.println();
		System.out.println("EXECUTIONS COMPLETED:");
		System.out.println("Total completed executions: " + ((int) localCompletedExecutions + (int) remoteCompletedExecutions));
		System.out.println("Local completed executions: " + (int) localCompletedExecutions);
		System.out.println("Remote completed executions: " + (int) remoteCompletedExecutions);
		System.out.println("Executions average time: " + executionsAverageTime/(localCompletedExecutions + remoteCompletedExecutions));
		System.out.println();
		
		//reset the parameters
		localCompletedExecutions = 0;
		remoteCompletedExecutions = 0;
		executionsAverageTime = 0;
	}
	
	private void sortBufferedEvents() {
		//sort incoming events by priority, incoming events by local brokers has higher priorities
		Collections.sort(bufferedEvents, new Comparator<BufferedEvent>() {
			@Override
			public int compare(BufferedEvent o1, BufferedEvent o2) {
				if (o1.getEvent().get_tag() == Constants.LOCAL) {
					return o2.getEvent().get_tag() == Constants.LOCAL ? 0 : -1;
				}
				
				return o2.getEvent().get_tag() == Constants.REMOTE ? 0 : 1;
			}
		});
	}
}
