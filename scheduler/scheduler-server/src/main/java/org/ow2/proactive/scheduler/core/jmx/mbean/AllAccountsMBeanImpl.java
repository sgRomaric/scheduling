/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.core.jmx.mbean;

import javax.management.NotCompliantMBeanException;

import org.ow2.proactive.scheduler.core.account.SchedulerAccount;
import org.ow2.proactive.scheduler.core.account.SchedulerAccountsManager;


/**
 * Implementation of the AllAccountsMBean interface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class AllAccountsMBeanImpl extends MyAccountMBeanImpl implements AllAccountsMBean {

    private String targetUsername;

    public AllAccountsMBeanImpl(final SchedulerAccountsManager accountManager) throws NotCompliantMBeanException {
        super(AllAccountsMBean.class, accountManager);
    }

    public void setUsername(final String username) {
        this.targetUsername = username;
    }

    public String getUsername() {
        return this.targetUsername;
    }

    @Override
    protected SchedulerAccount internalGetAccount() {
        return super.accountsManager.getAccount(this.targetUsername); // can be null
    }

}
