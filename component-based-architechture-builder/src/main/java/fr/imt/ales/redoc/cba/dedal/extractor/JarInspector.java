package fr.imt.ales.redoc.cba.dedal.extractor;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import dedal.Assembly;
import dedal.Attribute;
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
import fr.imt.ales.redoc.cba.dedal.metrics.Metrics;
import fr.imt.ales.redoc.cba.dedal.transformation.SdslTransformer;
import fr.imt.ales.redoc.jarloader.JarLoader;

/**
 * This class is designed for inspecting jar/war files and generate the Dedal diagram
 * @author Alexandre Le Borgne
 */
public class JarInspector {

	/**
	 * static name of classes field in {@link ClassLoader}
	 */
	private static final String CLASSES = "classes";
	/**
	 * Logger
	 */
	static final Logger logger = LogManager.getLogger(JarInspector.class);
	/**
	 * {@link JarLoader}
	 */
	fr.imt.ales.redoc.jarloader.JarLoader jarLoader = null;
	/**
	 * Map jar/war to their respective <code>List<Class?></code>
	 */
	Map<URI, List<Class<?>>> jarMapping = null;
	/**
	 * Map Dedal component roles to their respective Java {@link Class}
	 */
	Map<CompRole, Class<?>> roleToClass = null;
	/**
	 * Map Dedal component classes to their respective Java {@link Class}
	 */
	Map<CompClass, Class<?>> compToClass = null;
	/**
	 * Map Dedal interfaces to their respective Java {@link Class}
	 */
	Map<Interface, Class<?>> intToClass = null;
	/**
	 * Map Dedal interfaces to the list of their respective candidate interfaces
	 */
	Map<Interface, List<Interface>> candidateInterfaces = null;
	/**
	 * The type hierarchy of a Dedal component class
	 */
	Map<CompClass, List<CompRole>> typeHierarchy = null;
	/**
	 * Map Dedal component classes to a Map of their interfaces to corresponding Java classes
	 */
	Map<CompClass, Map<Interface, Class<?>>> compIntToType = null;
	/**
	 * Map Dedal component roles to a Map of their interfaces to corresponding Java classes
	 */
	Map<CompRole, Map<Interface, Class<?>>> roleIntToType = null;
	/**
	 * The {@link DedalFactory} object for generating the model artifacts
	 */
	DedalFactory factory;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Constructor, init, accessors	//////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructor
	 * @param jarLoader The JarLoader object that has loaded the jar/war files
	 */
	public JarInspector(JarLoader jarLoader) {
		super();
		this.jarLoader = jarLoader;
		this.compToClass = new HashMap<>();
		this.roleToClass = new HashMap<>();
		this.compIntToType = new HashMap<>();
		this.roleIntToType = new HashMap<>();
		this.typeHierarchy = new HashMap<>();
		this.intToClass = new HashMap<>();
		this.candidateInterfaces = new HashMap<>();
		factory = DedalFactoryImpl.init();
	}
	/**
	 * Initialization
	 */
	private void init()
	{
		this.compToClass = new HashMap<>();
		this.roleToClass = new HashMap<>();
		this.compIntToType = new HashMap<>();
		this.roleIntToType = new HashMap<>();
		this.typeHierarchy = new HashMap<>();
		this.intToClass = new HashMap<>();
		this.candidateInterfaces = new HashMap<>();
	}

	/**
	 * Get the {@link JarLoader}
	 * @return {@link #jarLoader}
	 */
	public JarLoader getJarLoader() {
		return jarLoader;
	}

	/**
	 * Set the {@link JarLoader} instance
	 * @param jarLoader the {@link #jarLoader} to set
	 */
	public void setJarLoader(JarLoader jarLoader) {
		this.jarLoader = jarLoader;
	}

	/**
	 * Get the {@link #jarMapping}
	 * @return #jarMapping
	 */
	public Map<URI, List<Class<?>>> getJarMapping() {
		return jarMapping;
	}

