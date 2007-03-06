/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package testsuite.test;

import testsuite.exception.AssertionFailedException;


/**
 * This class provides assertion methods which upon failure throw an {@link testsuite.exception.AssertionFailedException}
 * leading to the exit of the current test.
 * @author Matthieu Morel
 */
public class Assertions {

    /**
     * Asserts a boolean condition.
     * If it is false, a runtime exception is thrown (AssertionFailedException) and
     * the current test is terminated
     * @param condition the boolean condition
     */
    static public void assertTrue(boolean condition) {
        assertTrue(null, condition);
    }

    /**
     * Asserts a boolean condition.
     * If it is false, a runtime exception is thrown with the given message (AssertionFailedException) and
     * the current test is terminated
     * @param failureMessage failure explanation
     * @param condition the boolean condition
     */
    static public void assertTrue(String failureMessage, boolean condition) {
        if (!condition) {
            failureNotification(failureMessage);
        }
    }

    /**
     * Asserts a boolean condition
     * If it is true, a runtime exception is thrown (AssertionFailedException) and
     * the current test is terminated
     * @param failureMessage failure explanation
     * @param condition the boolean condition
     */
    static public void assertFalse(String failureMessage, boolean condition) {
        if (condition) {
            failureNotification(failureMessage);
        }
    }

    /**
     * Asserts a boolean condition
     * If it is true, a runtime exception is thrown (AssertionFailedException) and
     * the current test is terminated
     * @param condition the boolean condition
     */
    static public void assertFalse(boolean condition) {
        assertFalse(null, condition);
    }

    /**
     * Asserts the equality of 2 elements. <br>
     * The elements  can be null
     * If the elements are different , a runtime exception is thrown (AssertionFailedException) and
     * the current test is terminated
     *
     * @param o1 first object
     *  @param o2 second object
     */
    static public void assertEquals(Object o1, Object o2) {
        assertEquals(null, o1, o2);
    }

    /**
     * Asserts the equality of 2 elements. <br>
     * The elements  can be null
     * If the elements are different , a runtime exception is thrown (AssertionFailedException) and
     * the current test is terminated
     *  @param failureMessage failure explanation
     * @param o1 first object
     *  @param o2 second object
     */
    static public void assertEquals(String failureMessage, Object o1, Object o2) {
        if ((o1 == null) || (o2 == null)) {
            if (o1 != o2) {
                throw new AssertionFailedException(failureMessage);
            } else {
                return;
            }
        }
        if (!o1.equals(o2)) {
            throw new AssertionFailedException(failureMessage);
        }
    }

    /**
     * Asserts the null value of an element <br>
     * If the element is not null , a runtime exception is thrown (AssertionFailedException) and
     * the current test is terminated
     *
     * @param o the reference to test
     */
   static public void assertNull(Object o) {
	  if (o!=null) {
		  throw new AssertionFailedException(null);
	  }
    }
   
    /**
     * Asserts the non-equality of 2 elements. <br>
     * The elements  can be null
     * If the elements are equal , a runtime exception is thrown (AssertionFailedException) and
     * the current test is terminated
     *
     * @param o1 first object
     *  @param o2 second object
     */
    static public void assertNonEquals(Object o1, Object o2) {
        assertNonEquals(null, o1, o2);
    }

    /**
     * Asserts the non-equality of 2 elements. <br>
     * The elements  can be null
     * If the elements are equal, a runtime exception is thrown (AssertionFailedException) and
     * the current test is terminated
     *  @param failureMessage failure explanation
     * @param o1 first object
     *  @param o2 second object
     */
    static public void assertNonEquals(String failureMessage, Object o1,
        Object o2) {
        if ((o1 == null) || (o2 == null)) {
            if (o1 == o2) {
                throw new AssertionFailedException(failureMessage);
            } else {
                return;
            }
        }
        if (o1.equals(o2)) {
            throw new AssertionFailedException(failureMessage);
        }
    }
    
    /**
     * Asserts that an element is not null<br>
     * If the element is  null , a runtime exception is thrown (AssertionFailedException) and
     * the current test is terminated
     *
     * @param o the reference to test
     */
   static public void assertNotNull(Object o) {
	  assertNotNull(null, o);
    }
    
   /**
    * Asserts that an element is not null<br>
    * If the element is  null , a runtime exception is thrown (AssertionFailedException) and
    * the current test is terminated
    *
    * @param o the reference to test
    */
  static public void assertNotNull(String failureMessage, Object o) {
      if (o==null) {
          throw new AssertionFailedException(failureMessage);
      }
   }

//    static private void failureNotification() {
//        failureNotification(null);
//    }

    static private void failureNotification(String message) {
        if (message == null) {
            throw new AssertionFailedException();
        } else {
            throw new AssertionFailedException(message);
        }
    }
}
