package fr.imt.ales.redoc.cba.dedal.metrics;

/**
 * Class containing static attributes and methods for applying metrics on reconstruction
 * @author Alexandre Le Borgne
 *
 */
public class Metrics {
	
	/**
	 * Analyzed Java classes
	 */
	private static double nbClasses = 0;
	/**
	 * Analyzed Spring XML descriptors
	 */
	private static double nbSpringXML = 0;
	/**
	 * Reconstructed Dedal Specification
	 */
	private static double nbSpecs = 0;
	/**
	 * Reconstructed Dedal Configurations
	 */
	private static double nbConfs = 0;
	/**
	 * Reconstructed Dedal Assemblies
	 */
	private static double nbAssembs = 0;
	/**
	 * Reconstructed Dedal component classes
	 */
	private static double nbCompsClasses = 0;
	/**
	 * Reconstructed Dedal component instances
	 */
	private static double nbCompsInst = 0;
	/**
	 * Reconstructed Dedal component roles
	 */
	private static double nbCompsRoles = 0;
	/**
	 * Empty Spring XML descriptors (no architecture-like information)
	 */
	private static double nbEmptySpringXML = 0;
	/**
	 * Reconstructed Dedal Specifications which are
	 * the same as their corresponding Reconstructed
	 * Dedal Configurations
	 */
	private static double nbSpecsEqualsConf = 0;
	/**
	 * Reconstructed Dedal Interfaces
	 */
	private static double nbInterfaces = 0;
	/**
	 * Reconstructed connections between components
	 */
	private static double nbConnections = 0;
	/**
	 * Reconstructed Dedal architecture description without connections
	 */
	private static double nbConnectionlessArchis = 0;
	/**
	 * Reconstructed Dedal component classes realizing several component roles
	 */
	private static double nbCompClassMultiRoles= 0;
	/**
	 * Reconstructed Dedal Interfaces that have been split
	 */
	private static double nbSplitInterfaces = 0;
	/**
	 * Reconstructed Dedal Interfaces that could have been replaced by more abstract ones
	 */
	private static double nbAbstractedInterfaceType= 0;
	/**
	 * Java classes that could not be loaded because of various issues
	 */
	private static double nbFailedClass= 0;
	
	/**
	 * Default private constructor for avoiding instantiation
	 */
	private Metrics() {}
	
