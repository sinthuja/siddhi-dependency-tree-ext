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

package org.siddhi.extension.tracing.dependency.model;

import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

/**
 * Test case to validate the behaviour of the Dependency tree test
 */
public class SiddhiDependencyTreeTest {
    @Test
    public void testExtension() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "define stream GroupedSpanStream (componentName string, traceId string, " +
                "spanId long, baggageItems string, parentId long, serviceName string, duration long, tags string, " +
                "references string);";
        String query = ("@info(name = 'query1') from GroupedSpanStream#tracing:dependencyTree(componentName, spanId, " +
                "parentId, serviceName, tags, references) \n" +
                "select * \n" +
                "insert into ProcessedGroupedSpanStream");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("GroupedSpanStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2 MSF4J", "f76c6ac4-c847-4e11-bae4-c67f6046034a", 11L, "{}", 0L, "report",
                14039241L,
                "{\"operation.name\":\"GET\",\"service.context\":\"/report/invoice/I001\",\"span.kind\":" +
                        "\"server-receive\"}", "[]"});
        inputHandler.send(new Object[]{"WSO2 MSF4J", "f76c6ac4-c847-4e11-bae4-c67f6046034a", 12L, "{}", 11L,
                "InvoiceServiceClient", 5082743L,
                "{\"operation.name\":\"GET\",\"service.context\":\"http://localhost:8082/invoice/I001\"," +
                        "\"span.kind\":\"client-send\"}",
                "[{\"context\":{\"traceId\":\"f76c6ac4-c847-4e11-bae4-c67f6046034a\",\"baggage\":{},\"spanId\":11}," +
                        "\"referenceType\":\"child_of\"}]]"});
        inputHandler.send(new Object[]{"WSO2 MSF4J", "f76c6ac4-c847-4e11-bae4-c67f6046034a", 13L, "{}", 12L,
                "invoice", 51248L,
                "{\"operation.name\":\"GET\",\"service.context\":\"/invoice/I001\",\"span.kind\":\"server-receive\"}",
                "[{\"context\":{\"traceId\":\"f76c6ac4-c847-4e11-bae4-c67f6046034a\",\"baggage\":{},\"spanId\":12}," +
                        "\"referenceType\":\"child_of\"}]]"});
        inputHandler.send(new Object[]{"WSO2 MSF4J", "f76c6ac4-c847-4e11-bae4-c67f6046034a", 14L, "{}", 11L,
                "CustomerServiceClient", 6711145L,
                "{\"operation.name\":\"GET\",\"service.context\":\"http://localhost:8081/customer/C001\"," +
                        "\"span.kind\":\"client-send\"}",
                "[{\"context\":{\"traceId\":\"f76c6ac4-c847-4e11-bae4-c67f6046034a\",\"baggage\":{}," +
                        "\"spanId\":11},\"referenceType\":\"child_of\"}]"});
        inputHandler.send(new Object[]{"WSO2 MSF4J", "f76c6ac4-c847-4e11-bae4-c67f6046034a", 15L, "{}", 14L,
                "customer", 65290L,
                "{\"operation.name\":\"GET\",\"service.context\":\"/customer/C001\",\"span.kind\":\"server-receive\"}",
                "[{\"context\":{\"traceId\":\"f76c6ac4-c847-4e11-bae4-c67f6046034a\",\"baggage\":{},\"spanId\":14}," +
                        "\"referenceType\":\"child_of\"}]"});
        siddhiAppRuntime.shutdown();
    }
}
