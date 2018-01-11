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
package org.wso2.extension.siddhi.tracing.dependency.model;

import org.wso2.extension.siddhi.tracing.dependency.model.service.DependencyModelService;
import org.wso2.extension.siddhi.tracing.dependency.model.util.DependencyModelManager;
import org.wso2.extension.siddhi.tracing.dependency.model.util.Node;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the Siddhi extension which generates the dependency graph for the spans created.
 */
@Extension(
        name = "dependencyTree",
        namespace = "tracing",
        description = "This generates the dependency model of the spans",
        examples = @Example(description = "TBD"
                , syntax = "from inputStream#tracing:dependencyTree(componentName, spanId, parentId, serviceName," +
                " tags, references) \" +\n" +
                "                \"select * \n" +
                "                \"insert into outputStream;")
)
public class DependencyModelGenerationExtension extends StreamProcessor {
    private ExpressionExecutor componentNameExecutor;
    private ExpressionExecutor spanIdExecutor;
    private ExpressionExecutor parentIdExecutor;
    private ExpressionExecutor serviceNameExecutor;
    private ExpressionExecutor tagExecutor;
    private ExpressionExecutor referenceExecutor;
    private MicroservicesRunner microservicesRunner;

    @Override
    protected void process(ComplexEventChunk<StreamEvent> complexEventChunk, Processor processor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

        Map<Long, Node> spanIdNodeNameCache = new HashMap<>();
        while (complexEventChunk.hasNext()) {
            StreamEvent streamEvent = complexEventChunk.next();
            String componentName = (String) componentNameExecutor.execute(streamEvent);
            long spanId = (Long) spanIdExecutor.execute(streamEvent);
            long parentId = (Long) parentIdExecutor.execute(streamEvent);
            String serviceName = (String) serviceNameExecutor.execute(streamEvent);
            String tags = (String) tagExecutor.execute(streamEvent);
            String reference = (String) referenceExecutor.execute(streamEvent);

            Node node = new Node(getNodeName(componentName, serviceName), tags);
            spanIdNodeNameCache.put(spanId, node);

            DependencyModelManager.getInstance().addNode(node);
            if (parentId != 0) {
                Node parentNode = spanIdNodeNameCache.get(parentId);
                DependencyModelManager.getInstance().addEdge(parentNode, node);
            }
        }
    }

    private String getNodeName(String componentName, String serviceName) {
        return componentName + " - " + serviceName;
    }

    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors,
                                   ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        if (expressionExecutors.length != 6) {
            throw new SiddhiAppCreationException("Minimum number of attributes is 2, those are Source ID and Sequence "
                    + "number fields of the stream. " +
                    "But only found 1 attribute.");
        } else {
            if (expressionExecutors[0].getReturnType() == Attribute.Type.STRING) {
                componentNameExecutor = expressionExecutors[0];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the component name "
                        + "field, but found a field with return type - " + expressionExecutors[0].getReturnType());
            }

            if (expressionExecutors[1].getReturnType() == Attribute.Type.LONG) {
                spanIdExecutor = expressionExecutors[1];
            } else {
                throw new SiddhiAppCreationException("Expected a field with Long return type for the span id field," +
                        "but found a field with return type - " + expressionExecutors[1].getReturnType());
            }

            if (expressionExecutors[2].getReturnType() == Attribute.Type.LONG) {
                parentIdExecutor = expressionExecutors[2];
            } else {
                throw new SiddhiAppCreationException("Expected a field with Long return type for the parent id field,"
                        + "but found a field with return type - " + expressionExecutors[2].getReturnType());
            }

            if (expressionExecutors[3].getReturnType() == Attribute.Type.STRING) {
                serviceNameExecutor = expressionExecutors[3];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the service name" +
                        " field, but found a field with return type - " + expressionExecutors[3].getReturnType());
            }

            if (expressionExecutors[4].getReturnType() == Attribute.Type.STRING) {
                tagExecutor = expressionExecutors[4];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the tags field," +
                        "but found a field with return type - " + expressionExecutors[4].getReturnType());
            }

            if (expressionExecutors[5].getReturnType() == Attribute.Type.STRING) {
                referenceExecutor = expressionExecutors[5];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the tags field," +
                        "but found a field with return type - " + expressionExecutors[5].getReturnType());
            }
        }
        return new ArrayList<Attribute>();
    }

    @Override
    public void start() {
        microservicesRunner = new MicroservicesRunner(8083)
                .deploy(new DependencyModelService());
        microservicesRunner.start();
    }

    @Override
    public void stop() {
        microservicesRunner.stop();
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }
}
