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
package functionaltests;

import java.util.Map.Entry;

import org.junit.Assert;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionalTests.FunctionalTest;


/**
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 *
 * Submit a job (test 1).
 * After the job submission, the test monitor all jobs states changes, in order
 * to observe its execution :
 * job submitted (test 2),
 * job pending to running (test 3),
 * the task pending to running, and task running to finished (test 4),
 * job running to finished (test 5).
 * After it retrieves job's result and check that the
 * task result is available (test 6).
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive Scheduling 1.0
 */
public class TestJobMultiNodesSubmission extends FunctionalTest {

    private static String jobDescriptor = TestJobMultiNodesSubmission.class.getResource(
            "/functionaltests/descriptors/Job_MultiNodes.xml").getPath();

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        //submit job
        JobId id = SchedulerTHelper.submitJob(jobDescriptor);
        //connect to RM
        RMAuthentication rmAuth = RMConnection.waitAndJoin(null);
        ResourceManager rmAdmin = rmAuth.login(Credentials.getCredentials(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));

        //wait job is running
        SchedulerTHelper.waitForEventJobRunning(id);

        Thread.sleep(500);

        //check RM has 5 busy nodes
        RMState rms = rmAdmin.getState();
        Assert.assertEquals(5, rms.getTotalNodesNumber() - rms.getFreeNodesNumber());

        //wait for job to be finished
        SchedulerTHelper.waitForEventJobFinished(id);

        // check result are not null
        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());

        for (Entry<String, TaskResult> entry : res.getAllResults().entrySet()) {
            Assert.assertNotNull(entry.getValue().value());
        }

        //remove job
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);

    }
}
