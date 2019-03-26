package fr.imt.ales.redoc.cba.dedal.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

import dedal.Assembly;
import dedal.ClassConnection;
import dedal.CompClass;
import dedal.CompInstance;
import dedal.CompRole;
import dedal.Component;
import dedal.Configuration;
import dedal.DIRECTION;
import dedal.DedalFactory;
import dedal.InstConnection;
import dedal.Interaction;
import dedal.Interface;
import dedal.RoleConnection;
import dedal.Specification;
import fr.imt.ales.redoc.cba.dedal.builder.AbstractionOption;
import fr.imt.ales.redoc.cba.dedal.extractor.ComponentRoleExtractor;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaField;
import fr.imt.ales.redoc.type.hierarchy.structure.JavaType;

public class DedalComponentRole extends DedalComponentType {

	CompRole componentRole;
	List<DedalComponentRole> candidateComponentRoles;
	List<DedalComponentRole> substitutableComponentRoles = new ArrayList<>();
	private List<DedalInterfaceType> requiredInterfaces = new ArrayList<>();
	private List<DedalInterfaceType> providedInterfaces = new ArrayList<>();

	public DedalComponentRole(String projectPath, Component component, DedalFactory dedalFactory, 
			DedalArchitecture architecture, Component sourceComponent, 
			List<ClassConnection> configConnections, CompClass initialComponentClass) throws IOException {
		super(projectPath, component, dedalFactory, architecture);
		this.architecture.getSpecification().add(this);
		this.candidateComponentRoles = this.computeCandidateComponentRoles(configConnections, initialComponentClass);
		this.mapInterfaces(sourceComponent, configConnections, initialComponentClass);
	}

