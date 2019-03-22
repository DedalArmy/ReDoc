package fr.imt.ales.redoc.cba.dedal.builder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dedal.ArchitectureDescription;
import dedal.Assembly;
import dedal.ClassConnection;
import dedal.CompInstance;
import dedal.CompRole;
import dedal.Configuration;
import dedal.DedalDiagram;
import dedal.DedalFactory;
import dedal.InstConnection;
import dedal.Interaction;
import dedal.Interface;
import dedal.Repository;
import dedal.RoleConnection;
import dedal.Specification;
import dedal.impl.DedalFactoryImpl;
import fr.imt.ales.redoc.cba.dedal.structure.DedalArchitecture;
import fr.imt.ales.redoc.cba.dedal.structure.DedalComponentClass;
import fr.imt.ales.redoc.cba.dedal.structure.DedalComponentInstance;
import fr.imt.ales.redoc.cba.dedal.structure.DedalComponentRole;
import fr.imt.ales.redoc.cba.dedal.structure.DedalInterfaceType;
import fr.imt.ales.redoc.cba.dedal.transformation.SdslTransformer;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilder;
import fr.imt.ales.redoc.type.hierarchy.build.HierarchyBuilderManager;
import fr.imt.ales.redoc.xml.spring.structure.XMLFile;

/**
 * This class is designed for inspecting jar/war files and generate the Dedal
 * diagram
 * 
 * @author Alexandre Le Borgne
 */
public class DedalArchitectureBuilder {

	/**
	 * {@link Logger}
	 */
	static final Logger logger = LogManager.getLogger(DedalArchitectureBuilder.class);
	private DedalFactory factory;
	private HierarchyBuilder hierarchyBuilder;
	private DedalArchitecture dedalArchitecture;
	private String projectPath;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// Constructor, init, accessors	////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 

	public DedalArchitectureBuilder(String projectPath) throws IOException {
		this.projectPath = projectPath;
		this.hierarchyBuilder = HierarchyBuilderManager.getInstance().getHierarchyBuilder(projectPath);
		this.factory = DedalFactoryImpl.init();
	}

	public DedalDiagram build(XMLFile springXMLFile) throws URISyntaxException, IOException {

		// Setting the DedalDiagram up
		DedalDiagram dedalDiagram = (DedalDiagram) SdslTransformer.extractDedalArtifacts(springXMLFile).get(0); // extracting
																												// from
																												// Spring
		dedalDiagram.setName(springXMLFile.getParentFile().getParentFile().getName() + "_" + springXMLFile.getName() + "_genDedalDiag");

		// Setting the Repository up
		Repository repo;
		if (dedalDiagram.getRepositories().isEmpty()) {
			repo = new DedalFactoryImpl().createRepository();
			repo.setName("genRepo");
			dedalDiagram.getRepositories().add(repo);
		} else
			repo = dedalDiagram.getRepositories().get(0);

		// Setting Dedal architecture levels
		Assembly asm = null;
		Configuration config = null;
		Specification spec = factory.createSpecification();
		spec.setName("Specification");
		for (ArchitectureDescription ad : dedalDiagram.getArchitectureDescriptions()) {
			if (ad instanceof Configuration) {
				config = (Configuration) ad;
				config.setName("Configuration");
			} else if (ad instanceof Assembly) {
				asm = (Assembly) ad;
				asm.setName("Assembly");
			}
		}
		if (asm != null && config != null) {
			dedalDiagram.getArchitectureDescriptions().add(spec);
			asm.setInstantiates(config);
			config.getImplements().add(spec);

			if (this.dedalArchitecture == null)
				this.dedalArchitecture = new DedalArchitecture(this.projectPath, dedalDiagram);

			buildFromSpring(asm, config, spec, repo);
		}

		return dedalDiagram;
	}

	/**
	 * @param dedalDiagram
	 * @param repo
	 * @param spec
	 * @param config
	 * @param asm
	 * @throws IOException
	 */
	private void buildFromSpring(Assembly asm, Configuration config, Specification spec, Repository repo)
			throws IOException {
		for (CompInstance ci : asm.getAssmComponents()) {
			DedalComponentInstance compInstance = this.dedalArchitecture.createCompInstIfNotExists(ci, this.factory,
					asm.getAssemblyConnections()); // complete the description of the component instances and instance connections
			this.dedalArchitecture.createCompClassIfNotExists(ci.getInstantiates(), this.factory, compInstance); // complete the description of component classes
		}
		for (ClassConnection ccon : config.getConfigConnections()) { 
			for (InstConnection acon : asm.getAssemblyConnections()) {
				String property = ccon.getProperty().replaceAll("\"", "");
				String substring = acon.getProperty().substring(acon.getProperty().lastIndexOf('.') + 1);
				if (property.equals(substring)) {
					this.dedalArchitecture.setConfigConnectionFromAsmConnection(ccon, acon); // complete the description of class connections
					break;
				}
			}
		}
		for(DedalComponentClass dcc : this.dedalArchitecture.getConfiguration()) {
			List<DedalComponentRole> componentRoles = dcc.computeComponentRoles(config.getConfigConnections());
			for(DedalComponentRole cr : componentRoles) {
				dcc.getComponentClass().getRealizes().add(cr.getComponentRole());
				spec.getSpecComponents().add(cr.getComponentRole());
			}
			this.dedalArchitecture.getSpecification().addAll(componentRoles);
		}
		for(ClassConnection ccon : config.getConfigConnections()) {
			RoleConnection specConn = this.factory.createRoleConnection();
			CompRole clientRole = ccon.getClientClassElem().getRealizes().get(0);
			Interface clientInter = this.findInterface(ccon.getClientIntElem(), clientRole);
			CompRole serverRole = ccon.getServerClassElem().getRealizes().get(0);
			Interface serverInter = this.findInterface(ccon.getServerIntElem(), serverRole);
			specConn.setClientCompElem(clientRole);	
			specConn.setClientIntElem(clientInter);
			specConn.setServerCompElem(serverRole);
			specConn.setServerIntElem(serverInter);
			spec.getSpecConnections().add(specConn);
		}
		System.out.println();
		for (DedalInterfaceType interType : this.dedalArchitecture.getInterfaceTypes()) {
			repo.getInterfaceTypes().add(interType.getInterfaceType());
		}
	}

	private Interface findInterface(Interaction inter, CompRole role) {
		for(Interaction temp : role.getCompInterfaces()) {
			if(temp instanceof Interface && ((Interface)temp).getType().equals(((Interface)inter).getType())) {
				return (Interface) temp;
			}
		}
		return null;
	}
}