/* Sim_pareto_obj.java */

package eduni.simjava.distributions;

/**
 * A random number generator based on the Pareto distribution.
 * @version     1.0, 13 May 2002
 * @author      Costas Simatos
 */

public class Sim_pareto_obj implements ContinuousGenerator {
  private Sim_random_obj source;
  private double shape, scale;
  private String name;

  /**
   * Constructor with which <code>Sim_system</code> is allowed to set the random number
   * generator's seed
   * @param name The name to be associated with this instance
   * @param shape The shape of the distribution
   * @param scale The scale of the distribution
   */
  public Sim_pareto_obj(String name, double shape, double scale) {
    if ((shape <= 0.0) || (scale <= 0.0)) {
      throw new Sim_parameter_exception("Sim_pareto_obj: Shape and scale parameters must be greater than 0.");
    }
    source = new Sim_random_obj("Internal PRNG");
    this.shape = shape;
    this.name = name;
  }

  /**
   * The constructor with which a specific seed is set for the random
   * number generator
   * @param name The name to be associated with this instance
   * @param shape The shape of the distribution
   * @param scale The scale of the distribution
   * @param seed The initial seed for the generator, two instances with
   *             the same seed will generate the same sequence of numbers
   */
  public Sim_pareto_obj(String name, double shape, double scale, long seed) {
    if ((shape <= 0.0) || (scale <= 0.0)) {
      throw new Sim_parameter_exception("Sim_pareto_obj: Shape and scale parameters must be greater than 0.");
    }
    source = new Sim_random_obj("Internal PRNG", seed);
    this.shape = shape;
    this.name = name;
}

  /**
   * Generate a new random number.
   * @return The next random number in the sequence
   */
  public double sample() {
    return scale / Math.pow(source.sample(), 1/shape);
  }

  // Used by other distributions that rely on the Pareto distribution
  static double sample(Sim_random_obj source, double shape, double scale) {
    return scale / Math.pow(source.sample(), 1/shape);
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
