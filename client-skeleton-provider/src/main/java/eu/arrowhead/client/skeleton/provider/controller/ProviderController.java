package eu.arrowhead.client.skeleton.provider.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;

@RestController
public class ProviderController {




/*
	NodeId nodeId = new NodeId(namespaceIndex, identifier);

	OPCUAConnection connection = new OPCUAConnection(address);
        try {
		OPCUAInteractions.readVariableNode(connection.getConnectedClient(), nodeId);
		connection.dispose();
		return Response.status(200).entity("CONTENT GOES HERE...").build();
	} catch (Exception ex) {
		Logger.getLogger(RESTHandler.class.getName()).log(Level.SEVERE, null, ex);
		connection.dispose();
		return Response.status(500).build();
	}
*/


	//=================================================================================================
	// members

	//TODO: add your variables here

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	//TODO: implement here your provider related REST end points

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = "/opcua/read/variable/{opcuaServerAddress}/{opcuaNamespace}/{opcuaNodeId}")
	@ResponseBody
	public String readVariableNode(@PathVariable(name = "opcuaServerAddress") final String opcuaServerAddress, @PathVariable(name = "opcuaNamespace") final String opcuaNamespace, @PathVariable(name = "opcuaNodeId") final String opcuaNodeId) {
		System.out.println("Got a read variable request:" + opcuaServerAddress + "/" + opcuaNamespace + "/" + opcuaNodeId);
		return "{result:japp}";
	}
}
