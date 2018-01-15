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

/**
 * This is the POJO of the Graphe node information that is being returned to by the MSF4J services -
 * {@link DependencyModelService}
 */
public class GraphNode {
    private int id;
    private String label;
    private String title;

    public GraphNode(int id, String label, String title) {
        this.id = id;
        this.label = label;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getTitle() {
        return title;
    }
}
