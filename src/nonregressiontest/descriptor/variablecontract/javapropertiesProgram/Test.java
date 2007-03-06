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
package nonregressiontest.descriptor.variablecontract.javapropertiesProgram;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;

import testsuite.test.Assertions;
import testsuite.test.FunctionalTest;

public class Test extends FunctionalTest {
	static final long serialVersionUID = 1;

	private static String XML_LOCATION = Test.class.getResource(
			"/nonregressiontest/descriptor/variablecontract/javapropertiesProgram/Test.xml").getPath();
	ProActiveDescriptor pad;
	
	boolean bogusFromProgram, bogusFromDescriptor;
	
	public Test() {
		super("Variable Contract: JavaPropertiesProgram",
				"Tests conditions for variables of type JavaPropertiesProgram");
	}

	public boolean postConditions() throws Exception {
	
		return true;
	}

	public void initTest() throws Exception {
		bogusFromDescriptor=true;
		bogusFromProgram=true;
	}
	
	public void endTest() throws Exception {
		
		if (pad != null) {
			pad.killall(false);
		}
	}

	public void action() throws Exception {
		
		VariableContract variableContract= new VariableContract();
		
		//Setting from Program
		HashMap map = new HashMap();
		map.put("user.home", "/home/userprogram");
		variableContract.setVariableFromProgram(map, VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
		Assertions.assertTrue( variableContract.getValue("user.home").equals(System.getProperty("user.home")));
		
		boolean bogus=false;
		try{
			variableContract.setVariableFromProgram("bogus.property", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
			bogus=true; //shouldn't reach this line
		}catch(Exception e){}
		Assertions.assertTrue(!bogus);
		
		variableContract.setVariableFromProgram("bogus.property", "bogus_value", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
		Assertions.assertTrue( variableContract.getValue("bogus.property").equals("bogus_value"));
		 
		
		//Setting from Descriptor
		variableContract.setDescriptorVariable("user.home", "/home/userdesc", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
		Assertions.assertTrue( variableContract.getValue("user.home").equals(System.getProperty("user.home")));
		
		try{
			bogus=false;
			variableContract.setDescriptorVariable("${ilegal.var.name}", "ilegalvariablename", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
			bogus=true; //shouldn't reach this line
		}catch(Exception e){}
		Assertions.assertTrue(!bogus);
		
		//Setting bogus from program
		variableContract.setDescriptorVariable("bogus.property", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
		Assertions.assertTrue( variableContract.getValue("bogus.property").equals("bogus_value"));

		pad = ProActive.getProactiveDescriptor(XML_LOCATION, variableContract);
		variableContract = pad.getVariableContract();
		variableContract.getValue("user.home").equals(System.getProperty("user.home"));
		
		//Empty value in descriptor should have less priority, and not set to empty
		
		Assertions.assertTrue(variableContract.isClosed());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Test test = new Test();
		try {
			System.out.println("InitTest");
			test.initTest();
			System.out.println("Action");
			test.action();
			System.out.println("postConditions");
			System.out.println("Result="+test.postConditions());
			System.out.println("endTest");
			test.endTest();
			System.out.println("The end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