	private void mapInterfaces(Component sourceComponent, List<ClassConnection> configConnections, CompClass initialComponentClass) throws IOException {
		if(sourceComponent.equals(initialComponentClass)) { // this is the first role that is extracted which corresponds to the bigger role
			DedalComponentClass dcc = this.architecture.getConfigComponent((CompClass)sourceComponent);
			for(DedalInterface inter : dcc.getInterfaces()) {
				DedalInterface dedalInterface = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), 
						inter.getInterfaceType().getjType(), this.architecture);
				dedalInterface.getCompInterface().setDirection(inter.getCompInterface().getDirection());
				if(dedalInterface.getCompInterface().getDirection().equals(DIRECTION.PROVIDED)) {
					String name = this.componentRole.getName()+".prov"+dedalInterface.getCompInterface().getType().getName();
					dedalInterface.getCompInterface().setName(name);
				}
				else if(dedalInterface.getCompInterface().getDirection().equals(DIRECTION.REQUIRED)) {
					String name = this.componentRole.getName() 
							+ inter.getCompInterface().getName().substring(inter.getCompInterface().getName().lastIndexOf('.'));
					dedalInterface.getCompInterface().setName(name);
				}
				this.interfaces.add(dedalInterface);
				this.componentRole.getCompInterfaces().add(dedalInterface.getCompInterface());
			}
		} else { // the case of a candidate component role
			this.interfaces.add(new DedalInterface(this.getProjectPath(), this.getDedalFactory(), this.getjType(), this.architecture));
			this.mapRequiredInterfaces(configConnections, initialComponentClass);
			for(DedalInterface inter : this.interfaces) {
				this.componentRole.getCompInterfaces().add(inter.getCompInterface());
				if(inter.getCompInterface().getDirection().equals(DIRECTION.PROVIDED)) {
					String name = this.componentRole.getName()+".prov"+inter.getCompInterface().getType().getName();
					inter.getCompInterface().setName(name);
				}
			}
		}
	}

	private void mapRequiredInterfaces(List<ClassConnection> configConnections, CompClass initialComponentClass) throws IOException {
		List<ClassConnection> clientConnections = new ArrayList<>(); // when initialComponentClass is the client in a connection
		for(ClassConnection con : configConnections) {
			if(con.getClientClassElem().equals(initialComponentClass)) {
				clientConnections.add(con);
			}
		}
		for(ClassConnection ccon : clientConnections) {
			String name = ccon.getProperty().substring(ccon.getProperty().lastIndexOf('.') + 1);
			String interName = this.componentRole.getName()+"."+name;
			JavaField jField = this.getjType().getRequiredType(name);
			if(jField != null && initialComponentClass.equals(ccon.getClientClassElem())) {
				JavaType jt = this.hierarchyBuilder.findJavaType(jField.getType());
				DedalInterface inter = new DedalInterface(this.getProjectPath(), this.getDedalFactory(), jt, this.architecture);
				inter.getCompInterface().setDirection(DIRECTION.REQUIRED);
				inter.getCompInterface().setName(interName);
				this.interfaces.add(inter);
				ccon.setClientIntElem(inter.getCompInterface());
			} else if(initialComponentClass.equals(ccon.getServerClassElem())) {
				ccon.setServerIntElem(this.interfaces.get(0).getCompInterface()); // because the first interface is the global provided one
			}
		}
	}

	private List<DedalComponentRole> computeCandidateComponentRoles(List<ClassConnection> configConnections, 
			CompClass initialComponentClass) throws IOException {
		List<DedalComponentRole> result = new ArrayList<>();
		List<JavaType> jTypes = this.getSuperTypes(this.getjType());
		for(JavaType jt : jTypes) {
			if(jt!=null && !jt.equals(this.getjType())) {
				DedalComponentRole dcr = this.architecture.getComponentRoleByJavaType(jt);
				if(dcr != null) {
					result.add(dcr);
				}
				else {
					ComponentRoleExtractor cre = new ComponentRoleExtractor(jt, this.getDedalFactory());
					CompRole component = cre.mapComponentRole();
					result.add(new DedalComponentRole(this.getProjectPath(), component, this.getDedalFactory(), 
							this.architecture, this.componentRole, configConnections, initialComponentClass));
				}
			}
		}
		return result;
	}

	private List<JavaType> getSuperTypes(JavaType jType) {
		List<JavaType> result = new ArrayList<>();
		if(!jType.getjExtends().isEmpty()) {
			result.addAll(jType.getjExtends());
		}
		if(!jType.getjImplements().isEmpty()) {
			result.addAll(jType.getjImplements());
		}
		return result;
	}

	@Override
	protected void initJType(Component component) {
		if(component instanceof CompRole) {
			this.componentRole = (CompRole) component;
			this.setjType(this.hierarchyBuilder.findJavaType(this.componentRole.getName().substring(0, this.componentRole.getName().lastIndexOf('_')).replaceAll("\"", "")));
		}
	}

	public CompRole getComponentRole() {
		return this.componentRole;
	}

	@Override
	public String toString() {
		return this.componentRole.getName();
	}

	public void refineRole(Assembly asm, Configuration config, Specification spec, AbstractionOption abstractOption) {
		this.defineSubstitutable(spec.getSpecConnections());
		switch(abstractOption) {
		case ALLABSTRACT:
			this.refineAllAbstract(asm, config, spec);
			break;
		case MIXED:
			this.refineMixed(asm, config, spec);
			break;
		case ALLCONCRETE:
			this.refineAllConcrete(asm, config, spec);
			break;
		default: 
			throw new UnsupportedOperationException();
		}
	}

	private void defineSubstitutable(EList<RoleConnection> specConnections) {
//		if(!this.candidateComponentRoles.isEmpty()) {
			List<DedalInterfaceType> minProvidedInterfaces = new ArrayList<>();
			for(RoleConnection conn : specConnections) {
				if(conn.getServerCompElem().equals(this.componentRole)) {
					minProvidedInterfaces.add(this.architecture.findInterfaceType(((Interface)conn.getClientIntElem()).getType())); //this is the smallest interface to remain substitutable
					this.providedInterfaces.add(this.architecture.findInterfaceType(((Interface)conn.getServerIntElem()).getType()));
				}
			}
			for(DedalInterface inter : this.interfaces) {
				if(inter.getCompInterface().getDirection().equals(DIRECTION.REQUIRED)) {
					requiredInterfaces .add(inter.getInterfaceType());
				}
			}
			for(DedalComponentRole candidate : this.candidateComponentRoles) {
				candidate.defineSubstitutable(this.requiredInterfaces, this.providedInterfaces, minProvidedInterfaces);
			}
			if(this.isSubstitutable()) {
				this.substitutableComponentRoles.addAll(this.candidateComponentRoles);
				this.refineSubstitutable();
			}
//		}
	}

	private void refineSubstitutable() {
		List<DedalComponentRole> toRemove = new ArrayList<>();
		for(DedalComponentRole scr : this.substitutableComponentRoles) {
			if(scr.providedInterfaces.isEmpty() && scr.requiredInterfaces.isEmpty())
				toRemove.add(scr);
		}
		this.substitutableComponentRoles.removeAll(toRemove);
	}

	private Boolean isSubstitutable() {
		List<DedalInterfaceType> provInterfaceTypes = new ArrayList<>();
		List<DedalInterfaceType> reqInterfaceTypes = new ArrayList<>();
		for(DedalComponentRole candidate : this.candidateComponentRoles) {
			provInterfaceTypes.addAll(candidate.providedInterfaces);
			reqInterfaceTypes.addAll(candidate.requiredInterfaces);
		}
		if(!reqInterfaceTypes.containsAll(this.requiredInterfaces)) {
			return Boolean.FALSE;
		}
		if(provInterfaceTypes.containsAll(this.providedInterfaces)) {
			return Boolean.TRUE;
		} else {
			for(DedalInterfaceType inter : this.providedInterfaces) {
				if(!existsIn(inter, provInterfaceTypes)) {
					return Boolean.FALSE;
				}
			}
		}
		return Boolean.TRUE;
	}

	private Boolean existsIn(DedalInterfaceType inter, List<DedalInterfaceType> provInterfaceTypes) {
		for(DedalInterfaceType inter2 : provInterfaceTypes) {
			if(inter.equals(inter2)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	private void defineSubstitutable(List<DedalInterfaceType> requiredInterfaces2,
			List<DedalInterfaceType> providedInterfaces2, List<DedalInterfaceType> minProvidedInterfaces) {
		for(DedalInterface inter : this.interfaces) {
			switch(inter.getCompInterface().getDirection()) {
			case PROVIDED:
				if(providedInterfaces2.size() == minProvidedInterfaces.size()) {
					for(int i = 0; i < providedInterfaces2.size(); i++) {
						JavaType interJType = inter.getInterfaceType().getjType();
						if(interJType.isSubtypeOf(minProvidedInterfaces.get(i).getjType())
								&& providedInterfaces2.get(i).getjType().isSubtypeOf(interJType)) {
							this.providedInterfaces.add(inter.getInterfaceType());
						}
					}
				}
				break;
			case REQUIRED:
				if(requiredInterfaces2.contains(inter.getInterfaceType())) {
					this.requiredInterfaces.add(inter.getInterfaceType());
				}
				break;
			}
		}
		for(DedalComponentRole candidate : this.candidateComponentRoles) {
			candidate.defineSubstitutable(this.requiredInterfaces, this.providedInterfaces, minProvidedInterfaces);
		}
	}

	private void refineAllConcrete(Assembly asm, Configuration config, Specification spec) {
		this.substitute(config, spec);
		if(this.getjType().isAbstractType()) {
			this.getComponentRole().setName(/*this.findAsmNames(asm) +" : " + */this.getjType().getSimpleName());
		} 
//		else {
//			this.getComponentRole().setName(/*this.findAsmNames(asm) +" : " + */this.getComponentRole().getName());
//		}
		for(DedalComponentRole candidate : this.substitutableComponentRoles) {
			candidate.refineAllConcrete(asm, config, spec);
		}
	}

	private void refineMixed(Assembly asm, Configuration config, Specification spec) {
		this.substitute(config, spec);
		if(this.getjType().isAbstractType()) {
			this.getComponentRole().setName(/*this.findAsmNames(asm) +" : " + */this.getjType().getSimpleName());
		} 
//		else {
//			this.getComponentRole().setName(this.findAsmNames(asm) +" : " + this.getComponentRole().getName());
//		}
		this.removeUnusedInterfaces(spec);
		for(DedalComponentRole candidate : this.substitutableComponentRoles) {
			candidate.refineMixed(asm, config, spec);
		}
	}

	private void removeUnusedInterfaces(Specification spec) {
		List<Interaction> toRemove = new ArrayList<>();
		List<Interaction> toKeep = new ArrayList<>();
		for(Interaction inter : this.componentRole.getCompInterfaces()) {
			if(((Interface)inter).getDirection().equals(DIRECTION.PROVIDED)) {
				toRemove.add(inter);
			}
		}
		boolean isInvolved = false;
		for(CompRole role : spec.getSpecComponents()) {
			if(this.componentRole.equals(role)) {
				isInvolved = true;
			}
		}
		for(RoleConnection conn : spec.getSpecConnections()) {
			if(conn.getServerCompElem().equals(this.componentRole)) {
				toKeep.add(conn.getServerIntElem());
			}
		}
		if(isInvolved) { // otherwise it means that this component role is not part of the specification
			toRemove.removeAll(toKeep);
			this.componentRole.getCompInterfaces().removeAll(toRemove);
		}
	}

	private void refineAllAbstract(Assembly asm, Configuration config, Specification spec) {
		this.substitute(config, spec);
//		this.getComponentRole().setName(this.findAsmNames(asm) +" : " + this.getComponentRole().getName());
		this.removeUnusedInterfaces(spec);
		for(DedalComponentRole candidate : this.substitutableComponentRoles) {
			candidate.refineAllAbstract(asm, config, spec);
		}
	}

	private String findAsmNames(Assembly asm) {
		StringBuilder strBuilder = new StringBuilder();
		for(CompInstance ci : asm.getAssmComponents()) {
			if(ci.getInstantiates().getRealizes().contains(this.componentRole)) { // we get to the comp instance that instantiates the class which realizes the current role
				for(InstConnection conn : asm.getAssemblyConnections()) {
					if(conn.getServerInstElem().equals(ci)) { // we found a connection
						DedalInterfaceType intType = this.architecture.findInterfaceType(((Interface)conn.getServerIntElem()).getType());
						System.out.println();
						for(DedalInterfaceType prov : this.providedInterfaces) {
							if(intType.getjType().isSubtypeOf(prov.getjType())) {
								String propName = conn.getProperty().substring(conn.getProperty().lastIndexOf('.')+1);
								if("".contentEquals(strBuilder.toString()))
									strBuilder.append(propName);
								else {
									if(!(strBuilder.toString().equals(propName)
											|| strBuilder.toString().endsWith(","+propName)
											|| strBuilder.toString().startsWith(propName+",")
											|| strBuilder.toString().contains(","+propName+",")))
										strBuilder.append("," + propName);
								}

							}
						}
					}
				}
			}
		}
		return strBuilder.toString();
	}

	private void substitute(Configuration config, Specification spec) {
		if(!this.substitutableComponentRoles.isEmpty()) { // the current component role is gonna be replaced in the architecture
			this.replaceInConnections(spec);
			this.replaceInConfigComponents(config); // For realizes property of component classes
			spec.getSpecComponents().remove(this.getComponentRole());
			for(DedalComponentRole substitutable : this.substitutableComponentRoles) {
				spec.getSpecComponents().add(substitutable.getComponentRole());
			}
		}
	}

	private void replaceInConfigComponents(Configuration config) {
		for(CompClass cc : config.getConfigComponents()) {
			if(cc.getRealizes().contains(this.componentRole)) {
				cc.getRealizes().remove(this.componentRole);
				for(DedalComponentRole candidate : this.substitutableComponentRoles) {
					cc.getRealizes().add(candidate.getComponentRole());
				}
			}
		}
	}

	private void replaceInConnections(Specification spec) {
		List<RoleConnection> connections = new ArrayList<>();
		for(RoleConnection connection : spec.getSpecConnections()) { //list of the connections the current component role is involved in
			if(connection.getServerCompElem().equals(this.componentRole)) {
				connections.add(connection);
			}
		}
		for(RoleConnection connection : connections) {
			DedalInterfaceType inter = this.architecture.findInterfaceType(((Interface)connection.getServerIntElem()).getType());
			for(DedalComponentRole candidate : this.substitutableComponentRoles) {
				for(DedalInterfaceType candInt : candidate.providedInterfaces) {
					if(candInt.equals(inter) || inter.getCandidateInterfaceTypes().contains(candInt)) {
						connection.setServerCompElem(candidate.getComponentRole());
						connection.setServerIntElem(candidate.findInterface(candInt));
					}
				}
			}
		}
	}

	private Interaction findInterface(DedalInterfaceType inter) {
		for(DedalInterface inteface : this.interfaces) {
			if(inteface.interfaceType.equals(inter))
				return inteface.getCompInterface();
		}
		return null;
	}



}
