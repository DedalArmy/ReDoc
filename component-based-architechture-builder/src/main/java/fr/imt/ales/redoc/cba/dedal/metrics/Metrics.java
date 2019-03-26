package fr.imt.ales.redoc.cba.dedal.metrics;

/**
 * Class containing static attributes and methods for applying metrics on reconstruction
 * @author Alexandre Le Borgne
 *
 */
public class Metrics {

	/**
	 * Analyzed Spring XML descriptors
	 */
	private static double nbXMLFiles = 0;
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
	 * Analyzed Java classes
	 */
	private static double nbClasses = 0;
	private static double nbSourceCodeClasses = 0;
	private static double nbOutterClasses = 0;
	/**
	 * Reconstructed Dedal component classes
	 */
	private static double nbCompClasses = 0;
	/**
	 * Reconstructed Dedal component instances
	 */
	private static double nbCompInst = 0;
	/**
	 * Reconstructed Dedal component roles
	 */
	private static double nbCompRoles = 0;
	/**
	 * Reconstructed Dedal component classes realizing several component roles
	 */
	private static double nbCompClassMultiRoles= 0;
	/**
	 * Reconstructed Dedal Specifications which are
	 * the same as their corresponding Reconstructed
	 * Dedal Configurations
	 */
	private static double nbDifferentSpecs = 0;
	/**
	 * Reconstructed connections between components
	 */
	private static double nbConnections = 0;
	
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
	 * @return {@link #nbXMLFiles}
	 */
	public static double getNbSpringXML() {
		return nbXMLFiles;
	}
	/**
	 * Increments the amount of analyzed XML-based descriptors
	 */
	public static void addNbSpringXML() {
		Metrics.nbXMLFiles++;
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
	 * @return {@link #nbCompClasses}
	 */
	public static double getNbCompsClasses() {
		return nbCompClasses;
	}
	/**
	 * Increments the amount of reconstructed Dedal component classes
	 */
	public static void addNbCompClasses() {
		Metrics.nbCompClasses++;
	}
	/**
	 * Get the amount of reconstructed Dedal component instances
	 * @return {@link #nbCompInst}
	 */
	public static double getNbCompsInst() {
		return nbCompInst;
	}
	/**
	 * Increments the amount of reconstructed Dedal component instances
	 */
	public static void addNbCompInst() {
		Metrics.nbCompInst++;
	}
	/**
	 * Get the amount of reconstructed Dedal component roles
	 * @return {@link #nbCompRoles}
	 */
	public static double getNbCompsRoles() {
		return nbCompRoles;
	}
	/**
	 * Increments the amount of reconstructed Dedal component roles
	 */
	public static void addNbCompRoles() {
		Metrics.nbCompRoles++;
	}
	/**
	 * Get the amount of reconstructed Dedal Specifications which are the same as their corresponding Reconstructed Dedal Configurations
	 * @return {@link #nbDifferentSpecs}
	 */
	public static double getNbSpecsEqualsConf() {
		return nbDifferentSpecs;
	}
	/**
	 * Increments the amount of reconstructed Dedal Specifications which are the same as their corresponding Reconstructed Dedal Configurations
	 */
	public static void addNbDiffSpecs() {
		Metrics.nbDifferentSpecs++;
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
	
	public static void initMetrics() {
		Metrics.nbAssembs = 0;
		Metrics.nbClasses = 0;
		Metrics.nbCompClassMultiRoles = 0;
		Metrics.nbCompClasses = 0;
		Metrics.nbCompInst = 0;
		Metrics.nbCompRoles = 0;
		Metrics.nbConfs = 0;
		Metrics.nbConnections = 0;
		Metrics.nbDifferentSpecs = 0;
		Metrics.nbOutterClasses = 0;
		Metrics.nbSourceCodeClasses = 0;
		Metrics.nbSpecs = 0;
		Metrics.nbXMLFiles = 0;
	}

	public static double getNbSourceCodeClasses() {
		return nbSourceCodeClasses;
	}

	public static double getNbOutterClasses() {
		return nbOutterClasses;
	}
	
	public static void addNbSourceCodeClasses() {
		Metrics.nbSourceCodeClasses++;
	}
	
	public static void addNbOutterClasses() {
		Metrics.nbOutterClasses++;
	}
	
	
}
