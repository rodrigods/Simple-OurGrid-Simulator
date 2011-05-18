import eduni.simjava.*;
import eduni.simjava.distributions.*;
import eduni.simanim.*;

class Source extends Sim_entity {
  private Sim_port out;
  private Sim_negexp_obj delay;

  Source(String name, int x, int y, String image, double mean){
    super(name, image, x, y);
    out = new Sim_port("Out", "port", Anim_port.RIGHT, 40);
    add_port(out);
    delay = new Sim_negexp_obj("Delay", mean);
    add_generator(delay);
  }

  public void body() {
    while (Sim_system.running()) {
      sim_schedule(out, 0.0, 0);
      sim_trace(1, "S Out");
      sim_pause(delay.sample());
    }
  }
}

class Sink extends Sim_entity {
  private Sim_port in, out1, out2;
  private Sim_normal_obj delay;
  private Sim_random_obj prob;
  private Sim_stat stat;

  Sink(String name, int x, int y, String image, double mean, double var) {
    super(name, image, x, y);
    in = new Sim_port("In", "port", Anim_port.LEFT, 40);
    out1 = new Sim_port("Out1", "port", Anim_port.RIGHT, 20);
    out2 = new Sim_port("Out2", "port", Anim_port.RIGHT, 60);
    add_port(in);
    add_port(out1);
    add_port(out2);
    delay = new Sim_normal_obj("Delay", mean, var);
    prob = new Sim_random_obj("Probability");
    add_generator(delay);
    add_generator(prob);
    stat = new Sim_stat();
    stat.add_measure(Sim_stat.THROUGHPUT);
    stat.add_measure(Sim_stat.RESIDENCE_TIME);
    stat.add_measure("Thread use", Sim_stat.STATE_BASED, false);
    stat.calc_proportions("Thread use", new double[] { 0, 1, 2, 3, 4});
    set_stat(stat);
  }

  public void body() {
    while (Sim_system.running()) {
      Sim_event e = new Sim_event();
      sim_get_next(e);
      double before = Sim_system.sim_clock();
      sim_process(delay.sample());
      sim_completed(e);
      double p = prob.sample();
      if (p < 0.15) {
        stat.update("Thread use", 1, before, Sim_system.sim_clock());
      } else if (p < 0.75) {
        stat.update("Thread use", 2, before, Sim_system.sim_clock());
      } else {
        stat.update("Thread use", 3, before, Sim_system.sim_clock());
      }
      p = prob.sample();
      if (p < 0.60) {
        sim_schedule(out1, 0.0, 1);
        sim_trace(1, "S Out1");
      } else {
        sim_schedule(out2, 0.0, 1);
        sim_trace(1, "S Out2");
      }
    }
  }
}

class Disk extends Sim_entity {
  private Sim_port in;
  private Sim_normal_obj delay;
  private Sim_stat stat;

  Disk(String name, int x, int y, String image, double mean, double var) {
    super(name, image, x, y);
    in = new Sim_port("In", "port", Anim_port.LEFT, 40);
    add_port(in);
    delay = new Sim_normal_obj("Delay", mean, var);
    add_generator(delay);
    stat = new Sim_stat();
    stat.add_measure(Sim_stat.UTILISATION);
    set_stat(stat);
    add_param(new Anim_param("Busy", Anim_param.STATE, "idle", 0, 0));
  }

  public void body() {
    while (Sim_system.running()) {
      Sim_event e = new Sim_event();
      sim_get_next(e);
      sim_trace(1, "P busy");
      sim_process(delay.sample());
      sim_trace(1, "P idle");
      sim_completed(e);
    }
  }
}

public class ProcessorSubsystem6 extends Anim_applet {

  // Setup the animation
  public void anim_layout() {
    Source source = new Source("Source", 10, 60, "source", 150.45);
    Sink processor = new Sink("Processor", 140, 60, "cpu", 110.5, 90.5);
    Disk disk1 = new Disk("Disk1", 370, 10, "disk1", 130.0, 65.0);
    Disk disk2 = new Disk("Disk2", 370, 110, "disk2", 350.5, 200.5);
    Sim_system.link_ports("Source", "Out", "Processor", "In");
    Sim_system.link_ports("Processor", "Out1", "Disk1", "In");
    Sim_system.link_ports("Processor", "Out2", "Disk2", "In");
  }

  public void sim_setup() {
    Sim_system.set_transient_condition(Sim_system.TIME_ELAPSED, 10000);
    Sim_system.set_termination_condition(Sim_system.EVENTS_COMPLETED,
                                         "Processor", 0, 50, false);
    Sim_system.set_output_analysis(Sim_system.IND_REPLICATIONS, 2, 0.95);
    Sim_system.set_report_detail(true, false);
  }

  // Choose animation output detail
  public void anim_output() {
    generate_report(true);
    generate_messages(true);
  }
}
