package fr.imt.ales.redoc.cba.dedal.writer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import dedal.ArchitectureDescription;
import dedal.DedalDiagram;
import fr.ema.dedal.xtext.DedalADLStandaloneSetup;
import fr.imt.ales.redoc.cba.dedal.main.Main;
import fr.imt.ales.redoc.cba.dedal.metrics.Metrics;

public class DedalDiagramWriter {

	/**
		 * @param dedalDiagram
		 */
		public static void saveDiagram(DedalDiagram dedalDiagram, String projectPath) {
//			projectPath = ".";
			Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
	
			Injector injector = new DedalADLStandaloneSetup().createInjectorAndDoEMFRegistration();
			ResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
			resourceSet.setResourceFactoryRegistry(reg);
			
	
			List<ArchitectureDescription> toRemoveFromDiagram = new ArrayList<>();
			dedalDiagram.getArchitectureDescriptions().removeAll(toRemoveFromDiagram);
			
			URI uri = URI.createFileURI(projectPath + "/generated/"
					+ dedalDiagram.getName().substring(dedalDiagram.getName().indexOf('_')+1) + ".dedaladl");
			new java.io.File(uri.path()).getParentFile().mkdirs();
			Resource resource = resourceSet.createResource(uri);
			
			
	
			URI uri2 = URI.createFileURI(/*"file:///" + */projectPath + "/generated/"
					+ dedalDiagram.getName().substring(dedalDiagram.getName().indexOf('_')+1) + ".dedal");
			new java.io.File(uri2.path()).getParentFile().mkdirs();
			Resource resource2 = new XMIResourceImpl(uri2);
	
			// Get the first model element and cast it to the right type, in my
			// example everything is hierarchical included in this first node
			resource.getContents().add(dedalDiagram);
			resource2.getContents().add(EcoreUtil.copy(dedalDiagram));
	
			// now save the content.
			try {
				/*
				 * We need to save resource2 before resource because in case of errors in 
				 * model, resource throws an exception since it verifies the correctness
				 * of the DedalADL syntax.
				 */
				Map<Object,Object> options = new HashMap<>();
				options.put(XtextResource.OPTION_ENCODING, Main.UTF_8);
				options.put(XtextResource.OPTION_SAVE_ONLY_IF_CHANGED, Boolean.TRUE);
				Map<Object,Object> options2 = new HashMap<>();
				options.put(XtextResource.OPTION_ENCODING, Main.UTF_8);
				options2.put(XtextResource.OPTION_SAVE_ONLY_IF_CHANGED, Boolean.TRUE);
				resource2.save(options2);
				DedalDiagramWriter.saveDedalAdlFile(dedalDiagram, resource, options, options2);
			} catch (IOException e) {
				Main.logger.error(e.getMessage(), e);
			}
		}

	/**
	 * @param dedalDiagram
	 * @param resource
	 * @param options
	 * @param options2
	 * @throws IOException
	 */
	public static void saveDedalAdlFile(DedalDiagram dedalDiagram, Resource resource, Map<Object, Object> options,
			Map<Object, Object> options2) throws IOException {
		try {
			resource.save(options);
		} catch (Exception e) {
			Main.logger.error("could not generate " + dedalDiagram.getName() +".dedaladl", e);
		}
	}

	/**
	 * @param reconstructedArchitectures
	 */
	public static void saveArchitectures(List<DedalDiagram> reconstructedArchitectures, String projectPath) {
		if(!reconstructedArchitectures.isEmpty())
		{
			for(DedalDiagram dd : reconstructedArchitectures)
			{
				DedalDiagramWriter.saveDiagram(dd, projectPath);
			}
		}
		else
		{
			Main.logger.error("No architecture were reconstructed.");
			JOptionPane.showMessageDialog(null, "Ooops! Something wrong happened during the reconstruction...");
		}
	}

	public static void exportMetrics(String out) throws FileNotFoundException, UnsupportedEncodingException {
		new java.io.File(out).getParentFile().mkdirs();
		PrintWriter writer = new PrintWriter(out, Main.UTF_8);
		writer.print(Metrics.print());
		writer.close();
	}

}
