import eduni.simjava.*;

class Source extends Sim_entity {      // The class for the source
  private Sim_port out;
  private double delay;

  Source(String name, double delay) {
    super(name);
    this.delay = delay;
    out = new Sim_port("Out");         // Port for sending events to the processor
    add_port(out);
  }

  public void body() {
    for (int i=0; i < 100; i++) {
      sim_schedule(out, 0.0, 0);       // Send the processor a job
      sim_pause(delay);                // Pause
    }
  }
}

class Sink extends Sim_entity {        // The class for the processor
  private Sim_port in;
  private Sim_port out1, out2;
  private double delay;

  Sink(String name, double delay) {
    super(name);
    this.delay = delay;
    in = new Sim_port("In");           // Port for receiving events from the source
    out1 = new Sim_port("Out1");       // Port for sending events to disk 1
    out2 = new Sim_port("Out2");       // Port for sending events to disk 2
    add_port(in);
    add_port(out1);
    add_port(out2);
  }

  public void body() {
    int i = 0;
    while (Sim_system.running()) {
      Sim_event e = new Sim_event();
      sim_get_next(e);                 // Get the next event
      sim_process(delay);              // Process the event
      sim_completed(e);                // The event has completed service
      if ((i % 2) == 0) {
        sim_schedule(out1, 0.0, 1);    // Even I/O jobs go to disk 1
      } else {
        sim_schedule(out2, 0.0, 1);    // Odd I/O jobs go to disk 2
      }
      i++;
    }
  }
}

class Disk extends Sim_entity {        // The class for the two disks
  private Sim_port in;
  private double delay;

  Disk(String name, double delay) {
    super(name);
    this.delay = delay;
    in = new Sim_port("In");           // Port for receiving events from the processor
    add_port(in);
  }

  public void body() {
    while (Sim_system.running()) {
      Sim_event e = new Sim_event();
      sim_get_next(e);                 // Get the next event
      sim_process(delay);              // Process the event
      sim_completed(e);                // The event has completed service
    }
  }
}

public class ProcessorSubsystem {
  public static void main(String[] args) {      // The main method
    Sim_system.initialise();                    // Initialise Sim_system
    Source source = new Source("Source", 50);   // Add the source
    Sink processor = new Sink("Processor", 30); // Add the processor
    Disk disk1 = new Disk("Disk1", 60);         // Add disk 1
    Disk disk2 = new Disk("Disk2", 110);        // Add disk 2
    // Link the entities' ports
    Sim_system.link_ports("Source", "Out", "Processor", "In");
    Sim_system.link_ports("Processor", "Out1", "Disk1", "In");
    Sim_system.link_ports("Processor", "Out2", "Disk2", "In");
    Sim_system.run();                           // Run the simulation
  }
}
