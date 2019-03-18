package fr.imt.ales.redoc.cba.dedal.builder;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dedal.ArchitectureDescription;
import dedal.Assembly;
import dedal.ClassConnection;
import dedal.CompClass;
import dedal.CompInstance;
import dedal.Configuration;
import dedal.DedalDiagram;
import dedal.DedalFactory;
import dedal.InstConnection;
import dedal.Repository;
import dedal.Specification;
import dedal.impl.DedalFactoryImpl;
import fr.imt.ales.redoc.cba.dedal.structure.DedalArchitecture;
import fr.imt.ales.redoc.cba.dedal.structure.DedalComponentInstance;
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
		this.dedalArchitecture.getConfiguration().forEach(dcc -> {
			try {
				this.dedalArchitecture.getSpecification().addAll(dcc.computeComponentRoles(config.getConfigConnections()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		for (DedalInterfaceType interType : this.dedalArchitecture.getInterfaceTypes()) {
			repo.getInterfaceTypes().add(interType.getInterfaceType());
		}
	}

	// /**
	// * @param config
	// * @param server
	// * @param client
	// * @param result
	// * @return
	// */
	// private Map<? extends ClassConnection, ? extends Interface>
	// buildAbstractInterfacesMultiplyConnected(Configuration config, Interaction
	// server,
	// List<Interaction> client) {
	// Map<ClassConnection,Interface> result = new HashMap<>();
	// Interface iserv = (Interface) server;
	// if(comparable(client))
	// {
	// boolean changed = false;
	// for(Interaction icli : client)
	// {
	// ClassConnection newConnection = this.findConnection(config, iserv,
	// (Interface) icli);
	// Interface newServerInterface = assignNewServerInterface(iserv, (Interface)
	// icli);
	// result.put(newConnection,newServerInterface);
	// if(server!=null && server.equals(newServerInterface))
	// changed = true;
	// }
	// if(changed)
	// Metrics.addNbAbstractType();
	// }
	// else
	// {
	// Metrics.addNbSplitInterfaces();
	// result.putAll(this.decoupleInterfaces(iserv, client,
	// config.getConfigConnections()));
	// }
	// return result;
	// }
	//
	// /**
	// * @param con
	// * @param client
	// */
	// private void calculateCorrespondingRequiredInterface(ClassConnection con,
	// CompClass client) {
	// if(con.getProperty()!=null)
	// {
	// String attrName = con.getProperty().replaceAll("\"", "");
	// Attribute tempAttr = new DedalFactoryImpl().createAttribute();
	// for(Attribute a : client.getAttributes()) {
	// if(a.getName().equals(attrName))
	// {
	// tempAttr = a;
	// break;
	// }
	// }
	// final Attribute attr = tempAttr; //to be used in the following loop, attr
	// must be final.
	//
	// Map<Interface, Class<?>> interfaceToClassMapServer =
	// this.compIntToType.get(client);
	// if(interfaceToClassMapServer!=null)
	// interfaceToClassMapServer.forEach((key, clazz) -> {
	// if(clazz.getCanonicalName().equals(attr.getType()))
	// {
	// con.setClientIntElem(key);
	// }
	// });
	// }
	// }
	//
	// private Boolean comparable(List<Interaction> value) {
	// for(Interaction i1 : value)
	// {
	// for(Interaction i2 : value)
	// {
	// if((this.intToClass.containsKey(i1) && this.intToClass.containsKey(i2)
	// &&!((this.intToClass.get((Interface)
	// i1).isAssignableFrom(this.intToClass.get((Interface) i2)))
	// ||(this.intToClass.get((Interface)
	// i2).isAssignableFrom(this.intToClass.get((Interface) i1))))))
	// return Boolean.FALSE;
	// }
	// }
	// return Boolean.TRUE;
	// }
	//
	// private boolean connected(Interface iserv, List<ClassConnection>
	// configConnections) {
	// for(ClassConnection cc : configConnections)
	// {
	// if(cc.getServerIntElem().equals(iserv))
	// return true;
	// }
	// return false;
	// }

	// /**
	// *
	// * @param iserv
	// * @param value
	// * @param configConnections
	// * @return
	// */
	// private Map<ClassConnection, Interface> decoupleInterfaces(Interface iserv,
	// List<Interaction> value,
	// List<ClassConnection> configConnections) {
	// Map<ClassConnection, Interface> result = new HashMap<>();
	// List<ClassConnection> targetedConnections = new ArrayList<>();
	// for(ClassConnection cc : configConnections)
	// {
	// if(cc.getServerIntElem().equals(iserv))
	// {
	// targetedConnections.add(cc);
	// }
	// }
	// for(ClassConnection cc : targetedConnections)
	// {
	// List<Interaction> comparables = assembleComparable((Interface)
	// cc.getClientIntElem(), value);
	// Interface intToAssign = this.getMostSatisfyingInterface(iserv, comparables);
	// if(!intToAssign.equals(iserv))
	// result.put(cc,intToAssign);
	// }
	// return result;
	// }
	//
	// /**
	// * @param tempRoleConnection
	// * @param cclient
	// * @param realizedByClient
	// * @return
	// */
	// private RoleConnection findClientRole(RoleConnection tempRoleConnection,
	// Interaction iclient, List<CompRole> realizedByClient) {
	// for(CompRole rclient: realizedByClient)
	// {
	// for(Interaction inter2 : rclient.getCompInterfaces())
	// {
	// if(this.intToClass.containsKey(iclient) &&
	// this.intToClass.get(iclient).equals(this.roleIntToType.get(rclient).get(inter2)))
	// {
	// tempRoleConnection.setClientCompElem(rclient);
	// tempRoleConnection.setClientIntElem(inter2);
	// return tempRoleConnection;
	// }
	// }
	// }
	// return tempRoleConnection;
	// }
	//
	// /**
	// *
	// * @param config
	// * @param iserv
	// * @param icli
	// * @return
	// */
	// private ClassConnection findConnection(Configuration config, Interface iserv,
	// Interface icli) {
	// ClassConnection result = new DedalFactoryImpl().createClassConnection();
	// for(ClassConnection cc : config.getConfigConnections())
	// {
	// if(iserv != null && icli != null
	// && iserv.equals(cc.getServerIntElem()) && icli.equals(cc.getClientIntElem()))
	// {
	// return cc;
	// }
	// }
	// return result;
	// }
	//
	// /**
	// * @param tempRoleConnection
	// * @param cserver
	// * @param realizedByServer
	// * @return
	// */
	// private RoleConnection findServerRole(RoleConnection tempRoleConnection,
	// CompClass cserver, List<CompRole> realizedByServer) {
	// for(CompRole rserver : realizedByServer)
	// {
	// for(Interaction inter : cserver.getCompInterfaces())
	// {
	// for(Interaction inter2 : rserver.getCompInterfaces())
	// {
	// try {
	// Class<?> icli =
	// this.roleIntToType.get(tempRoleConnection.getClientCompElem()).get(tempRoleConnection.getClientIntElem());
	// Class<?> iserv = this.roleIntToType.get(rserver).get(inter2);
	// if(this.roleIntToType.get(rserver).get(inter2).isAssignableFrom(this.compIntToType.get(cserver).get(inter))
	// && icli.isAssignableFrom(iserv))
	// {
	// tempRoleConnection.setServerCompElem(rserver);
	// tempRoleConnection.setServerIntElem(inter2);
	// return tempRoleConnection;
	// }
	// } catch (NullPointerException e) {
	// logger.error("A problem occured when trying to find server role. -> " +
	// e.getMessage());
	// }
	//
	// }
	// }
	// }
	// return tempRoleConnection;
	// }
	//
	// /**
	// * Get the {@link #jarMapping}
	// * @return #jarMapping
	// */
	// public Map<URI, List<Class<?>>> getJarMapping() {
	// return jarMapping;
	// }
	//
	// /**
	// * @param config
	// * @param server
	// * @param client
	// * @return
	// */
	// private Map<ClassConnection, Interface>
	// getMostAbstractProvidedInterfaces(Configuration config, Interaction server,
	// List<Interaction> client) {
	// Map<ClassConnection,Interface> result = new HashMap<>();
	// if(client.size()==1) //if a server is connected to a single client
	// {
	// if( (server instanceof Interface) && (client.get(0) instanceof Interface))
	// {
	// Interface iserv = (Interface) server;
	// Interface icli = (Interface) client.get(0);
	// if((this.intToClass.get(icli)).isAssignableFrom(this.intToClass.get(iserv))
	// && !(this.intToClass.get(icli)).equals(this.intToClass.get(iserv)))
	// {
	// Metrics.addNbAbstractType();
	// result.put(this.findConnection(config,iserv,icli),assignNewServerInterface(iserv,
	// icli));
	// }
	// }
	// }
	// else if (client.size()>1)
	// {
	// try {
	// result.putAll(buildAbstractInterfacesMultiplyConnected(config, server,
	// client));
	// }
	// catch (Exception e) {
	// logger.error("A problem occured when setting most abstract provided
	// interfaces with error " + e.getCause(), e);
	// }
	// }
	// else // a server interface of a connection cannot be connected to 0 client
	// interface
	// logger.error("Something went terribly wrong while assembling connections");
	// return result;
	// }
	//
	// private Interface getMostSatisfyingInterface(Interface iserv, Interface icli)
	// {
	// Class<?> baseCliClass = this.intToClass.get(icli);
	// Interface result = iserv;
	// if(iserv != null)
	// {
	// for(Interface i : this.candidateInterfaces.get(iserv))
	// {
	// if(this.intToClass.get(i).isAssignableFrom(this.intToClass.get(result)) &&
	// baseCliClass.isAssignableFrom(this.intToClass.get(i)))
	// {
	// if(this.intToClass.get(i).equals(baseCliClass))
	// return i;
	// result = i;
	// }
	// }
	// }
	// return result;
	// }
	//
	// /**
	// *
	// * @param iserv
	// * @param value
	// * @return
	// */
	// private Interface getMostSatisfyingInterface(Interface iserv,
	// List<Interaction> value) {
	// List<Interface> interfaces = new ArrayList<>();
	// for(Interaction i : value)
	// {
	// interfaces.add(this.getMostSatisfyingInterface(iserv, (Interface) i));
	// }
	// Interface result = interfaces.get(0);
	// for(Interface i1 : interfaces)
	// {
	// if(this.intToClass.get(result).isAssignableFrom(this.intToClass.get(i1)))
	// result = i1;
	// }
	// return result;
	// }
	//
	// /**
	// * @param asm
	// */
	// private void instantiateInteractions(Assembly asm) {
	// try {
	// asm.getAssmComponents().forEach(c -> {
	// Metrics.addNbCompsInst();
	// c.getInstantiates().getCompInterfaces().forEach(ci -> {
	// Metrics.addNbInterfaces();
	// Interaction tempInteraction = (Interaction) EcoreUtil.copy(ci);
	// tempInteraction.setName(ci.getName() + "_" + c.getName());
	// if(ci instanceof Interface)
	// ((Interface) tempInteraction).setInstantiates(((Interface) ci));
	// c.getCompInterfaces().add(tempInteraction);
	// });
	// });
	// } catch (Exception e) {
	// logger.error("An problem occured while instantiating interactions. Ended up
	// with error " + e.getCause());
	// }
	// }
	//
	// /**
	// * @param tempCompClass
	// * @return
	// * @throws ClassNotFoundException
	// */
	// private Class<?> loadClass(String compClass) throws NoClassDefFoundError {
	// try {
	// return hierarchyBuilder.getJarLoader().loadClass(compClass);
	// } catch (ClassNotFoundException e) {
	// if(compClass.lastIndexOf('.')>-1)
	// {
	// String newName = compClass.substring(0, compClass.lastIndexOf('.'))
	// + "$" + compClass.substring(compClass.lastIndexOf('.')+1);
	// return loadClass(newName);
	// }
	// }
	// return null;
	// }
	//
	// /**
	// * @param dedalDiagram
	// * @param repo
	// * @param classList
	// * @param config
	// */
	// private void mapComponentClasses(DedalDiagram dedalDiagram, Repository repo,
	// Configuration config){
	// for(CompClass tempCompClass : config.getConfigComponents())
	// {
	// Metrics.addNbCompsClasses();
	// if(logger.isInfoEnabled())
	// {
	// logger.info("compName : " + tempCompClass.getName());
	// }
	//
	// Class<?> c = null;
	// try {
	// c = loadClass(tempCompClass.getName());
	// } catch (NoClassDefFoundError e) {
	// logger.error("No class has been found for component class" +
	// tempCompClass.getName());
	// }
	//
	// JavaType jt = null;
	//
	// ComponentClassExtractor ce = new ComponentClassExtractor(jt, factory);
	// ce.mapCompClass();
	// this.compToClass.put(tempCompClass, c);
	//// this.compIntToType.put(tempCompClass, ce.getInterfaceToClassMap());
	//// this.intToClass.putAll(ce.getInterfaceToClassMap());
	//// this.candidateInterfaces.putAll(ce.getCandidateInterfaces());
	// }
	// }
	//
	// /**
	// * @param mapServerToClients
	// * @param intServToCon
	// * @param con
	// * @param server
	// * @param clientClass
	// */
	// private void matchCorrespondingServerInterface(Map<Interaction,
	// List<Interaction>> mapServerToClients,
	// Map<Interface, List<ClassConnection>> intServToCon, ClassConnection con,
	// CompClass server,
	// final Class<?> clientClass) {
	// if(clientClass != null)
	// server.getCompInterfaces().forEach(ci -> {
	// Class<?> ciClass = (this.compIntToType.get(server)).get(ci);
	// if(ciClass!=null && clientClass.isAssignableFrom(ciClass))
	// {
	// con.setServerIntElem(ci);
	// }
	// });
	// mapServerToClients.put(con.getServerIntElem(), new ArrayList<>());
	// List<ClassConnection> lcn = new ArrayList<>();
	// lcn.add(con);
	// if(intServToCon.get((Interface)con.getServerIntElem())==null)
	// intServToCon.put((Interface)con.getServerIntElem(),lcn);
	// else
	// intServToCon.get((Interface)con.getServerIntElem()).add(con);
	// }
	//
	// /**
	// * @param dedalDiagram
	// * @param repo
	// * @param spec
	// * @param config
	// * @param asm
	// */
	// private void reconstructArchitecture(DedalDiagram dedalDiagram, Repository
	// repo, Specification spec,
	// Configuration config, Assembly asm) {
	// mapComponentClasses(dedalDiagram, repo, config);
	// setConfigConnections(config);
	//
	// instantiateInteractions(asm);
	// setAssmConnections(asm, config.getConfigConnections());
	//
	// setSpecificationFromConfiguration(repo, spec, config);
	// dedalDiagram.getArchitectureDescriptions().add(spec);
	// config.getImplements().add(spec);
	//
	// dedalDiagram.getArchitectureDescriptions().forEach(ad -> {
	// ad.setName(ad.getName().replaceAll("\\.", "_"));
	// ad.getInterfaceTypes().forEach(it -> {
	// it.setName(it.getName().replaceAll("\\.", "_"));
	// });
	// });
	// config.getConfigComponents().forEach(c -> {
	// c.setName(c.getName().replaceAll("\\.", "_"));
	// });
	// asm.getAssmComponents().forEach(c -> {
	// c.setName(c.getName().replaceAll("\\.", "_"));
	// });
	// spec.getSpecComponents().forEach(c -> {
	// c.setName(c.getName().replaceAll("\\.", "_"));
	// });
	// }
	//
	// /**
	// *
	// * @param dedalDiagram
	// * @param repo
	// * @param spec
	// * @param config
	// * @param asm
	// */
	// private void reconstructArchitectureWithMetrics(DedalDiagram dedalDiagram,
	// Repository repo, Specification spec,
	// Configuration config, Assembly asm) {
	// Metrics.addNbAssembs();
	// Metrics.addNbConfs();
	// if(asm.getAssemblyConnections().isEmpty())
	// Metrics.addNbConnexionlessArchis();
	//
	// reconstructArchitecture(dedalDiagram, repo, spec, config, asm);
	//
	// if(!(spec.getSpecComponents().isEmpty()&&spec.getSpecConnections().isEmpty()))
	// {
	// Metrics.addNbSpecs();
	// if(areEquivalent(spec, config))
	// Metrics.addNbSpecsEqualsConf();
	// }
	//
	// Field f;
	// try {
	// f = ClassLoader.class.getDeclaredField(CLASSES);
	// f.setAccessible(true);
	// @SuppressWarnings("unchecked")
	// ArrayList<Class<?>> classes = (ArrayList<Class<?>>)
	// f.get(hierarchyBuilder.getJarLoader());
	// Metrics.addNbClasses(classes.size());
	// } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
	// | IllegalAccessException e) {
	// logger.error("Error while counting loaded classes", e);
	// }
	//
	// }
	//
	// /**
	// *
	// * @param asm
	// * @param eList
	// */
	// private void setAssmConnections(Assembly asm, EList<ClassConnection> eList) {
	// asm.getAssemblyConnections().forEach(con -> {
	// Metrics.addNbConnexions();
	// CompInstance client = con.getClientInstElem();
	// CompInstance server = con.getServerInstElem();
	// eList.forEach(e -> {
	// CompClass cclient = e.getClientClassElem();
	// CompClass cserver = e.getServerClassElem();
	// setIntElem(con, client, server, e, cclient, cserver);
	// });
	// });
	// }
	//
	// /**
	// * @param config
	// */
	// private void setConfigConnections(Configuration config) {
	// /**
	// * Setting configurations
	// */
	// Map<Interaction, List<Interaction>> mapServerToClients = new HashMap<>();
	// Map<Interface, List<ClassConnection>> intServToCon = new HashMap<>();
	//
	// connectInterfaces(config, mapServerToClients, intServToCon);
	//
	// /**
	// * Connect clients to the most abstract provided interface as possible
	// */
	// config.getConfigConnections().forEach(con -> {
	// Metrics.addNbConnexions();
	// mapServerToClients.get(con.getServerIntElem()).add(con.getClientIntElem());
	// });
	// Map<ClassConnection, Interface> transform = new HashMap<>();
	// mapServerToClients.forEach((key,value) ->
	// transform.putAll(getMostAbstractProvidedInterfaces(config, key, value)));
	//
	// try {
	// intServToCon.forEach((initServ, lcon) -> {
	// for(ClassConnection con : lcon) {
	// CompClass server = con.getServerClassElem();
	// Interaction finalServ = transform.get(con);
	// if(finalServ!=null)
	// {
	// server.getCompInterfaces().add(finalServ);
	// con.setServerIntElem(finalServ);
	// if(!server.getCompInterfaces().contains(finalServ))
	// server.getCompInterfaces().add(finalServ);
	// if(!connected(initServ, config.getConfigConnections()))
	// {
	// server.getCompInterfaces().remove(initServ);
	// }
	// }
	// }
	// });
	// config.getConfigComponents().forEach(cc -> cc.getCompInterfaces().forEach(ci
	// -> Metrics.addNbInterfaces()));
	// }
	// catch (Exception e) {
	// logger.error("A problem occured when building interfaces with error " +
	// e.getCause(), e);
	// }
	// }
	//
	// /**
	// * @param con
	// * @param client
	// * @param server
	// * @param e
	// * @param cclient
	// * @param cserver
	// */
	// private void setIntElem(InstConnection con, CompInstance client, CompInstance
	// server, ClassConnection e,
	// CompClass cclient, CompClass cserver) {
	// try {
	// if(client.getInstantiates().equals(cclient)
	// && server.getInstantiates().equals(cserver))
	// {
	// client.getCompInterfaces().forEach(ci -> {
	// if(ci instanceof Interface
	// && ((Interface) ci).getInstantiates().equals(e.getClientIntElem()))
	// con.setClientIntElem(ci);
	// });
	// server.getCompInterfaces().forEach(ci -> {
	// if(ci instanceof Interface
	// && ((Interface) ci).getInstantiates().equals(e.getServerIntElem()))
	// con.setServerIntElem(ci);
	// });
	// }
	// }
	// catch (Exception ex)
	// {
	// logger.error("An error occured when setting the interface implied in a
	// connection. Ended up with " + ex.getCause());
	// }
	// }
	//
	// /**
	// * Set the {@link #jarMapping}
	// * @param jarMapping the {@link #jarMapping} to set
	// */
	// public void setJarMapping(Map<URI, List<Class<?>>> jarMapping) {
	// this.jarMapping = jarMapping;
	// }
	//
	// /**
	// * @param spec
	// * @param config
	// */
	// private void setSpecConnections(Specification spec, Configuration config) {
	// config.getConfigConnections().forEach(ccon -> {
	// try {
	// RoleConnection tempRoleConnection = new
	// DedalFactoryImpl().createRoleConnection();
	// CompClass cclient = ccon.getClientClassElem();
	// CompClass cserver = ccon.getServerClassElem();
	// List<CompRole> realizedByClient = cclient.getRealizes();
	// List<CompRole> realizedByServer = cserver.getRealizes();
	// tempRoleConnection = findClientRole(tempRoleConnection,
	// ccon.getClientIntElem(), realizedByClient);
	// tempRoleConnection = findServerRole(tempRoleConnection, cserver,
	// realizedByServer);
	// spec.getSpecConnections().add(tempRoleConnection);
	// Metrics.addNbConnexions();
	// } catch (Exception e)
	// {
	// logger.error("A problem occured while setting spec connection with error " +
	// e.getCause(), e);
	// }
	// });
	// }
	//
	// /**
	// *
	// * @param repo
	// * @param dedalDiagram
	// * @param spec
	// * @param config
	// */
	// private void setSpecificationFromConfiguration(Repository repo, Specification
	// spec, Configuration config) {
	// this.compToClass.forEach((cclass,clazz) -> {
	// try {
	//// ComponentRoleExtractor re = new ComponentRoleExtractor(clazz, cclass,
	// this.intToClass, repo);
	//// List<CompRole> extractedRoles = re.calculateSuperTypes();
	//// this.roleIntToType.putAll(re.getRoleToIntToType());
	//// this.intToClass.putAll(re.getIntToType());
	//// this.roleToClass.putAll(re.getRoleToClass());
	//// spec.getSpecComponents().addAll(extractedRoles);
	//// cclass.getRealizes().addAll(extractedRoles);
	//// if(extractedRoles.size()>1)
	//// Metrics.addNbCompClassMultiRoles();
	//// for(CompRole er : extractedRoles)
	//// {
	//// er.getCompInterfaces().forEach(ci -> Metrics.addNbInterfaces());
	//// }
	// } catch (Exception e)
	// {
	// logger.error("A problem occured while setting specification with error " +
	// e.getCause(), e);
	// }
	// });
	// setSpecConnections(spec, config);
	// }
	//
	// /**
	// * @param config
	// * @param asm
	// */
	// private void standardizeNames(Configuration config, Assembly asm) {
	// try {
	// asm.getAssmComponents().forEach(c -> c.setName(c.getName().replace("\"",
	// "")));
	// config.getConfigComponents().forEach(c -> c.setName(c.getName().replace("\"",
	// "")));
	// }
	// catch (Exception e) {
	// logger.error("A problem occured when standardizing names with error " +
	// e.getCause());
	// }
	// }
}