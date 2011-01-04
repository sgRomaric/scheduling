/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.selectionscript;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;

import org.junit.Assert;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *
 * This class tests RM's mechanism of resource selection with static
 * scripts.
 *
 * -launch nodes with a specified JVM environment variable => verify script
 * -launch 5 nodes without this specified JVM environment variable => not verify script
 *
 *  1/ ask 1 node with a selection that check this environment variable,
 *  and verify that a node can be provided by RM
 *  2/ ask 3 nodes with specific environment var, and check that just one node can be provided
 *  3/ add a second node with specific JVM env var and ask 3 nodes with specific environment var,
 *   and check that two nodes can be provided with one getAtMostNodes method call.
 *  4/ remove the node with specified environment var, end check that no node
 *  can be provided
 *  5/ ask a node with a selection script that provides execution error,
 *  and check that error handling is correct.
 *  6/ ask a node with a selection script that doesn't return
 *  the 'selected' return value
 *
 * @author ProActive team
 *
 */
public class staticSelectionScriptTest extends FunctionalTest {

    private String vmPropSelectionScriptpath = this.getClass().getResource("vmPropertySelectionScript.js")
            .getPath();

    private String badSelectionScriptpath = this.getClass().getResource("badSelectionScript.js").getPath();

    private String withoutSelectedSelectionScriptpath = this.getClass().getResource(
            "withoutSelectedSScript.js").getPath();

    private String vmPropKey = "myProperty";
    private String vmPropValue = "myValue";

    /** Actions to be Perform by this test.
    * The method is called automatically by Junit framework.
    * @throws Exception If the test fails.
    */
    @org.junit.Test
    public void action() throws Exception {

        ResourceManager resourceManager = RMTHelper.getResourceManager();

        RMTHelper.log("Deployment");
        RMTHelper.createGCMLocalNodeSource();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.GCM_LOCAL);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //wait for the nodes to be in free state
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        String node1Name = "node1";
        String node2Name = "node2";

        HashMap<String, String> vmProperties = new HashMap<String, String>();
        vmProperties.put(this.vmPropKey, this.vmPropValue);

        String node1URL = RMTHelper.createNode(node1Name, vmProperties).getNodeInformation().getURL();
        resourceManager.addNode(node1URL, NodeSource.GCM_LOCAL);

        //wait node adding event
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        //wait for the nodes to be in free state
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        //create the static selection script object
        SelectionScript sScript = new SelectionScript(new File(vmPropSelectionScriptpath), new String[] {
                this.vmPropKey, this.vmPropValue }, false);

        RMTHelper.log("Test 1");

        NodeSet nodes = resourceManager.getAtMostNodes(1, sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(nodes.get(0).getNodeInformation().getURL().equals(node1URL));

        //wait for node busy event
        RMNodeEvent evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        resourceManager.releaseNode(nodes.get(0));

        //wait for node free event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        RMTHelper.log("Test 2");

        nodes = resourceManager.getAtMostNodes(3, sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(nodes.get(0).getNodeInformation().getURL().equals(node1URL));

        //wait for node busy event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        resourceManager.releaseNode(nodes.get(0));

        //wait for node free event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        RMTHelper.log("Test 3");

        //add a second with JVM env var
        String node2URL = RMTHelper.createNode(node2Name, vmProperties).getNodeInformation().getURL();
        resourceManager.addNode(node2URL, NodeSource.GCM_LOCAL);

        //wait node adding event

        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the nodes to be in free state
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = resourceManager.getAtMostNodes(3, sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 2);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        //wait for node busy event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        resourceManager.releaseNodes(nodes);

        //wait for nodes free event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        RMTHelper.log("Test 4");

        resourceManager.removeNode(node1URL, true);
        resourceManager.removeNode(node2URL, true);

        //wait for node removed event
        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL);
        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node2URL);

        nodes = resourceManager.getAtMostNodes(3, sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 0);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        RMTHelper.log("Test 5");

        //create the bad static selection script object
        SelectionScript badScript = new SelectionScript(new File(badSelectionScriptpath), new String[] {},
            false);

        nodes = resourceManager.getAtMostNodes(3, badScript);

        //wait node selection
        try {
            PAFuture.waitFor(nodes);
            System.out.println("Number of found nodes " + nodes.size());
            Assert.assertTrue(false);
        } catch (RuntimeException e) {
        }

        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        RMTHelper.log("Test 6");

        //create the static selection script object that doesn't define 'selected'
        SelectionScript noSelectedScript = new SelectionScript(new File(withoutSelectedSelectionScriptpath),
            new String[] {}, false);

        nodes = resourceManager.getAtMostNodes(3, noSelectedScript);

        //wait node selection
        try {
            PAFuture.waitFor(nodes);
            System.out.println("Number of found nodes " + nodes.size());
            Assert.assertTrue(false);
        } catch (RuntimeException e) {
        }
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);
    }
}
