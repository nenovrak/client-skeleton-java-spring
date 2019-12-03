package eu.arrowhead.client.skeleton.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.exception.ArrowheadException;

import java.util.Map;

@SpringBootApplication
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE}) //TODO: add custom packages if any
public class ConsumerMain implements ApplicationRunner {

	//=================================================================================================
	// members

	@Autowired
	private ArrowheadService arrowheadService;

	//=================================================================================================
	// methods

	//------------------------------------------------------------------------------------------------
	public static void main(final String[] args) {
		SpringApplication.run(ConsumerMain.class, args);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void run(final ApplicationArguments args) throws Exception {
		//SIMPLE EXAMPLE OF INITIATING AN ORCHESTRATION

		// --------------------- Read OPC-UA Variable ---------------------------
		OrchestrationResultDTO result = orchestrate("read_counter1");
		Map<String, String> meta = result.getMetadata();

		final HttpMethod httpMethod = HttpMethod.GET;//Http method should be specified in the description of the service.
		final String address = result.getProvider().getAddress();
		final int port = result.getProvider().getPort();
		final String serviceUri = result.getServiceUri();
		final String interfaceName = result.getInterfaces().get(0).getInterfaceName(); //Simplest way of choosing an interface.
		String token = null;
		if (result.getAuthorizationTokens() != null) {
			token = result.getAuthorizationTokens().get(interfaceName); //Can be null when the security type of the provider is 'CERTIFICATE' or nothing.
		}
		final Object payload = null; //Can be null if not specified in the description of the service.

		System.out.println("GET " + address + "/" + serviceUri);
		final String consumedReadService = arrowheadService.consumeServiceHTTP(String.class, httpMethod, address, port, serviceUri, interfaceName, token, payload, "opcuaServerAddress", meta.get("serverAddress"), "opcuaNamespace", meta.get("namespace"), "opcuaNodeId", meta.get("nodeId"));
		System.out.println("Service response: " + consumedReadService);


		// --------------------- Write OPC-UA Variable ---------------------------
		//result = orchestrate("write_square1");
        result = orchestrate("write_counter1");
		Map<String, String> meta2 = result.getMetadata();
		// FIXME Why are the variables above (and therefore here) declared final? Thread safety? Performance? I've kept them final here (i.e. created new varibles instead of re-using the above), but I'd rather re-use the above variables if possible?
		final HttpMethod httpMethod2 = HttpMethod.POST;
		final String address2 = result.getProvider().getAddress();
		final int port2 = result.getProvider().getPort();
		final String serviceUri2 = result.getServiceUri();
		final String interfaceName2 = result.getInterfaces().get(0).getInterfaceName(); //Simplest way of choosing an interface.

		token = null;
		if (result.getAuthorizationTokens() != null) {
			token = result.getAuthorizationTokens().get(interfaceName); //Can be null when the security type of the provider is 'CERTIFICATE' or nothing.
		}
		final Object payload2 = null; //Can be null if not specified in the description of the service.

		System.out.println("POST " + address2 + "/" + serviceUri2);
		String valueAsString = "123";
		final String consumedWriteService = arrowheadService.consumeServiceHTTP(String.class, httpMethod2, address2, port2, serviceUri2, interfaceName2, token, payload2, "opcuaServerAddress", meta2.get("serverAddress"), "opcuaNamespace", meta2.get("namespace"), "opcuaNodeId", meta2.get("nodeId"), "value", valueAsString);
		System.out.println("Service response: " + consumedWriteService);
	}

	public OrchestrationResultDTO orchestrate(String serviceDefinition) {
		final Builder orchestrationFormBuilder = arrowheadService.getOrchestrationFormBuilder();

		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement(serviceDefinition);

		orchestrationFormBuilder.requestedService(requestedService)
				.flag(Flag.MATCHMAKING, false) //When this flag is false or not specified, then the orchestration response cloud contain more proper provider. Otherwise only one will be chosen if there is any proper.
				.flag(Flag.OVERRIDE_STORE, true) //When this flag is false or not specified, then a Store Orchestration will be proceeded. Otherwise a Dynamic Orchestration will be proceeded.
				.flag(Flag.TRIGGER_INTER_CLOUD, false); //When this flag is false or not specified, then orchestration will not look for providers in the neighbor clouds, when there is no proper provider in the local cloud. Otherwise it will.

		final OrchestrationFormRequestDTO orchestrationRequest = orchestrationFormBuilder.build();

		OrchestrationResponseDTO response = null;
		try {
			response = arrowheadService.proceedOrchestration(orchestrationRequest);
		} catch(final ArrowheadException ex) {
			//Handle the unsuccessful request as you wish!
		}

		if(response ==null||response.getResponse().isEmpty()) {
			//If no proper providers found during the orchestration process, then the response list will be empty. Handle the case as you wish!
			System.out.println("FATAL ERROR: Orchestration response came back empty. Make sure the Service you try to consume is in the Service Registry and that the Consumer has the privileges to consume this Service (e.g. check intra_cloud_authorization and intra_cloud_interface_connection).");
			System.exit(1);
		}

		final OrchestrationResultDTO result = response.getResponse().get(0); //Simplest way of choosing a provider.
		return result;
	}
}
