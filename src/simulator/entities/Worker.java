package simulator.entities;
import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_from_p;
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
//	private int idleWorkers;
//	private List<BufferedEvent> bufferedEvents = new ArrayList<BufferedEvent>();
	
	
	public Worker(String name, double meanDownload, double meanExecution, double meanUpload) {
		super(name);
		
//		this.idleWorkers = idleWorkers;
		
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
		double localExecutionsAverageTime = 0;
		double remoteExecutionsAverageTime = 0;
		double localCompletedExecutions = 0;
		double remoteCompletedExecutions = 0;
		
		while (Sim_system.running()) {
			Sim_event e = new Sim_event();
			sim_select(new Sim_from_p(Sim_system.get_entity_id(Constants.LOCAL_BROKER)), e); //priority to local brokers
			
			if (e.get_tag() == -1) { //means that there is not a local job
				sim_get_next(e);
			}
			
			double time = Sim_system.clock();
			Job job = (Job) e.get_data();
			
			if (job == null) continue;
			
			job.setExecutionTime(time);
			
			sim_pause(downloadDelay.sample()); //download delay
			sim_process(executionDelay.sample()); //execution delay
			sim_pause(uploadDelay.sample()); //upload delay
			
			//end the process
			sim_completed(e);
			
			double now = Sim_system.clock();

			if (e.get_tag() == Constants.LOCAL) {
				localCompletedExecutions++;
				localExecutionsAverageTime += (job.getExecutionTime() - job.getCreationTime()) + (now - time);
			} else {
				remoteCompletedExecutions++;
				remoteExecutionsAverageTime += (job.getExecutionTime() - job.getCreationTime()) + (now - time);
			}
		}

		System.out.println();
		System.out.println("EXECUTIONS COMPLETED:");
		System.out.println("Total completed executions: " + ((int) localCompletedExecutions + (int) remoteCompletedExecutions));
		System.out.println("Local completed executions: " + (int) localCompletedExecutions);
		System.out.println("Remote completed executions: " + (int) remoteCompletedExecutions);
		System.out.println("Executions average time: " + (localExecutionsAverageTime + remoteExecutionsAverageTime)/
				(localCompletedExecutions + remoteCompletedExecutions) + " seconds");
		System.out.println("Local executions average time: " + localExecutionsAverageTime/localCompletedExecutions + " seconds");
		System.out.println("Remote executions average time: " + remoteExecutionsAverageTime/remoteCompletedExecutions + " seconds");
		System.out.println();
		
		//reset the parameters
		localExecutionsAverageTime = 0;
		remoteExecutionsAverageTime = 0;
		localCompletedExecutions = 0;
		remoteCompletedExecutions = 0;
	}
}
