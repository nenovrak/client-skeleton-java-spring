package eu.arrowhead.client.skeleton.provider.OPC_UA;


import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.client.api.nodes.VariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

/**
 * This class contains different ways of interacting with OPC-UA. Note that the clients
 * supplied to the functions must already be connected (e.g. created through the OPCUAConnection class)
 * @author Niklas Karvonen
 */


public class OPCUAInteractions {

    public static Vector<String> browseNode(OpcUaClient client, NodeId browseRoot) {
        //String returnString = "";
        Vector<String> returnNodes = new Vector<String>();
        try {
            List<Node> nodes = client.getAddressSpace().browse(browseRoot).get();
            for(Node node:nodes) {
                returnNodes.add("ns=" + node.getNodeId().get().getNamespaceIndex() + ",identifier=" + node.getNodeId().get().getIdentifier() + ",displayName=" + node.getDisplayName().get().getText() + ",nodeClass=" + node.getNodeClass().get());
            }
        } catch (Exception e) {
            System.out.println("Browsing nodeId=" + browseRoot + " failed: " + e.getMessage());
        }
        return returnNodes;
    }


    public static String readNode(OpcUaClient client, NodeId nodeId) {
        String returnString = "";
        try {
            VariableNode node = client.getAddressSpace().createVariableNode(nodeId);
            DataValue value = node.readValue().get();

            CompletableFuture<DataValue> test = client.readValue(0.0, TimestampsToReturn.Both, nodeId);
            DataValue data = test.get();
            System.out.println("DataValue Object: " + data);
            returnString = data.toString();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.toString());
        }
        return returnString;
    }


    public static CompletableFuture<StatusCode> writeNode2(
            final OpcUaClient client,
            final NodeId nodeId,
            final Object value) {

        return client.writeValue(nodeId, new DataValue(new Variant(value)));
    }

    public static String writeNode(OpcUaClient client, NodeId nodeId, String value) {

        // FIXME There should be a way to programmatically get the type from Eclipse Milo and write the variable directly using that type. As far as I can see, however, Milo only supports writing Variants which requires the conversion of a value into an object before it can be written.
        String returnString = "";
        returnString += value;
        try {
            VariableNode node = client.getAddressSpace().createVariableNode(nodeId);
            Object val = new Object();
            Object identifier = node.getDataType().get().getIdentifier();
            UInteger id = UInteger.valueOf(0);

            if(identifier instanceof UInteger) {
                id = (UInteger) identifier;
            }

            System.out.println("getIdentifier: " + node.getDataType().get().getIdentifier());
            switch (id.intValue()) {
                // See Identifiers class in package org.eclipse.milo.opcua.stack.core; for more information
                case 11: // Double
                    val = Double.valueOf(value);
                    break;
                case 6: //Int32
                    val = Integer.valueOf(value);
                    break;
            }

            DataValue data = new DataValue(new Variant(val),StatusCode.GOOD, null);
            StatusCode status = client.writeValue(nodeId, data).get();
            System.out.println("Wrote DataValue: " + data + " status: " + status);
            returnString = status.toString();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.toString());
        }
        return returnString;
    }

}
