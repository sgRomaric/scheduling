/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.ic2d.spy;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.event.ProActiveEvent;

public class SpyEvent extends ProActiveEvent implements java.io.Serializable {
  
  public static final int GENERIC_TYPE = 0;
  public static final int OBJECT_WAIT_BY_NECESSITY_TYPE = 20;
  public static final int OBJECT_WAIT_FOR_REQUEST_TYPE = 30;
  public static final int REQUEST_SENT_MESSAGE_TYPE = 50;
  public static final int REPLY_SENT_MESSAGE_TYPE = 60;
  public static final int REQUEST_RECEIVED_MESSAGE_TYPE = 51;
  public static final int REPLY_RECEIVED_MESSAGE_TYPE = 61;
  public static final int BODY_EVENT_TYPE = 100;
  public static final int BODY_CREATION_EVENT_TYPE = 200;

  /** The id of the object */
  protected UniqueID bodyID;
  
  /** Position. useful for the receiver only */
  protected int pos = -1;
  
  public SpyEvent(int type, UniqueID bodyID) {
    super(bodyID, type);
    this.bodyID = bodyID;
  }
  
  public UniqueID getBodyID() { return bodyID; }
  
  public int getPos() { return pos; }
  public void setPos(int v) { pos = v; }
  
  public String toString() {
    return "SpyEvent "+eventTypeAsString(type)+" body="+bodyID;
  }
  
  public static String eventTypeAsString(int type) {
    switch (type) {
      case GENERIC_TYPE: return "GENERIC_TYPE";
      case OBJECT_WAIT_BY_NECESSITY_TYPE: return "OBJECT_WAIT_BY_NECESSITY_TYPE";
      case OBJECT_WAIT_FOR_REQUEST_TYPE: return "OBJECT_WAIT_FOR_REQUEST_TYPE";
      case REQUEST_SENT_MESSAGE_TYPE: return "REQUEST_SENT_MESSAGE_TYPE";
      case REPLY_SENT_MESSAGE_TYPE: return "REPLY_SENT_MESSAGE_TYPE";
      case REQUEST_RECEIVED_MESSAGE_TYPE: return "REQUEST_RECEIVED_MESSAGE_TYPE";
      case REPLY_RECEIVED_MESSAGE_TYPE: return "REPLY_RECEIVED_MESSAGE_TYPE";
      case BODY_EVENT_TYPE: return "BODY_EVENT_TYPE";
      case BODY_CREATION_EVENT_TYPE: return "BODY_CREATION_EVENT_TYPE";
    }
    return "UNKNOWN";
  }
}
 