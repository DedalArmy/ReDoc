/*******************************************************************************
 * Copyright (C) 2019 IMT Mines Alès
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package fr.imt.ales.redoc.cba.dedal.builder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dedal.ArchitectureDescription;
import dedal.Assembly;
import dedal.ClassConnection;
import dedal.CompClass;
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
	private DedalArchitecture dedalArchitecture;
	private String projectPath;

	private InterfaceOption interfaceOption = InterfaceOption.BIGINTERFACES;
	private AbstractionOption abstractOption = AbstractionOption.ALLCONCRETE;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// Constructor, init, accessors	////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 

	public DedalArchitectureBuilder(String projectPath) throws IOException {
		this.projectPath = projectPath;
		this.factory = DedalFactoryImpl.init();
	}

	public DedalArchitectureBuilder(String projectPath, InterfaceOption intOpt, AbstractionOption absOpt) throws IOException {
		this(projectPath);
		this.interfaceOption = intOpt;
		this.abstractOption = absOpt;
	}

	public DedalArchitectureBuilder(String projectPath, InterfaceOption intOpt) throws IOException {
		this(projectPath);
		this.interfaceOption = intOpt;
	}

	public DedalArchitectureBuilder(String projectPath, AbstractionOption absOpt) throws IOException {
		this(projectPath);
		this.abstractOption = absOpt;
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
		
		asm.getAssmComponents().forEach(ac -> {
			ac.getCompInterfaces().forEach(aci -> {
				aci.setName(aci.getName().replaceAll("\\.", "_"));
			});
		});
		
		config.getConfigComponents().forEach(cc -> {
			cc.getCompInterfaces().forEach(cci -> {
				cci.setName(cci.getName().replaceAll("\\.", "_"));
			});
		});
		
		spec.getSpecComponents().forEach(cr -> {
			cr.getCompInterfaces().forEach(cri -> {
				cri.setName(cri.getName().replaceAll("\\.", "_"));
			});
		});
		
		// dealing with automatic Ids
		dedalDiagram.getArchitectureDescriptions().forEach(ad -> {
			if(ad instanceof Assembly) {
				((Assembly) ad).getAssmComponents().forEach(ac -> ac.setId(ac.getName().replaceAll("\"", "") + "_inst"));
				((Assembly) ad).getAssemblyConnections().forEach(ac -> ac.setRefID("con_" + ac.getClientInstElem().getName().replaceAll("\"", "") 
						+ "_" + ac.getServerInstElem().getName().replaceAll("\"", "")));
			}
			else if(ad instanceof Configuration) {
				((Configuration) ad).getConfigComponents().forEach(cc -> cc.setId(cc.getName().replaceAll("\"", "") + "_class"));
				((Configuration) ad).getComptypes().forEach(ct -> ct.setId(ct.getName().replaceAll("\"", "") + "_type"));
				((Configuration) ad).getConfigConnections().forEach(cc -> cc.setRefID("con_" + cc.getClientClassElem().getName().replaceAll("\"", "") 
						+ "_" + cc.getServerClassElem().getName().replaceAll("\"", "")));
			}
			else if (ad instanceof Specification) {
				((Specification) ad).getSpecComponents().forEach(sc -> sc.setId(sc.getName().replaceAll("\"", "") + "_role"));
				((Specification) ad).getSpecConnections().forEach(sc -> sc.setRefID("con_" + sc.getClientCompElem().getName().replaceAll("\"", "") 
						+ "_" + sc.getServerCompElem().getName().replaceAll("\"", "")));
			}
		});
		repo.getInterfaceTypes().forEach(it -> {
			it.getSignatures().forEach(s -> {
				s.setId(s.getName().replaceAll("\"", ""));
				s.getParameters().forEach(p -> p.setId(p.getName().replaceAll("\"", "")));
			});
		});

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
		this.cleanAnonymousClasses(asm, config);
		for (CompInstance ci : asm.getAssmComponents()) {
			this.dedalArchitecture.createCompInstIfNotExists(ci, this.factory, asm.getAssemblyConnections()); // complete the description of the component instances and instance connections
		}
		this.ignoreIncompleteAssemblyConnections(asm);
		this.applyOptionsToAssembly();
		for (DedalComponentInstance compInstance : this.dedalArchitecture.getAssembly()) {
			this.dedalArchitecture.createCompClassIfNotExists(compInstance.getComponentInstance().getInstantiates(), this.factory, compInstance); // complete the description of component classes
		}
		List<ClassConnection> toRemove = new ArrayList<>();
		for (ClassConnection ccon : config.getConfigConnections()) { 
			if(ccon.getProperty()!=null) {
				for (InstConnection acon : asm.getAssemblyConnections()) {
					String property = ccon.getProperty().replaceAll("\"", "");
					String substring = acon.getProperty().substring(acon.getProperty().lastIndexOf('.') + 1);
					if (property.equals(substring)) {
						this.dedalArchitecture.setConfigConnectionFromAsmConnection(ccon, acon); // complete the description of class connections
						break;
					}
				}
			} else {
				toRemove.add(ccon);
			}
		}
		config.getConfigConnections().removeAll(toRemove);
		this.ignoreIncompleteConfigConnections(config);
		this.removeUnnamedCompClasses(config);
		for(DedalComponentClass dcc : this.dedalArchitecture.getConfiguration()) {
			List<DedalComponentRole> componentRoles = dcc.computeComponentRoles(config.getConfigConnections());
			for(DedalComponentRole cr : componentRoles) {
				dcc.getComponentClass().getRealizes().add(cr.getComponentRole());
				spec.getSpecComponents().add(cr.getComponentRole());
			}
		}
		for(ClassConnection ccon : config.getConfigConnections()) {
			RoleConnection specConn = this.factory.createRoleConnection();
			CompRole clientRole = !ccon.getClientClassElem().getRealizes().isEmpty()?ccon.getClientClassElem().getRealizes().get(0):null;
			Interface clientInter = null;
			if(ccon.getClientIntElem()!=null) {
				clientInter = this.findInterface(ccon.getClientIntElem(), clientRole);
				if(!ccon.getServerClassElem().getRealizes().isEmpty()) {
					CompRole serverRole = ccon.getServerClassElem().getRealizes().get(0);
					Interface serverInter = null;
					if(ccon.getServerIntElem()!=null) {
						serverInter = this.findInterface(ccon.getServerIntElem(), serverRole);
						specConn.setClientCompElem(clientRole);	
						specConn.setClientIntElem(clientInter);
						specConn.setServerCompElem(serverRole);
						specConn.setServerIntElem(serverInter);
						spec.getSpecConnections().add(specConn);
					}
				}
			}
		}
		this.ignoreIncompleteSpecConnections(spec);
		this.applyOptionsToSpecification(asm, config, spec);
		for (DedalInterfaceType interType : this.dedalArchitecture.getInterfaceTypes()) {
			repo.getInterfaceTypes().add(interType.getInterfaceType());
		}
	}


	private void removeUnnamedCompClasses(Configuration config) {
		List<CompClass> toRemove = new ArrayList<>();
		for(CompClass cc : config.getConfigComponents()) {
			if(cc.getName() == null) 
				toRemove.add(cc);
		}
		config.getConfigComponents().removeAll(toRemove);
	}

	private void cleanAnonymousClasses(Assembly asm, Configuration config) {
		List<CompInstance> toRemove = new ArrayList<>();
		for(CompInstance ci : asm.getAssmComponents()) {
			if(ci.getInstantiates() != null) {
				if(ci.getInstantiates().getName() == null) {
					toRemove.add(ci);
					config.getConfigComponents().remove(ci.getInstantiates());
				}
			} else {
				toRemove.add(ci);
			}
		}
		asm.getAssmComponents().removeAll(toRemove);
	}

	private void ignoreIncompleteSpecConnections(Specification spec) {
		List<RoleConnection> toRemove = new ArrayList<>();
		for(RoleConnection conn : spec.getSpecConnections()) {
			if(conn.getClientCompElem() == null || conn.getClientIntElem() == null
					|| conn.getServerCompElem() == null || conn.getServerIntElem() == null)
				toRemove.add(conn);
		}
		spec.getSpecConnections().removeAll(toRemove);
	}

	private void ignoreIncompleteConfigConnections(Configuration config) {
		List<ClassConnection> toRemove = new ArrayList<>();
		for(ClassConnection conn : config.getConfigConnections()) {
			if(conn.getClientClassElem() == null || conn.getClientIntElem() == null
					|| conn.getServerClassElem() == null || conn.getServerIntElem() == null)
				toRemove.add(conn);
		}
		config.getConfigConnections().removeAll(toRemove);
	}

	private void ignoreIncompleteAssemblyConnections(Assembly asm) {
		List<InstConnection> toRemove = new ArrayList<>();
		for(InstConnection conn : asm.getAssemblyConnections()) {
			if(conn.getClientInstElem() == null || conn.getClientIntElem() == null
					|| conn.getServerInstElem() == null || conn.getServerIntElem() == null)
				toRemove.add(conn);
		}
		asm.getAssemblyConnections().removeAll(toRemove);
	}

	private void applyOptionsToSpecification(Assembly asm, Configuration config, Specification spec) {
		List<DedalComponentRole> initialRoles = new ArrayList<>();
		initialRoles .addAll(this.dedalArchitecture.getSpecification());
		for(DedalComponentRole comp : initialRoles) {
			comp.refineRole(asm, config, spec, abstractOption);
		}
	}

	private void applyOptionsToAssembly() throws IOException {
		for(DedalComponentInstance comp : this.dedalArchitecture.getAssembly()) {
			comp.refine(this.interfaceOption);
		}
	}

	/**
	 * @return the dedalArchitecture
	 */
	public DedalArchitecture getDedalArchitecture() {
		return dedalArchitecture;
	}

	private Interface findInterface(Interaction inter, CompRole role) {
		if(role != null && role.getCompInterfaces() != null)
			for(Interaction temp : role.getCompInterfaces()) {
				if(temp instanceof Interface && ((Interface)temp).getType().equals(((Interface)inter).getType())) {
					return (Interface) temp;
				}
			}
		return null;
	}
}
