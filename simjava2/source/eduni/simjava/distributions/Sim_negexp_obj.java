/* Sim_negexp_obj.java */

package eduni.simjava.distributions;

/**
 * A random number generator based on the Negative Exponential distribution.
 * @version     1.0, 13 May 2002
 * @author      Costas Simatos
 */

public class Sim_negexp_obj implements ContinuousGenerator {
  private Sim_random_obj source;
  private double mean;
  private String name;

  /**
   * Constructor with which <code>Sim_system</code> is allowed to set the random number
   * generator's seed
   * @param name The name to be associated with this instance
   * @param mean The mean of the exponential distribution
   */
  public Sim_negexp_obj(String name, double mean) {
    if (mean <= 0.0) {
      throw new Sim_parameter_exception("Sim_negexp_obj: The mean must be greater than 0.");
    }
    source = new Sim_random_obj("Internal PRNG");
    this.mean = mean;
    this.name = name;
  }

  /**
   * The constructor with which a specific seed is set for the random
   * number generator
   * @param name The name to be associated with this instance
   * @param mean The mean of the exponential distribution
   * @param seed The initial seed for the generator, two instances with
   *             the same seed will generate the same sequence of numbers
   */
  public Sim_negexp_obj(String name, double mean, long seed) {
    if (mean <= 0.0) {
      throw new Sim_parameter_exception("Sim_negexp_obj: The mean must be greater than 0.");
    }
    source = new Sim_random_obj("Internal PRNG", seed);
    this.mean = mean;
    this.name = name;
  }

  /**
   * Generate a new random number.
   * @return The next random number in the sequence
   */
  public double sample() {
    return -mean * Math.log(source.sample());
  }

  // Used by other distributions that rely on the Negative Exponential distribution
  static double sample(Sim_random_obj source, double mean) {
    return -mean * Math.log(source.sample());
  }

  /**
   * Set the random number generator's seed.
   * @param seed The new seed for the generator
   */
  public void set_seed(long seed) {
    source.set_seed(seed);
  }

  /**
   * Get the random number generator's seed.
   * @return The generator's seed
   */
  public long get_seed() {
    return source.get_seed();
  }

  /**
   * Get the random number generator's name.
   * @return The generator's name
   */
  public String get_name() {
    return name;
  }
}
