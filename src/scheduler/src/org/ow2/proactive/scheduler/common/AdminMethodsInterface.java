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
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.exception.AccessRightException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.policy.Policy;


/**
 * AdminMethodsInterface describe the methods that an administrator
 * should do in addition to the user methods.<br>
 * This interface represents what a scheduler administrator should do.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 * $Id$
 */
@PublicAPI
@Deprecated
public interface AdminMethodsInterface {
    /**
     * For administrator only, Change the policy of the scheduler.<br>
     * This method will immediately change the policy and so the whole scheduling process.
     *
     * @param newPolicyFile the new policy file as a class.
     * @return true if the policy has been correctly change, false if not.
     * @throws SchedulerException if an exception occurs while serving the request.
     * @throws NotConnectedException if you are not authenticated.
     * @throws AccessRightException if you have not enough permission to access this method.
     */
    public BooleanWrapper changePolicy(Class<? extends Policy> newPolicyFile) throws SchedulerException;

    /**
     * For administrator only, Start the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws AccessRightException if you have not enough permission to access this method.
     */
    public BooleanWrapper start() throws SchedulerException;

    /**
     * For administrator only, Stop the scheduler.<br>
     * Once done, you won't be able to submit job, and the scheduling will be stopped.<br>
     * Every running jobs will be terminated.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws AccessRightException if you have not enough permission to access this method.
     */
    public BooleanWrapper stop() throws SchedulerException;

    /**
     * For administrator only, Pause the scheduler by terminating running jobs.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws AccessRightException if you have not enough permission to access this method.
     */
    public BooleanWrapper pause() throws SchedulerException;

    /**
     * For administrator only, Freeze the scheduler by terminating running tasks.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws AccessRightException if you have not enough permission to access this method.
     */
    public BooleanWrapper freeze() throws SchedulerException;

    /**
     * For administrator only, Resume the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws AccessRightException if you have not enough permission to access this method.
     */
    public BooleanWrapper resume() throws SchedulerException;

    /**
     * For administrator only, Shutdown the scheduler.<br>
     * It will terminate every submitted jobs but won't accept new submit.<br>
     * Use {@link #kill()} if you want to stop the scheduling and exit the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws AccessRightException if you have not enough permission to access this method.
     */
    public BooleanWrapper shutdown() throws SchedulerException;

    /**
     * For administrator only, Kill the scheduler.<br>
     * Will stop the scheduling, and shutdown the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws AccessRightException if you have not enough permission to access this method.
     */
    public BooleanWrapper kill() throws SchedulerException;

    /**
     * For administrator only, Reconnect a new Resource Manager to the scheduler.<br>
     * Can be used if the resource manager has crashed.
     *
     * @param rmURL the URL of the new Resource Manager to link to the scheduler.<br>
     * 		Example : //host/RM_node_name
     * @return true if success, false otherwise.
     * @throws SchedulerException if an exception occurs while serving the request.
     * @throws NotConnectedException if you are not authenticated.
     * @throws AccessRightException if you have not enough permission to access this method.
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException;
}
