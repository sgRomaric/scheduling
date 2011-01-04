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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.listeners;

import org.ow2.proactive.scheduler.common.job.JobId;


/**
 * Class providing events for running jobs.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public interface RunningJobsListener {

    /**
     * Invoke by jobs controller when a job has just started scheduling
     *
     * @param jobId the jobid
     */
    public void addRunningJob(JobId jobId);

    /**
     * Invoke by jobs controller when a job has just been terminated
     *
     * @param jobId the jobid
     */
    public void removeRunningJob(JobId jobId);

    /**
     * One or more tasks have been replicated inside this job,
     * some updates may be necessary
     * 
     * @param jobId the jobid
     */
    public void taskReplicated(JobId jobId);

    /**
     * Tasks were skipped due to branching flow action
     * 
     * @param info
     */
    public void taskSkipped(JobId info);
}
