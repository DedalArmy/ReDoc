package fr.imt.ales.redoc.cba.dedal.transformation;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import fr.imt.ales.redoc.xml.spring.structure.XMLFile;

/**
 * This class uses SpringDSL language for parsing Spring XML files and then it uses a transformation written in 
 * QVT Operational for transforming it into a Dedal Architecture
 * @author Alexandre Le Borgne
 *
 */
public class SdslTransformer {
	
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
	 * @param deploymentDescriptor is the XML file containing the Spring deployment description
	 * @return The List<EObject> containing Dedal features if the extraction finished normally, and an <b>empty</b> List<EObject> otherwise
	 * @throws URISyntaxException if this URL is not formatted strictly according toto RFC2396 and cannot be converted to a URI.
	 */
	public static List<EObject> extractDedalArtifacts(XMLFile deploymentDescriptor) throws URISyntaxException {
		TransformationExecutor executor;
		ClassLoader cl = SdslTransformer.class.getClassLoader();
//		URL resource = cl.getResource("springToDedal.qvto");
		URL resource = cl.getResource("transforms/springToDedal.qvto");
//		URL resource = cl.getResource("classpath:transforms/springToDedal.qvto");
		logger.debug("RESOURCE : " + resource);
		java.net.URI uri = resource.toURI();
		logger.debug("URI : " + uri);
//		String path = uri.getRawPath();
		String path = resource.toExternalForm().replaceAll("jar:file:",	"");
		logger.debug("PATH : " + path);
		URI uuri = URI.createFileURI(path);
		executor = new TransformationExecutor(uuri);

		Injector injector = new SpringConfigDslStandaloneSetup().createInjectorAndDoEMFRegistration();
		ResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		Resource inResource;
		inResource = resourceSet.getResource(org.eclipse.emf.common.util.URI.createFileURI(deploymentDescriptor.getAbsolutePath()),true);
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


