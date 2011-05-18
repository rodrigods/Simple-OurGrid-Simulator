import eduni.simjava.*;
// Import the distributions
import eduni.simjava.distributions.*;

class Source extends Sim_entity {
  private Sim_port out;
  private Sim_negexp_obj delay;

  Source(String name, double mean) {
    super(name);
    out = new Sim_port("Out");
    add_port(out);
    // Create the source's distribution and add it
    delay = new Sim_negexp_obj("Delay", mean);
    add_generator(delay);
  }

  public void body() {
    for (int i=0; i < 100; i++) {
      sim_schedule(out, 0.0, 0);
      // Sample the distribution
      sim_pause(delay.sample());
    }
  }
}

class Sink extends Sim_entity {
  private Sim_port in;
  private Sim_port out1, out2;
  private Sim_normal_obj delay;
  private Sim_random_obj prob;

  Sink(String name, double mean, double var) {
    super(name);
    in = new Sim_port("In");
    out1 = new Sim_port("Out1");
    out2 = new Sim_port("Out2");
    add_port(in);
    add_port(out1);
    add_port(out2);
    // Create the processor's distribution and probability generator and add them
    delay = new Sim_normal_obj("Delay", mean, var);
    prob = new Sim_random_obj("Probability");
    add_generator(delay);
    add_generator(prob);
  }

  public void body() {
    while (Sim_system.running()) {
      Sim_event e = new Sim_event();
      sim_get_next(e);
      // Sample the distribution
      sim_process(delay.sample());
      sim_completed(e);
      // Get the next probability
      double p = prob.sample();
      if (p < 0.60) {
        sim_schedule(out1, 0.0, 1);
      } else {
        sim_schedule(out2, 0.0, 1);
      }
    }
  }
}

class Disk extends Sim_entity {
  private Sim_port in;
  private Sim_normal_obj delay;

  Disk(String name, double mean, double var) {
    super(name);
    in = new Sim_port("In");
    add_port(in);
    // Create the disk's distribution and add it
    delay = new Sim_normal_obj("Delay", mean, var);
    add_generator(delay);
  }

  public void body() {
    while (Sim_system.running()) {
      Sim_event e = new Sim_event();
      sim_get_next(e);
      // Sample the distribution
      sim_process(delay.sample());
      sim_completed(e);
    }
  }
}

public class ProcessorSubsystem1 {
  public static void main(String[] args) {
    Sim_system.initialise();
    Source source = new Source("Source", 150.45);
    Sink processor = new Sink("Processor", 110.5, 90.5);
    Disk disk1 = new Disk("Disk1", 130.0, 65.0);
    Disk disk2 = new Disk("Disk2", 350.5, 200.5);
    Sim_system.link_ports("Source", "Out", "Processor", "In");
    Sim_system.link_ports("Processor", "Out1", "Disk1", "In");
    Sim_system.link_ports("Processor", "Out2", "Disk2", "In");
    Sim_system.run();
  }
}
