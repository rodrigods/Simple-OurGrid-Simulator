import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_system;

      // The class for the source
      class Source extends Sim_entity {
        private Sim_port out;
        private double delay;

        Source(String name, double delay) {
          super(name);
          this.delay = delay;
          // Port for sending events to the processor
          out = new Sim_port("Out");
          add_port(out);
        }

        public void body() {
          for (int i=0; i < 100; i++) {
            // Send the processor a job
            sim_schedule(out, 0.0, 0);
            // Pause
            sim_pause(delay);
          }
        }
      }

      // The class for the processor
      class Sink extends Sim_entity {
        private Sim_port in, out1, out2;
        private double delay;

        Sink(String name, double delay) {
          super(name);
          this.delay = delay;
          // Port for receiving events from the source
          in = new Sim_port("In");
          // Port for sending events to disk 1
          out1 = new Sim_port("Out1");
          // Port for sending events to disk 2
          out2 = new Sim_port("Out2");
          add_port(in);
          add_port(out1);
          add_port(out2);
        }

        public void body() {
          int i = 0;
          while (Sim_system.running()) {
            Sim_event e = new Sim_event();
            // Get the next event
            sim_get_next(e);
            // Process the event
            sim_process(delay);
            // The event has completed service
            sim_completed(e);
            if ((i % 2) == 0) {
              // Even I/O jobs go to disk 1
              sim_schedule(out1, 0.0, 1);
            } else {
              // Odd I/O jobs go to disk 2
              sim_schedule(out2, 0.0, 1);
            }
            i++;
          }
        }
      }

      // The class for the two disks
      class Disk extends Sim_entity {
        private Sim_port in;
        private double delay;

        Disk(String name, double delay) {
          super(name);
          this.delay = delay;
          // Port for receiving events from the processor
          in = new Sim_port("In");
          add_port(in);
        }

        public void body() {
          while (Sim_system.running()) {
            Sim_event e = new Sim_event();
            // Get the next event
            sim_get_next(e);
            // Process the event
            sim_process(delay);
            // The event has completed service
            sim_completed(e);
          }
        }
      }