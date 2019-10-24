package eu.arrowhead.client.skeleton.provider.controller;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.springframework.web.bind.annotation.*;
import eu.arrowhead.client.skeleton.provider.OPC_UA.*;

@RestController
public class ProviderController {
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
	public String readVariableNode(@RequestParam(name = "opcuaServerAddress") final String opcuaServerAddress, @RequestParam(name = "opcuaNamespace") final int namespaceIndex, @RequestParam(name = "opcuaNodeId") final String identifier) {
		System.out.println("Got a read variable request:" + opcuaServerAddress + "/" + namespaceIndex + "/" + identifier);
		//NodeId nodeId = new NodeId(Integer.parseInt(namespaceIndex), Integer.parseInt(identifier));
		NodeId nodeId = new NodeId(namespaceIndex, identifier);

		OPCUAConnection connection = new OPCUAConnection(opcuaServerAddress);
		String body = "";
		try {
			body = OPCUAInteractions.readVariableNode(connection.getConnectedClient(), nodeId);
			connection.dispose();
			return body;
		} catch (Exception ex) {
			connection.dispose();
			return "There was an error reading the OPC-UA node.";
		}
	}

	@RequestMapping("*")
	@ResponseBody
	public String fallbackMethod(){
		return "fallback method";
	}
}