	/**
	 * Get the amount of analyzed Java classes
	 * @return {@link #nbClasses}
	 */
	public static double getNbClasses() {
		return nbClasses;
	}
	/**
	 * Increments the amount of analyzed Java classes
	 */
	public static void addNbClasses(){
		Metrics.nbClasses++;
	}
	/**
	 * Increments the amount of analyzed Java classes by <b>nb</b>
	 * @param nb number of classes that have been analyzed
	 */
	public static void addNbClasses(int nb){
		Metrics.nbClasses+=nb;
	}
	/**
	 * Get the amount of analyzed XML-based descriptors
	 * @return {@link #nbSpringXML}
	 */
	public static double getNbSpringXML() {
		return nbSpringXML;
	}
	/**
	 * Increments the amount of analyzed XML-based descriptors
	 */
	public static void addNbSpringXML() {
		Metrics.nbSpringXML++;
	}
	/**
	 * Get the amount of generated Dedal Specifications
	 * @return {@link #nbSpecs}
	 */
	public static double getNbSpecs() {
		return nbSpecs;
	}
	/**
	 * Increments the amount of generated Dedal Specifications
	 */
	public static void addNbSpecs() {
		Metrics.nbSpecs++;
	}
	/**
	 * Get the amount of Interfaces that have been split during the reconstruction
	 * @return {@link #nbSplitInterfaces}
	 */
	public static double getNbSplitInterfaces() {
		return nbSplitInterfaces;
	}
	/**
	 * Increments the amount of Interfaces that have been split during the reconstruction
	 */
	public static void addNbSplitInterfaces() {
		Metrics.nbSplitInterfaces++;
	}
	/**
	 * Get the amount of reconstructed Dedal Configurations
	 * @return {@link #nbConfs}
	 */
	public static double getNbConfs() {
		return nbConfs;
	}
	/**
	 * Increments the amount of reconstructed Dedal Configurations
	 */
	public static void addNbConfs() {
		Metrics.nbConfs++;
	}
	/**
	 * Get the amount of reconstructed Dedal Assemblies
	 * @return {@link #nbAssembs}
	 */
	public static double getNbAssembs() {
		return nbAssembs;
	}
	/**
	 * Increments the amount of reconstructed Dedal Assemblies
	 */
	public static void addNbAssembs() {
		Metrics.nbAssembs++;
	}
	/**
	 * Get the amount of reconstructed Dedal component classes
	 * @return {@link #nbCompsClasses}
	 */
	public static double getNbCompsClasses() {
		return nbCompsClasses;
	}
	/**
	 * Increments the amount of reconstructed Dedal component classes
	 */
	public static void addNbCompsClasses() {
		Metrics.nbCompsClasses++;
	}
	/**
	 * Get the amount of reconstructed Dedal component instances
	 * @return {@link #nbCompsInst}
	 */
	public static double getNbCompsInst() {
		return nbCompsInst;
	}
	/**
	 * Increments the amount of reconstructed Dedal component instances
	 */
	public static void addNbCompsInst() {
		Metrics.nbCompsInst++;
	}
	/**
	 * Get the amount of reconstructed Dedal component roles
	 * @return {@link #nbCompsRoles}
	 */
	public static double getNbCompsRoles() {
		return nbCompsRoles;
	}
	/**
	 * Increments the amount of reconstructed Dedal component roles
	 */
	public static void addNbCompsRoles() {
		Metrics.nbCompsRoles++;
	}
	/**
	 * Get the amount of empty Spring XML descriptors (no architecture-like information)
	 * @return {@link #nbEmptySpringXML}
	 */
	public static double getNbEmptySpringXML() {
		return nbEmptySpringXML;
	}
	/**
	 * Increments the amount of empty Spring XML descriptors (no architecture-like information)
	 */
	public static void addNbEmptySpringXML() {
		Metrics.nbEmptySpringXML++;
	}
	/**
	 * Get the amount of reconstructed Dedal Specifications which are the same as their corresponding Reconstructed Dedal Configurations
	 * @return {@link #nbSpecsEqualsConf}
	 */
	public static double getNbSpecsEqualsConf() {
		return nbSpecsEqualsConf;
	}
	/**
	 * Increments the amount of reconstructed Dedal Specifications which are the same as their corresponding Reconstructed Dedal Configurations
	 */
	public static void addNbSpecsEqualsConf() {
		Metrics.nbSpecsEqualsConf++;
	}
	/**
	 * Get the amount of reconstructed Dedal Interfaces
	 * @return {@link #nbInterfaces}
	 */
	public static double getNbInterfaces() {
		return nbInterfaces;
	}
	/**
	 * Increments the amount of reconstructed Dedal Interfaces
	 */
	public static void addNbInterfaces() {
		Metrics.nbInterfaces++;
	}
	/**
	 * Get the amount of reconstructed connections between components
	 * @return {@link #nbConnections}
	 */
	public static double getNbConnexions() {
		return nbConnections;
	}
	/**
	 * Increments the amount of reconstructed connections between components
	 */
	public static void addNbConnexions() {
		Metrics.nbConnections++;
	}
	/**
	 * Get the amount of reconstructed Dedal architecture description without connections
	 * @return {@link #nbConnectionlessArchis}
	 */
	public static double getNbConnexionlessArchis() {
		return nbConnectionlessArchis;
	}
	/**
	 * Increments the amount of reconstructed Dedal architecture description without connections
	 */
	public static void addNbConnexionlessArchis() {
		Metrics.nbConnectionlessArchis++;
	}
	/**
	 * Get the amount of reconstructed Dedal component classes realizing several component roles
	 * @return {@link #nbCompClassMultiRoles}
	 */
	public static double getNbCompClassMultiRoles() {
		return nbCompClassMultiRoles;
	}
	/**
	 * Increments the amount of reconstructed Dedal component classes realizing several component roles
	 */
	public static void addNbCompClassMultiRoles() {
		Metrics.nbCompClassMultiRoles++;
	}
	/**
	 * Get the amount of reconstructed Dedal Interfaces that could have been replaced by more abstract ones
	 * @return {@link #nbAbstractedInterfaceType}
	 */
	public static double getNbAbstractedInterfaceType() {
		return nbAbstractedInterfaceType;
	}
	/**
	 * Increments the amount of reconstructed Dedal Interfaces that could have been replaced by more abstract ones
	 */
	public static void addNbAbstractType() {
		Metrics.nbAbstractedInterfaceType++;
	}
	/**
	 * Get the amount of Java classes that could not be loaded because of various issues
	 * @return {@link #nbFailedClass}
	 */
	public static double getNbFailedClass() {
		return nbFailedClass;
	}
	/**
	 * Increments the amount of Java classes that could not be loaded because of various issues
	 */
	public static void addNbFailedClass() {
		Metrics.nbFailedClass++;
	}
	
}
