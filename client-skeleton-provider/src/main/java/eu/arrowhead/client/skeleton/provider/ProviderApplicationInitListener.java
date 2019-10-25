package eu.arrowhead.client.skeleton.provider;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.client.library.config.ApplicationInitListener;
import eu.arrowhead.client.library.util.ClientCommonConstants;
import eu.arrowhead.client.skeleton.provider.security.ProviderSecurityConfig;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.client.skeleton.provider.OPC_UA.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

@Component
public class ProviderApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	@Autowired
	private ProviderSecurityConfig providerSecurityConfig;
	
	@Value(ClientCommonConstants.$TOKEN_SECURITY_FILTER_ENABLED_WD)
	private boolean tokenSecurityFilterEnabled;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;

	@Value(ClientCommonConstants.$CLIENT_SYSTEM_NAME)
	private String mySystemName;

	@Value(ClientCommonConstants.$CLIENT_SERVER_ADDRESS_WD)
	private String mySystemAddress;

	@Value(ClientCommonConstants.$CLIENT_SERVER_PORT_WD)
	private int mySystemPort;

	@Value("${opc.ua.connection_address}")
	private String opcuaServerAddress;

	private final Logger logger = LogManager.getLogger(ProviderApplicationInitListener.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {

		//Checking the availability of necessary core systems
		checkCoreSystemReachability(CoreSystem.SERVICE_REGISTRY);
		if (tokenSecurityFilterEnabled) {
			checkCoreSystemReachability(CoreSystem.AUTHORIZATION);			

			//Initialize Arrowhead Context
			arrowheadService.updateCoreServiceURIs(CoreSystem.AUTHORIZATION);			
		}

		setTokenSecurityFilter();
		
		//TODO: implement here any custom behavior on application start up
		//Register services into ServiceRegistry

		// OPC-UA Variable read
		// FIXME This should be read from a file...

		// FIXME The opcServerAddress should NOT include opc.tcp:// since Eclipse Milo will add these
		opcuaServerAddress = opcuaServerAddress.replaceAll("opc.tcp://", "");

		// Variable list:
		// #rootNodeId, read (Adds read services to SR for all variables including and below this node)
		// #rootNodeId, write (Adds write services to SR for all variables including and below this node)

		//String opcServerAddress = "A9824.neteq.ltu.se:53530/OPCUA/SimulationServer";
		//int rootNodeNamespaceIndex = 5;
		//String rootNodeIdentifier = "85/0:Simulation";
		//String readVariableUri = "/opcua/read/variable/";

		System.out.println("SERVER_ADDRESS:" + opcuaServerAddress);

		int rootNodeNamespaceIndex = 5;
		String rootNodeIdentifier = "85/0:Simulation";
		String readVariableUri = "/opcua/read/variable/";


		try {
			NodeId nodeId = new NodeId(rootNodeNamespaceIndex, rootNodeIdentifier);
			OPCUAConnection connection = new OPCUAConnection(opcuaServerAddress);
			Vector<String> nodesBeneath = OPCUAInteractions.browseNode(connection.getConnectedClient(), nodeId);
			connection.dispose();

			for(String nodeString:nodesBeneath) {
				// FIXME Maybe create a custom object for this? This is somewhat hacked together now since I don't really know if this is even the proper way to register/orchestrate the OPC-UA variables...
				String parts[] = nodeString.split(",");
				String identifierPart[] = parts[1].split("=");
				String identifier = identifierPart[1];

				//ServiceRegistryRequestDTO serviceRequest = createServiceRegistryRequest("" + identifier,  "/opcua/read/variable?opcuaServerAddress=" + opcServerAddress + "&opcuaNamespace=" + rootNodeNamespaceIndex + "&opcuaNodeId=" + identifier , HttpMethod.GET);
				ServiceRegistryRequestDTO serviceRequest = createServiceRegistryRequest("" + identifier,  "/opcua/read/variable", HttpMethod.GET);
				serviceRequest.getMetadata().put("nodeId", identifier);
				serviceRequest.getMetadata().put("serverAddress", opcuaServerAddress);
				serviceRequest.getMetadata().put("namespace", "" + rootNodeNamespaceIndex);

				arrowheadService.forceRegisterServiceToServiceRegistry(serviceRequest);
				System.out.println("Registered service for variable " + nodeString + ".");
			}
		} catch (Exception e) {
			System.out.println("ERROR: Could not register to ServiceRegistry.");
		}

		//ServiceRegistryRequestDTO getCarServiceRequest = createServiceRegistryRequest("OPC-UA_read_variable",  "/opcua/read/variable", HttpMethod.GET);
		//getCarServiceRequest.getMetadata().put(CarProviderConstants.REQUEST_PARAM_KEY_BRAND, CarProviderConstants.REQUEST_PARAM_BRAND);
		//arrowheadService.forceRegisterServiceToServiceRegistry(getCarServiceRequest);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void customDestroy() {
		//TODO: implement here any custom behavior on application shout down
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------------------------
	private ServiceRegistryRequestDTO createServiceRegistryRequest(final String serviceDefinition, final String serviceUri, final HttpMethod httpMethod) {
		final ServiceRegistryRequestDTO serviceRegistryRequest = new ServiceRegistryRequestDTO();
		serviceRegistryRequest.setServiceDefinition(serviceDefinition);
		final SystemRequestDTO systemRequest = new SystemRequestDTO();
		systemRequest.setSystemName(mySystemName);
		systemRequest.setAddress(mySystemAddress);
		systemRequest.setPort(mySystemPort);

		if (tokenSecurityFilterEnabled) {
			systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
			serviceRegistryRequest.setSecure(ServiceSecurityType.TOKEN);
			serviceRegistryRequest.setInterfaces(List.of("HTTPS-SECURE-JSON"));
		} else if (sslEnabled) {
			systemRequest.setAuthenticationInfo(Base64.getEncoder().encodeToString(arrowheadService.getMyPublicKey().getEncoded()));
			serviceRegistryRequest.setSecure(ServiceSecurityType.CERTIFICATE);
			serviceRegistryRequest.setInterfaces(List.of("HTTPS-SECURE-JSON"));
		} else {
			serviceRegistryRequest.setSecure(ServiceSecurityType.NOT_SECURE);
			serviceRegistryRequest.setInterfaces(List.of("HTTP-INSECURE-JSON"));
		}
		serviceRegistryRequest.setProviderSystem(systemRequest);
		serviceRegistryRequest.setServiceUri(serviceUri);
		serviceRegistryRequest.setMetadata(new HashMap<>());
		serviceRegistryRequest.getMetadata().put("http-method", httpMethod.name());
		return serviceRegistryRequest;
	}



	private void setTokenSecurityFilter() {
		if(!tokenSecurityFilterEnabled) {
			logger.info("TokenSecurityFilter in not active");
		} else {
			final PublicKey authorizationPublicKey = arrowheadService.queryAuthorizationPublicKey();
			if (authorizationPublicKey == null) {
				throw new ArrowheadException("Authorization public key is null");
			}
			
			KeyStore keystore;
			try {
				keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
				keystore.load(sslProperties.getKeyStore().getInputStream(), sslProperties.getKeyStorePassword().toCharArray());
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
				throw new ArrowheadException(ex.getMessage());
			}			
			final PrivateKey providerPrivateKey = Utilities.getPrivateKey(keystore, sslProperties.getKeyPassword());

			providerSecurityConfig.getTokenSecurityFilter().setAuthorizationPublicKey(authorizationPublicKey);
			providerSecurityConfig.getTokenSecurityFilter().setMyPrivateKey(providerPrivateKey);
		}
	}
}
