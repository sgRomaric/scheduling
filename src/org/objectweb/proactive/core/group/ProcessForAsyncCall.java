package org.objectweb.proactive.core.group;

import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class provides multithreading for the (a)synchronous methodcall on a group.
 * 
 * @author Laurent Baduel
 */

public class ProcessForAsyncCall implements Runnable {
	private ProxyForGroup proxyGroup;
	private Vector memberList;
	private Vector memberListOfResultGroup;
	private int index;
	private MethodCall mc;
	private Body body;

	public ProcessForAsyncCall(ProxyForGroup proxyGroup, Vector memberList, Vector memberListOfResultGroup, int index, MethodCall mc, Body body) {
		this.proxyGroup = proxyGroup;
		this.memberList = memberList;
		this.memberListOfResultGroup = memberListOfResultGroup;
		this.index = index;
		this.mc = mc;
		this.body = body;
	}

	public synchronized void run() {
		Object object = this.memberList.get(this.index);
		LocalBodyStore.getInstance().setCurrentThreadBody(body);
		/* only do the communication (reify) if the object is not an error nor an exception */ 
		if (!(object instanceof Throwable)) {
			try {
				/* add the return value into the result group */
				this.proxyGroup.addToListOfResult(this.memberListOfResultGroup, ((StubObject) object).getProxy().reify(this.mc), this.index);
			} catch (Throwable e) {
				/* when an exception occurs, put it in the result group instead of the (unreturned) value */ 
				this.proxyGroup.addToListOfResult(this.memberListOfResultGroup,new ExceptionInGroup(this.memberList.get(this.index),e),this.index);
			}
		}
		else {
			/* when there is a Throwable instead of an Object, a method call is impossible, add null to the result group */
			this.proxyGroup.addToListOfResult(this.memberListOfResultGroup,null,this.index);
		}
	}
}
