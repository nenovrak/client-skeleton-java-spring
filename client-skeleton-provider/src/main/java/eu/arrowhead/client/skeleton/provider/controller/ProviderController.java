package eu.arrowhead.client.skeleton.provider.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
	@RequestMapping(path = "/echo")
	@ResponseBody
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	//TODO: implement here your provider related REST end points

	//-------------------------------------------------------------------------------------------------
	@RequestMapping(path = "/opcua/read/variable")
	@ResponseBody
	public String readVariableNode(@RequestParam(name = "opcuaServerAddress") final String opcuaServerAddress, @RequestParam(name = "opcuaNamespace") final String opcuaNamespace, @RequestParam(name = "opcuaNodeId") final String opcuaNodeId) {
		System.out.println("Got a read variable request:" + opcuaServerAddress + "/" + opcuaNamespace + "/" + opcuaNodeId);
		return "{result:japp}";
	}

	@RequestMapping("*")
	@ResponseBody
	public String fallbackMethod(){
		return "fallback method";
	}
}
