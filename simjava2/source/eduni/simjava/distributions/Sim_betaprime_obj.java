/* Sim_betaprime_obj.java */

package eduni.simjava.distributions;

/**
 * A random number generator based on the Beta Prime distribution.
 * @version     1.0, 14 May 2002
 * @author      Costas Simatos
 */

public class Sim_betaprime_obj implements ContinuousGenerator {
  private Sim_random_obj source;
  private double shape_a, shape_b;
  private String name;

  /**
   * Constructor with which <code>Sim_system</code> is allowed to set the random number
   * generator's seed
   * @param name The name to be associated with this instance
   * @param shape_a The a shape parameter of the distribution
   * @param shape_b The b shape parameter of the distribution
   */
  public Sim_betaprime_obj(String name, double shape_a, double shape_b) {
    if ((shape_a <= 0.0) || (shape_b <= 0.0)) {
      throw new Sim_parameter_exception("Sim_betaprime_obj: The shape parameters must be greater than 0.");
    }
    source = new Sim_random_obj("Internal PRNG");
    this.shape_a = shape_a;
    this.shape_b = shape_b;
    this.name = name;
  }

  /**
   * The constructor with which a specific seed is set for the random
   * number generator
   * @param name The name to be associated with this instance
   * @param shape_a The a shape parameter of the distribution
   * @param shape_b The b shape parameter of the distribution
   * @param seed The initial seed for the generator, two instances with
   *             the same seed will generate the same sequence of numbers
   */
  public Sim_betaprime_obj(String name, double shape_a, double shape_b, long seed) {
    if ((shape_a <= 0.0) || (shape_b <= 0.0)) {
      throw new Sim_parameter_exception("Sim_betaprime_obj: The shape parameters must be greater than 0.");
    }
    source = new Sim_random_obj("Internal PRNG", seed);
    this.shape_a = shape_a;
    this.shape_b = shape_b;
    this.name = name;
  }

  /**
   * Generate a new random number.
   * @return The next random number in the sequence
   */
  public double sample() {
    return (1.0/Sim_beta_obj.sample(source, shape_a, shape_b)) - 1.0;
  }

  // Used by other distributions that rely on the Beta Prime distribution
  static double sample(Sim_random_obj source, double shape_a, double shape_b) {
    return (1.0/Sim_beta_obj.sample(source, shape_a, shape_b)) - 1.0;
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
