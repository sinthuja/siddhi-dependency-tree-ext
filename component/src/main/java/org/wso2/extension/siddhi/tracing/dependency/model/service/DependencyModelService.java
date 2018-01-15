/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.extension.siddhi.tracing.dependency.model.service;

import org.wso2.extension.siddhi.tracing.dependency.model.util.DependencyModelManager;
import org.wso2.extension.siddhi.tracing.dependency.model.util.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * This is MSF4J service for the dependency model to plot the UI graph.
 */
@Path("/dependencyModel")
public class DependencyModelService {

    @GET
    @Path("/graph")
    @Produces("application/json")
    public Response getGraph() {
        List<GraphEdge> graphEdges = new ArrayList<>();
        Map<String, Integer> graphNodeIdCache = new HashMap<>();
        List<GraphNode> graphNodes = new ArrayList<>();

        Iterator<Node> nodeSet = DependencyModelManager.getInstance().getNodes().iterator();
        int nodeId = 0;
        while (nodeSet.hasNext()) {
            Node node = nodeSet.next();
            GraphNode graphNode = new GraphNode(++nodeId, node.getName(), node.getTags());
            graphNodes.add(graphNode);
            graphNodeIdCache.put(graphNode.getLabel(), graphNode.getId());
        }

        Iterator<String> edgeSet = DependencyModelManager.getInstance().getEdges().iterator();
        while (edgeSet.hasNext()) {
            String[] parentChildNodeNames = DependencyModelManager.getInstance().
                    getParentChildNodeNames(edgeSet.next());
            GraphEdge edge = new GraphEdge(graphNodeIdCache.get(parentChildNodeNames[0]),
                    graphNodeIdCache.get(parentChildNodeNames[1]));
            graphEdges.add(edge);
        }
        return Response.ok().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                .entity(new Graph(graphNodes, graphEdges))
                .build();
    }
}
