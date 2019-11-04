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
	// FIXME Double-check that the token security prevents tampering with variables in the OPC-UA it is not supposed to access (I.e. only allows access to the variables in the Service Registry)
	//-------------------------------------------------------------------------------------------------
	@RequestMapping(path = "/opcua/read/variable")
	@ResponseBody
	public String readVariableNode(@RequestParam(name = "opcuaServerAddress") final String opcuaServerAddress, @RequestParam(name = "opcuaNamespace") final int namespaceIndex, @RequestParam(name = "opcuaNodeId") final String identifier) {
		System.out.println("Got a read variable request:" + opcuaServerAddress + "/" + namespaceIndex + "/" + identifier);
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

	@RequestMapping(path = "/opcua/write/variable")
	@ResponseBody
	public String readVariableNode(@RequestParam(name = "opcuaServerAddress") final String opcuaServerAddress, @RequestParam(name = "opcuaNamespace") final int namespaceIndex, @RequestParam(name = "opcuaNodeId") final String identifier, @RequestParam(name = "value") final String value) {
		System.out.println("Got a write variable request:" + opcuaServerAddress + "/" + namespaceIndex + "/" + identifier + " value: " + value);
		NodeId nodeId = new NodeId(namespaceIndex, identifier);

		OPCUAConnection connection = new OPCUAConnection(opcuaServerAddress);
		String body = "Wrote value: " + value;
		try {
			body = OPCUAInteractions.writeNode(connection.getConnectedClient(), nodeId, value, "double");
			connection.dispose();
			return body;
		} catch (Exception ex) {
			connection.dispose();
			return "There was an error writing to the OPC-UA node.";
		}
	}

	@RequestMapping("*")
	@ResponseBody
	public String fallbackMethod(){
		return "fallback method";
	}
}
