package fr.imt.ales.redoc.cba.dedal.transformation;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.m2m.qvt.oml.BasicModelExtent;
import org.eclipse.m2m.qvt.oml.ExecutionContextImpl;
import org.eclipse.m2m.qvt.oml.ExecutionDiagnostic;
import org.eclipse.m2m.qvt.oml.ModelExtent;
import org.eclipse.m2m.qvt.oml.TransformationExecutor;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.xtext.spring.SpringConfigDslStandaloneSetup;

import com.google.inject.Injector;

import dedal.DedalPackage;

/**
 * This class uses SpringDSL language for parsing Spring XML files and then it uses a transformation written in 
 * QVT Operational for transforming it into a Dedal Architecture
 * @author Alexandre Le Borgne
 *
 */
public class SdslTransformer {

	/**
	 * URI to the Spring to Dedal transformation file
	 */
	static final String TRANSFORMATION_URI = "fr.ema.dedal.componentinspector/transforms/springToDedal.qvto";
	static final String TRANSFORMATION_JAR_URI = "fr/ema/dedal/componentinspector/transforms/springToDedal.qvto";
	/**
	 * Logger
	 */
	static final Logger logger = LogManager.getLogger(SdslTransformer.class);

	/**
	 * Default private constructor for avoiding instantiation
	 */
	private SdslTransformer() {}

	/**
	 * Extract artifacts from the parsing of Spring files and then transform them into the Dedal language
	 * @param sdslPath is the String representing the Path to the Spring XML file
	 * @return The List<EObject> containing Dedal features if the extraction finished normally, and an <b>empty</b> List<EObject> otherwise
	 * @throws URISyntaxException if this URL is not formatted strictly according toto RFC2396 and cannot be converted to a URI.
	 */
	public static List<EObject> extractDedalArtifacts(String sdslPath) throws URISyntaxException {
		TransformationExecutor executor;
		ClassLoader cl = SdslTransformer.class.getClassLoader();
		URL resource = cl.getResource("springToDedal.qvto");
		System.out.println("RESOURCE : " + resource);
		java.net.URI uri = resource.toURI();
		System.out.println("URI : " + uri);
		String path = uri.getRawPath();
		System.out.println("PATH : " + path);
		executor = new TransformationExecutor(URI.createFileURI(path));

		Injector injector = new SpringConfigDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		ResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		Resource inResource;
		inResource = resourceSet.getResource(org.eclipse.emf.common.util.URI.createFileURI(sdslPath),true);
		EList<EObject> inObjects = inResource.getContents();

		/**
		 * create the input extent with its initial contents
		 */
		ModelExtent input = new BasicModelExtent(inObjects);		
		/**
		 * create an empty extent to catch the output
		 */
		ModelExtent output = new BasicModelExtent();

		EPackage.Registry.INSTANCE.put(DedalPackage.eNS_URI,
				DedalPackage.eINSTANCE);

		/**
		 * setup the execution environment details -> 
		 * configuration properties, logger, monitor object etc.
		 */
		ExecutionContextImpl context = new ExecutionContextImpl();
		context.setConfigProperty("keepModeling", true);

		/**
		 * run the transformation assigned to the executor with the given 
		 * input and output and execution context
		 */
		ExecutionDiagnostic executorResult = executor.execute(context, input, output);

		/**
		 * check the result for success
		 */
		if(executorResult.getSeverity() == Diagnostic.OK) {
			return output.getContents();
		} else {
			/**
			 * turn the result diagnostic into status and send it to error log			
			 */
			IStatus status = BasicDiagnostic.toIStatus(executorResult);
			logger.error(status.getMessage());
			return Collections.emptyList();
		}
	}

}