	/**
	 * Set the {@link #jarMapping}
	 * @param jarMapping the {@link #jarMapping} to set
	 */
	public void setJarMapping(Map<URI, List<Class<?>>> jarMapping) {
		this.jarMapping = jarMapping;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////Constructor, init, accessors	//////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Generates a Dedal diagram
	 * @param dd The Dedal diagram to generate
	 * @param xmlSpringFiles The list of Spring XML files
	 */
	public void generate(DedalDiagram dedalDiagram, List<URI> xmlSpringFiles) {
		Repository repo = new DedalFactoryImpl().createRepository();
		repo.setName("genRepo");
		dedalDiagram.getRepositories().add(repo);
		List<EObject> extractedFromSpring = new ArrayList<>();

		for(URI uri : xmlSpringFiles)
		{
			Metrics.addNbSpringXML();
			init();
			try {
				extractedFromSpring.addAll(SdslTransformer.extractDedalArtifacts(uri.toURL().getFile()));
				Specification spec = factory.createSpecification();
				Configuration config = factory.createConfiguration();
				Assembly asm = factory.createAssembly();
				initArchitectureDescriptions(dedalDiagram, extractedFromSpring, uri, spec, config, asm);

				if(!config.getConfigComponents().isEmpty())
				{
					reconstructArchitectureWithMetrics(dedalDiagram, repo, spec, config, asm);
				}
				else
					Metrics.addNbEmptySpringXML();
			} catch (Exception e) {
				logger.error("An error occured while extracting information in file " + uri.toString() + "Error -> " + e.getCause(), e);
			}
		}	
		
	}

	/**
	 * 
	 * @param dedalDiagram
	 * @param repo
	 * @param spec
	 * @param config
	 * @param asm
	 */
	private void reconstructArchitectureWithMetrics(DedalDiagram dedalDiagram, Repository repo, Specification spec,
			Configuration config, Assembly asm) {
		Metrics.addNbAssembs();
		Metrics.addNbConfs();
		if(asm.getAssemblyConnections().isEmpty())
			Metrics.addNbConnexionlessArchis();
		
		reconstructArchitecture(dedalDiagram, repo, spec, config, asm);
				
		if(!(spec.getSpecComponents().isEmpty()&&spec.getSpecConnections().isEmpty()))
		{
			Metrics.addNbSpecs();
			if(areEquivalent(spec, config))
				Metrics.addNbSpecsEqualsConf();
		}
		
		
//		Instrumentation instrumentation = InstrumentHook.getInstrumentation();
//		int nb = instrumentation.getInitiatedClasses(jarLoader).length;
//		System.out.println(nb);
		
		Field f;
		try {
			f = ClassLoader.class.getDeclaredField(CLASSES);
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			Vector<Class<?>> classes =  (Vector<Class<?>>) f.get(jarLoader);
			Metrics.addNbClasses(classes.size());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			logger.error("Error while counting loaded classes", e);
		}

	}

	/**
	 * @param dedalDiagram
	 * @param extractedFromSpring
	 * @param uri
	 * @param spec
	 * @param config
	 * @param asm
	 */
	private void initArchitectureDescriptions(DedalDiagram dedalDiagram, List<EObject> extractedFromSpring, URI uri,
			Specification spec, Configuration config, Assembly asm) {
		String tempName = uri.toString().replaceAll("/", ".").substring(
				uri.toString().replaceAll("/", ".").indexOf(dedalDiagram.getName()));
		spec.setName(tempName+"_spec");
		config.setName(tempName+"_config");
		dedalDiagram.getArchitectureDescriptions().add(config);
		asm.setInstantiates(config);
		asm.setName(tempName+"_asm");
		dedalDiagram.getArchitectureDescriptions().add(asm);

		copyInDiagram(extractedFromSpring, config, asm);

		standardizeNames(config, asm);
	}

	/**
	 * 
	 * @param spec
	 * @param config
	 * @return
	 */
	private Boolean areEquivalent(Specification spec, Configuration config) {
		if((config.getConfigComponents().size() != spec.getSpecComponents().size()) 
				|| (config.getConfigConnections().size() != spec.getSpecConnections().size()))
			return Boolean.FALSE;
		else 
		{
			for(CompClass cc : config.getConfigComponents())
			{
				if((cc.getRealizes().size()>1)
						||!this.roleToClass.get(cc.getRealizes().get(0)).equals(this.compToClass.get(cc)))
					return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	/**
	 * @param dedalDiagram
	 * @param repo
	 * @param spec
	 * @param config
	 * @param asm
	 */
	private void reconstructArchitecture(DedalDiagram dedalDiagram, Repository repo, Specification spec,
			Configuration config, Assembly asm) {
		mapComponentClasses(dedalDiagram, repo, config);
		setConfigConnections(config);

		instantiateInteractions(asm);
		setAssmConnections(asm, config.getConfigConnections());

		setSpecificationFromConfiguration(repo, spec, config);
		dedalDiagram.getArchitectureDescriptions().add(spec);
		config.getImplements().add(spec);
		
		dedalDiagram.getArchitectureDescriptions().forEach(ad -> {
			ad.setName(ad.getName().replaceAll("\\.", "_"));
			ad.getInterfaceTypes().forEach(it -> {
				it.setName(it.getName().replaceAll("\\.", "_"));
			});
		});
		config.getConfigComponents().forEach(c -> {
			c.setName(c.getName().replaceAll("\\.", "_"));
		});
		asm.getAssmComponents().forEach(c -> {
			c.setName(c.getName().replaceAll("\\.", "_"));
		});
		spec.getSpecComponents().forEach(c -> {
			c.setName(c.getName().replaceAll("\\.", "_"));
		});
	}

	/**
	 * 
	 * @param dedalDiagram
	 * @param sdslPath
	 * @throws URISyntaxException 
	 */
	public void generate(DedalDiagram dedalDiagram, String sdslPath) throws URISyntaxException{

		Repository repo = new DedalFactoryImpl().createRepository();
		repo.setName("genRepo");
		dedalDiagram.getRepositories().add(repo);
		List<EObject> extractedFromSpring = SdslTransformer.extractDedalArtifacts(sdslPath);

		Specification spec = factory.createSpecification();
		spec.setName(sdslPath.replace('.', '_')+"_spec");
		Configuration config = factory.createConfiguration();
		config.setName(sdslPath.replace('.', '_')+"_config");
		dedalDiagram.getArchitectureDescriptions().add(config);
		Assembly asm = factory.createAssembly();
		asm.setInstantiates(config);
		asm.setName(sdslPath.replace('.', '_')+"_asm");
		dedalDiagram.getArchitectureDescriptions().add(asm);

		copyInDiagram(extractedFromSpring, config, asm);
		
		standardizeNames(config, asm);

		if(logger.isInfoEnabled())
			logger.info("URL : " + sdslPath);

		reconstructArchitectureWithMetrics(dedalDiagram, repo, spec, config, asm);

	}

	/**
	 * 
	 * @param repo 
	 * @param dedalDiagram 
	 * @param spec
	 * @param config
	 */
	private void setSpecificationFromConfiguration(Repository repo, Specification spec, Configuration config) {
		this.compToClass.forEach((cclass,clazz) -> {
			try {
				ComponentRoleExtractor re = new ComponentRoleExtractor(clazz, cclass, this.intToClass, repo);
				List<CompRole> extractedRoles = re.calculateSuperTypes();
				this.roleIntToType.putAll(re.getRoleToIntToType());
				this.intToClass.putAll(re.getIntToType());
				this.roleToClass.putAll(re.getRoleToClass());
				spec.getSpecComponents().addAll(extractedRoles);
				cclass.getRealizes().addAll(extractedRoles);
				if(extractedRoles.size()>1)
					Metrics.addNbCompClassMultiRoles();
				for(CompRole er : extractedRoles)
				{
					er.getCompInterfaces().forEach(ci -> Metrics.addNbInterfaces());
				}
			} catch (Exception e)
			{
				logger.error("A problem occured while setting specification with error " + e.getCause(), e);
			}
		});
		setSpecConnections(spec, config);
	}

	/**
	 * @param spec
	 * @param config
	 */
	private void setSpecConnections(Specification spec, Configuration config) {
		config.getConfigConnections().forEach(ccon -> {
			try {
				RoleConnection tempRoleConnection = new DedalFactoryImpl().createRoleConnection();
				CompClass cclient = ccon.getClientClassElem();
				CompClass cserver = ccon.getServerClassElem();
				List<CompRole> realizedByClient = cclient.getRealizes();
				List<CompRole> realizedByServer = cserver.getRealizes();
				tempRoleConnection = findClientRole(tempRoleConnection, ccon.getClientIntElem(), realizedByClient);
				tempRoleConnection = findServerRole(tempRoleConnection, cserver, realizedByServer);
				spec.getSpecConnections().add(tempRoleConnection);
				Metrics.addNbConnexions();
			} catch (Exception e)
			{
				logger.error("A problem occured while setting spec connection with error " + e.getCause(), e);
			}
		});
	}

	/**
	 * @param tempRoleConnection
	 * @param cserver
	 * @param realizedByServer
	 * @return 
	 */
	private RoleConnection findServerRole(RoleConnection tempRoleConnection, CompClass cserver, List<CompRole> realizedByServer) {
		for(CompRole rserver : realizedByServer)
		{
			for(Interaction inter : cserver.getCompInterfaces())
			{
				for(Interaction inter2 : rserver.getCompInterfaces())
				{
					try {
						Class<?> icli = this.roleIntToType.get(tempRoleConnection.getClientCompElem()).get(tempRoleConnection.getClientIntElem());
						Class<?> iserv = this.roleIntToType.get(rserver).get(inter2);
						if(this.roleIntToType.get(rserver).get(inter2).isAssignableFrom(this.compIntToType.get(cserver).get(inter))
								&& icli.isAssignableFrom(iserv))
						{
							tempRoleConnection.setServerCompElem(rserver);
							tempRoleConnection.setServerIntElem(inter2);
							return tempRoleConnection;
						}
					} catch (NullPointerException e) {
						logger.error("A problem occured when trying to find server role. -> " + e.getMessage());
					}
					
				}
			}
		}
		return tempRoleConnection;
	}

	/**
	 * @param tempRoleConnection
	 * @param cclient
	 * @param realizedByClient
	 * @return 
	 */
	private RoleConnection findClientRole(RoleConnection tempRoleConnection, Interaction iclient, List<CompRole> realizedByClient) {
		for(CompRole rclient: realizedByClient)
		{
				for(Interaction inter2 : rclient.getCompInterfaces())
				{
					if(this.intToClass.containsKey(iclient) && this.intToClass.get(iclient).equals(this.roleIntToType.get(rclient).get(inter2)))
					{
						tempRoleConnection.setClientCompElem(rclient);
						tempRoleConnection.setClientIntElem(inter2);
						return tempRoleConnection;
					}
				}
		}
		return tempRoleConnection;
	}

	/**
	 * 
	 * @param asm
	 * @param eList
	 */
	private void setAssmConnections(Assembly asm, EList<ClassConnection> eList) {
		asm.getAssemblyConnections().forEach(con -> {
			Metrics.addNbConnexions();
			CompInstance client = con.getClientInstElem();
			CompInstance server = con.getServerInstElem();
			eList.forEach(e -> {
				CompClass cclient = e.getClientClassElem();
				CompClass cserver = e.getServerClassElem();
				setIntElem(con, client, server, e, cclient, cserver);
			});
		});
	}

	/**
	 * @param con
	 * @param client
	 * @param server
	 * @param e
	 * @param cclient
	 * @param cserver
	 */
	private void setIntElem(InstConnection con, CompInstance client, CompInstance server, ClassConnection e,
			CompClass cclient, CompClass cserver) {
		try {
			if(client.getInstantiates().equals(cclient)
					&& server.getInstantiates().equals(cserver))
			{
				client.getCompInterfaces().forEach(ci -> {
					if(ci instanceof Interface
							&& ((Interface) ci).getInstantiates().equals(e.getClientIntElem()))
						con.setClientIntElem(ci);
				});
				server.getCompInterfaces().forEach(ci -> {
					if(ci instanceof Interface
							&& ((Interface) ci).getInstantiates().equals(e.getServerIntElem()))
						con.setServerIntElem(ci);
				});
			}
		}
		catch (Exception ex)
		{
			logger.error("An error occured when setting the interface implied in a connection. Ended up with " + ex.getCause());
		}
	}

	/**
	 * @param config
	 */
	private void setConfigConnections(Configuration config) {
		/**
		 * Setting configurations
		 */
		Map<Interaction, List<Interaction>> mapServerToClients = new HashMap<>();
		Map<Interface, List<ClassConnection>> intServToCon = new HashMap<>();
		
		connectInterfaces(config, mapServerToClients, intServToCon);
		
		/**
		 * Connect clients to the most abstract provided interface as possible
		 */
		config.getConfigConnections().forEach(con -> {
			Metrics.addNbConnexions();
			mapServerToClients.get(con.getServerIntElem()).add(con.getClientIntElem());
		});
		Map<ClassConnection, Interface> transform = new HashMap<>();
		mapServerToClients.forEach((key,value) -> transform.putAll(getMostAbstractProvidedInterfaces(config, key, value)));
		
		try {
			intServToCon.forEach((initServ, lcon) -> {
				for(ClassConnection con : lcon) {
					CompClass server = con.getServerClassElem();
					Interaction finalServ = transform.get(con);
					if(finalServ!=null)
					{
						server.getCompInterfaces().add(finalServ);
						con.setServerIntElem(finalServ);
						if(!server.getCompInterfaces().contains(finalServ))
							server.getCompInterfaces().add(finalServ);
						if(!connected(initServ, config.getConfigConnections()))
						{
							server.getCompInterfaces().remove(initServ);
						}
					}
				}
			});
			config.getConfigComponents().forEach(cc -> cc.getCompInterfaces().forEach(ci -> Metrics.addNbInterfaces()));
		}
		catch (Exception e) {
			logger.error("A problem occured when building interfaces with error " + e.getCause(), e);
		}
	}

	/**
	 * @param config
	 * @param mapServerToClients
	 * @param intServToCon
	 */
	private void connectInterfaces(Configuration config, Map<Interaction, List<Interaction>> mapServerToClients,
			Map<Interface, List<ClassConnection>> intServToCon) {
		config.getConfigConnections().forEach(con -> {
			CompClass client = con.getClientClassElem();
			CompClass server = con.getServerClassElem();
			
			/**
			 * calculate which is the corresponding required interface
			 */
			calculateCorrespondingRequiredInterface(con, client);
			final Class<?> clientClass = (this.compIntToType.get(client) != null)?(this.compIntToType.get(client)).get(con.getClientIntElem()):null;
			
			/**
			 * matching the corresponding server interface
			 */
			matchCorrespondingServerInterface(mapServerToClients, intServToCon, con, server, clientClass);
		});
	}

	/**
	 * @param mapServerToClients
	 * @param intServToCon
	 * @param con
	 * @param server
	 * @param clientClass
	 */
	private void matchCorrespondingServerInterface(Map<Interaction, List<Interaction>> mapServerToClients,
			Map<Interface, List<ClassConnection>> intServToCon, ClassConnection con, CompClass server,
			final Class<?> clientClass) {
		if(clientClass != null)
			server.getCompInterfaces().forEach(ci -> {
				Class<?> ciClass = (this.compIntToType.get(server)).get(ci);
				if(ciClass!=null && clientClass.isAssignableFrom(ciClass))
				{
					con.setServerIntElem(ci);
				}
			});
		mapServerToClients.put(con.getServerIntElem(), new ArrayList<>());
		List<ClassConnection> lcn = new ArrayList<>();
		lcn.add(con);
		if(intServToCon.get((Interface)con.getServerIntElem())==null)
			intServToCon.put((Interface)con.getServerIntElem(),lcn);
		else
			intServToCon.get((Interface)con.getServerIntElem()).add(con);
	}

	/**
	 * @param con
	 * @param client
	 */
	private void calculateCorrespondingRequiredInterface(ClassConnection con, CompClass client) {
		if(con.getProperty()!=null)
		{
			String attrName = con.getProperty().replaceAll("\"", "");
			Attribute tempAttr = new DedalFactoryImpl().createAttribute();
			for(Attribute a : client.getAttributes()) {
				if(a.getName().equals(attrName))
				{
					tempAttr = a;
					break;
				}
			}
			final Attribute attr = tempAttr; //to be used in the following loop, attr must be final.

			Map<Interface, Class<?>> interfaceToClassMapServer = this.compIntToType.get(client);
			if(interfaceToClassMapServer!=null)
				interfaceToClassMapServer.forEach((key, clazz)  -> {
					if(clazz.getCanonicalName().equals(attr.getType()))
					{
						con.setClientIntElem(key);
					}
				});
		}
	}

	/**
	 * @param config
	 * @param server
	 * @param client
	 * @return 
	 */
	private Map<ClassConnection, Interface> getMostAbstractProvidedInterfaces(Configuration config, Interaction server, List<Interaction> client) {
		Map<ClassConnection,Interface> result = new HashMap<>();
		if(client.size()==1) //if a server is connected to a single client
		{
			if( (server instanceof Interface) && (client.get(0) instanceof Interface))
			{
				Interface iserv = (Interface) server;
				Interface icli = (Interface) client.get(0);
				if((this.intToClass.get(icli)).isAssignableFrom(this.intToClass.get(iserv)) 
						&& !(this.intToClass.get(icli)).equals(this.intToClass.get(iserv)))
				{
					Metrics.addNbAbstractType();
					result.put(this.findConnection(config,iserv,icli),assignNewServerInterface(iserv, icli));
				}
			}
		}
		else if (client.size()>1)
		{
			try {
				result.putAll(buildAbstractInterfacesMultiplyConnected(config, server, client));
			}
			catch (Exception e) {
				logger.error("A problem occured when setting most abstract provided interfaces with error " + e.getCause(), e);
			}
		}
		else // a server interface of a connection cannot be connected to 0 client interface
			logger.error("Something went terribly wrong while assembling connections");
		return result;
	}

	/**
	 * @param config
	 * @param server
	 * @param client
	 * @param result
	 * @return 
	 */
	private Map<? extends ClassConnection, ? extends Interface> buildAbstractInterfacesMultiplyConnected(Configuration config, Interaction server,
			List<Interaction> client) {
		Map<ClassConnection,Interface> result = new HashMap<>();
		Interface iserv = (Interface) server;
		if(comparable(client))
		{
			boolean changed = false;
			for(Interaction icli : client)
			{
				ClassConnection newConnection = this.findConnection(config, iserv, (Interface) icli);
				Interface newServerInterface = assignNewServerInterface(iserv, (Interface) icli);
				result.put(newConnection,newServerInterface);
				if(server!=null && server.equals(newServerInterface))
					changed = true;
			}
			if(changed)
				Metrics.addNbAbstractType();
		}
		else
		{
			Metrics.addNbSplitInterfaces();
			result.putAll(this.decoupleInterfaces(iserv, client, config.getConfigConnections()));
		}
		return result;
	}

	/**
	 * 
	 * @param config
	 * @param iserv
	 * @param icli
	 * @return
	 */
	private ClassConnection findConnection(Configuration config, Interface iserv, Interface icli) {
		ClassConnection result = new DedalFactoryImpl().createClassConnection();
		for(ClassConnection cc : config.getConfigConnections())
		{
			if(iserv != null && icli != null 
					&& iserv.equals(cc.getServerIntElem()) && icli.equals(cc.getClientIntElem()))
			{
				return cc;
			}
		}
		return result;
	}

	/**
	 * @param iserv
	 * @param icli
	 */
	private Interface assignNewServerInterface(Interface iserv, Interface icli) {
		Interface intToAssign = this.getMostSatisfyingInterface(iserv, icli);
		if(intToAssign!=null && !intToAssign.equals(iserv))
		{
			return intToAssign;
		}
		return iserv;
	}

	/**
	 * 
	 * @param iserv
	 * @param value
	 * @param configConnections
	 * @return 
	 */
	private Map<ClassConnection, Interface> decoupleInterfaces(Interface iserv, List<Interaction> value,
			List<ClassConnection> configConnections) {
		Map<ClassConnection, Interface> result = new HashMap<>();
		List<ClassConnection> targetedConnections = new ArrayList<>();
		for(ClassConnection cc : configConnections)
		{
			if(cc.getServerIntElem().equals(iserv))
			{
				targetedConnections.add(cc);
			}
		}
		for(ClassConnection cc : targetedConnections)
		{
			List<Interaction> comparables =  assembleComparable((Interface) cc.getClientIntElem(), value);
			Interface intToAssign = this.getMostSatisfyingInterface(iserv, comparables);
			if(!intToAssign.equals(iserv))
				result.put(cc,intToAssign);
		}
		return result;
	}

	private boolean connected(Interface iserv, List<ClassConnection> configConnections) {
		for(ClassConnection cc : configConnections)
		{
			if(cc.getServerIntElem().equals(iserv))
				return true;
		}
		return false;
	}

	private List<Interaction> assembleComparable(Interface inter, List<Interaction> interfaces) {
		List<Interaction> result = new ArrayList<>();
		for(Interaction i : interfaces)
		{
			Interface tempI = (Interface) i;
			if((this.intToClass.get(tempI).isAssignableFrom(this.intToClass.get(inter)))
					|| (this.intToClass.get(inter).isAssignableFrom(this.intToClass.get(tempI))))
				result.add(tempI);
		}
		return result;
	}

	/**
	 * 
	 * @param iserv
	 * @param value
	 * @return
	 */
	private Interface getMostSatisfyingInterface(Interface iserv, List<Interaction> value) {
		List<Interface> interfaces = new ArrayList<>();
		for(Interaction i : value)
		{
			interfaces.add(this.getMostSatisfyingInterface(iserv, (Interface) i));
		}
		Interface result = interfaces.get(0);
		for(Interface i1 : interfaces)
		{
			if(this.intToClass.get(result).isAssignableFrom(this.intToClass.get(i1)))
				result = i1;
		}
		return result;
	}

	private Boolean comparable(List<Interaction> value) {
		for(Interaction i1 : value)
		{
			for(Interaction i2 : value)
			{
				if((this.intToClass.containsKey(i1) && this.intToClass.containsKey(i2)
						&&!((this.intToClass.get((Interface) i1).isAssignableFrom(this.intToClass.get((Interface) i2)))
						||(this.intToClass.get((Interface) i2).isAssignableFrom(this.intToClass.get((Interface) i1))))))
					return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	private Interface getMostSatisfyingInterface(Interface iserv, Interface icli) {
		Class<?> baseCliClass = this.intToClass.get(icli);
		Interface result = iserv;
		if(iserv != null)
		{
			for(Interface i : this.candidateInterfaces.get(iserv))
			{
				if(this.intToClass.get(i).isAssignableFrom(this.intToClass.get(result)) && baseCliClass.isAssignableFrom(this.intToClass.get(i)))
				{
					if(this.intToClass.get(i).equals(baseCliClass))
						return i;
					result = i;	
				}
			}
		}
		return result;
	}

	/**
	 * @param asm
	 */
	private void instantiateInteractions(Assembly asm) {
		try {
			asm.getAssmComponents().forEach(c -> {
				Metrics.addNbCompsInst();
				c.getInstantiates().getCompInterfaces().forEach(ci -> {
					Metrics.addNbInterfaces();
					Interaction tempInteraction = (Interaction) EcoreUtil.copy(ci);
					tempInteraction.setName(ci.getName() + "_" + c.getName());
					if(ci instanceof Interface)
						((Interface) tempInteraction).setInstantiates(((Interface) ci));
					c.getCompInterfaces().add(tempInteraction);
				});
			});
		} catch (Exception e) {
			logger.error("An problem occured while instantiating interactions. Ended up with error " + e.getCause());
		}
	}

	/**
	 * @param dedalDiagram
	 * @param repo
	 * @param classList
	 * @param config
	 */
	private void mapComponentClasses(DedalDiagram dedalDiagram, Repository repo, Configuration config){
		for(CompClass tempCompClass : config.getConfigComponents())
		{
			Metrics.addNbCompsClasses();
			if(logger.isInfoEnabled())
			{
				logger.info("compName : " + tempCompClass.getName());
			}
			Class<?> c = null;
			try {
				c = loadClass(tempCompClass.getName());
			} catch (NoClassDefFoundError e) {
				logger.error("No class has been found for component class" + tempCompClass.getName());
			}
			if(c == null)
			{
				Metrics.addNbFailedClass();
				c = Object.class;
			}
			ComponentClassExtractor ci = new ComponentClassExtractor(c, dedalDiagram, config, repo);
			ci.mapComponentClass(tempCompClass);
			this.compToClass.put(tempCompClass, c);
			this.compIntToType.put(tempCompClass, ci.getInterfaceToClassMap());
			this.intToClass.putAll(ci.getInterfaceToClassMap());
			this.candidateInterfaces.putAll(ci.getCandidateInterfaces());
		}
	}

	/**
	 * @param tempCompClass
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?> loadClass(String compClass) throws NoClassDefFoundError {
		try {
			return jarLoader.loadClass(compClass);
		} catch (ClassNotFoundException e) {
			if(compClass.lastIndexOf('.')>-1)
			{
				String newName = compClass.substring(0, compClass.lastIndexOf('.')) 
						+ "$" + compClass.substring(compClass.lastIndexOf('.')+1);
				return loadClass(newName);
			}
		}
		return null;
	}

	/**
	 * @param extractedFromSpring
	 * @param config
	 * @param asm
	 */
	private void copyInDiagram(List<EObject> extractedFromSpring, Configuration config, Assembly asm) {
		extractedFromSpring.forEach(artefact -> {
			if(artefact instanceof DedalDiagram)
			{
				((DedalDiagram) artefact).getArchitectureDescriptions().forEach(ad -> {
					if(ad instanceof Assembly)
					{
						asm.getAssmComponents().addAll(((Assembly) ad).getAssmComponents());
						asm.getAssemblyConnections().addAll(((Assembly) ad).getAssemblyConnections());
					}
					else if(ad instanceof Configuration)
					{
						config.getConfigComponents().addAll(((Configuration) ad).getConfigComponents());
						config.getConfigConnections().addAll(((Configuration) ad).getConfigConnections());
					}
				});
			}
		});
	}

	/**
	 * @param config
	 * @param asm
	 */
	private void standardizeNames(Configuration config, Assembly asm) {
		try {
			asm.getAssmComponents().forEach(c -> c.setName(c.getName().replace("\"", "")));
			config.getConfigComponents().forEach(c -> c.setName(c.getName().replace("\"", "")));
		}
		catch (Exception e) {
			logger.error("A problem occured when standardizing names with error " + e.getCause());
		}
	}
}