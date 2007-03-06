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
package org.objectweb.proactive.core.mop;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A place where static methods go
 */
public abstract class MOP {

    /**
     * The name of the interface that caracterizes all stub classes
     */
    protected static String STUB_OBJECT_INTERFACE_NAME = "org.objectweb.proactive.core.mop.StubObject";
    protected static Class<?> STUB_OBJECT_INTERFACE;
    static Logger logger = ProActiveLogger.getLogger(Loggers.MOP);

    /**
     * The root interface of all metabehaviors
     */

    //protected static String ROOT_INTERFACE_NAME = "org.objectweb.proactive.core.mop.Reflect";
    //protected static Class ROOT_INTERFACE;

    /**
     * Class array representing no parameters
     */
    protected static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    /**
     * Empty object array
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Class array representing (Constructor Call, Object[])
     */
    protected static Class[] PROXY_CONSTRUCTOR_PARAMETERS_TYPES_ARRAY = new Class[2];

    /**
     * A Hashtable to cache (reified class, stub class constructor) couples.
     */
    protected static java.util.Hashtable<GenericStubKey, Constructor> stubTable = new java.util.Hashtable<GenericStubKey, Constructor>();

    /**
     * A Hashtable to cache (proxy class, proxy class constructor) couples
     */
    protected static java.util.Hashtable<String, Constructor> proxyTable = new java.util.Hashtable<String, Constructor>();

    /**
     * A Hashtable to cache (Class name, proxy class name) couples
     * this is meant for class-based reification
     */
    protected static java.util.Hashtable secondProxyTable = new java.util.Hashtable();
    
    protected static MOPClassLoader singleton = MOPClassLoader.getMOPClassLoader(); //MOPClassLoader.createMOPClassLoader();

    /**
     *        As this class is center to the API, its static initializer is
     *        a good place to initialize general stuff.
     */
    protected static HashMap<String, Class<?>> loadedClass = new HashMap<String, Class<?>>();

    static {
        PROXY_CONSTRUCTOR_PARAMETERS_TYPES_ARRAY = new Class[] {
                org.objectweb.proactive.core.mop.ConstructorCall.class,
                EMPTY_OBJECT_ARRAY.getClass()
            };

        try {
            STUB_OBJECT_INTERFACE = forName(STUB_OBJECT_INTERFACE_NAME);
        } catch (ClassNotFoundException e) {
            throw new CannotFindClassException(STUB_OBJECT_INTERFACE_NAME);
        }

        //try {
        //  ROOT_INTERFACE = forName(ROOT_INTERFACE_NAME);
        //} catch (ClassNotFoundException e) {
        //  throw new CannotFindClassException(ROOT_INTERFACE_NAME);
        //}
    }

    /**
     * Loads a class using standard classloader or a hashtable
     * @param s the name of the class to fetch
     * @return the Class object representing class s
     */
    public static Class forName(String s)
        throws java.lang.ClassNotFoundException {
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            //                System.out.println(
            //                   "MOP forName failed for class " + s + ", looking in table");
            Class cl = loadedClass.get(s);

            //                  System.out.println("MOP forName failed, result is " +
            //                       cl);
            if (cl == null) {
                throw e;
            } else {
                return cl;
            }
        }
    }

    /**
     * Creates an instance of an object
     * @param nameOfClass The class to instantiate
     * @param genericParameters the types of the generic parameters for the class (if any, otherwise this parameter may be null) 
     * @param constructorParameters Array of the constructor's parameters [wrapper]
     * @param nameOfProxy The name of its proxy class
     * @param proxyParameters The array holding the proxy parameter
     */
    public static Object newInstance(String nameOfClass,
        Class[] genericParameters, Object[] constructorParameters,
        String nameOfProxy, Object[] proxyParameters)
        throws ClassNotFoundException, ClassNotReifiableException, 
            InvalidProxyClassException, 
            ConstructionOfProxyObjectFailedException, 
            ConstructionOfReifiedObjectFailedException {
        try {
            return newInstance(nameOfClass, nameOfClass, genericParameters,
                constructorParameters, nameOfProxy, proxyParameters);
        } catch (ReifiedCastException e) {
            throw new InternalException(e);
        }
    }

    public static Object newInstance(Class clazz,
        Object[] constructorParameters, String nameOfProxy,
        Object[] proxyParameters)
        throws ClassNotFoundException, ClassNotReifiableException, 
            InvalidProxyClassException, 
            ConstructionOfProxyObjectFailedException, 
            ConstructionOfReifiedObjectFailedException {
        try {
            return newInstance(clazz, clazz.getName(), null,
                constructorParameters, nameOfProxy, proxyParameters);
        } catch (ReifiedCastException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates an instance of an object
     * @param nameOfStubClass The name of the Stub class corresponding to the object
     * @param nameOfClass The class to instantiate
     * @param genericParameters the types of the generic parameters for the class (if any, otherwise this parameter may be null) 
     * @param constructorParameters Array of the constructor's parameters [wrapper]
     * @param nameOfProxy The name of its proxy class
     * @param proxyParameters The array holding the proxy parameter
     */
    public static Object newInstance(String nameOfStubClass,
        String nameOfClass, Class[] genericParameters, Object[] constructorParameters,
        String nameOfProxy, Object[] proxyParameters)
        throws ClassNotFoundException, ClassNotReifiableException, 
            ReifiedCastException, InvalidProxyClassException, 
            ConstructionOfProxyObjectFailedException, 
            ConstructionOfReifiedObjectFailedException {
        // For convenience, allows 'null' to be equivalent to an empty array
        if (constructorParameters == null) {
            constructorParameters = EMPTY_OBJECT_ARRAY;
        }
        if (proxyParameters == null) {
            proxyParameters = EMPTY_OBJECT_ARRAY;
        }

        // Throws a ClassNotFoundException
        Class targetClass = forName(nameOfClass);

        // Class stubClass = null;
        //        try {
        //            targetClass = forName(nameOfStubClass);
        //        } catch (ClassNotFoundException e) {
        //        	//if (targetClass.getClassLoader() != null) {
        //           // targetClass = targetClass.getClassLoader().loadClass(nameOfClass);
        //        	//} else {
        //        //		System.out.println("TargetClass  " + targetClass + " has null classloader");
        //        		
        //        //	}
        //            MOP.forName(nameOfClass);//   addClassToCache(nameOfStubClass, targetClass);
        //        }
        // Instanciates the stub object
        StubObject stub = createStubObject(nameOfStubClass, targetClass, genericParameters);

        // build the constructor call for the target object to create
        ConstructorCall reifiedCall = buildTargetObjectConstructorCall(targetClass,
                constructorParameters);

        // Instanciates the proxy object
        Proxy proxy = createProxyObject(nameOfProxy, proxyParameters,
                reifiedCall);

        // Connects the proxy to the stub
        stub.setProxy(proxy);
        return stub;
    }

    public static Object newInstance(Class stubClass, String nameOfClass,
        Class[] genericParameters, Object[] constructorParameters,
        String nameOfProxy, Object[] proxyParameters)
        throws ClassNotFoundException, ClassNotReifiableException, 
            ReifiedCastException, InvalidProxyClassException, 
            ConstructionOfProxyObjectFailedException, 
            ConstructionOfReifiedObjectFailedException {
        // For convenience, allows 'null' to be equivalent to an empty array
        if (constructorParameters == null) {
            constructorParameters = EMPTY_OBJECT_ARRAY;
        }
        if (proxyParameters == null) {
            proxyParameters = EMPTY_OBJECT_ARRAY;
        }

        // Throws a ClassNotFoundException
        Class targetClass = null; // forName(nameOfClass);

        //  Class stubClass = null;
        try {
            targetClass = forName(nameOfClass);
        } catch (ClassNotFoundException e) {
            if (stubClass.getClassLoader() != null) {
                targetClass = stubClass.getClassLoader().loadClass(nameOfClass);
            } else {
                logger.info("TargetClass  " + targetClass +
                    " has null classloader");
            }

            // MOP.forName(nameOfClass);//   addClassToCache(nameOfStubClass, targetClass);
        }

        // Instanciates the stub object
        StubObject stub = createStubObject(stubClass.getName(), targetClass, genericParameters);

        // build the constructor call for the target object to create
        ConstructorCall reifiedCall = buildTargetObjectConstructorCall(targetClass,
                constructorParameters);

        // Instanciates the proxy object
        Proxy proxy = createProxyObject(nameOfProxy, proxyParameters,
                reifiedCall);

        // Connects the proxy to the stub
        stub.setProxy(proxy);
        return stub;
    }

    /**
     * Creates an instance of an object
     * @param nameOfClass The class to instanciate
     * @param constructorParameters Array of the constructor's parameters [wrapper]
     * @param proxyParameters The array holding the proxy parameter
     *
         public static Object newInstance(String nameOfClass, Object[] constructorParameters, Object[] proxyParameters)
           throws
             ClassNotFoundException,
             ClassNotReifiableException,
             CannotGuessProxyNameException,
             InvalidProxyClassException,
             ConstructionOfProxyObjectFailedException,
             ConstructionOfReifiedObjectFailedException {
           String nameOfProxy = guessProxyName(forName(nameOfClass));
           return newInstance(nameOfClass, constructorParameters, nameOfProxy, proxyParameters);
         }*/
    /**
     * Reifies an object
     * @param proxyParameters Array holding the proxy parameters
     * @param target the object to reify
     *
         public static Object turnReified(Object[] proxyParameters, Object target)
           throws ClassNotReifiableException, CannotGuessProxyNameException, InvalidProxyClassException, ConstructionOfProxyObjectFailedException {
           try {
             return turnReified(guessProxyName(target.getClass()), proxyParameters, target);
           } catch (ClassNotFoundException e) {
             throw new CannotGuessProxyNameException();
           }
         }*/
    /**
     * Reifies an object
     * @param nameOfProxyClass the name of the object's proxy
     * @param proxyParameters Array holding the proxy parameters
     * @param target the object to reify
     * @param genericParameters      * @param genericParameters the types of the generic parameters for the class (if any, otherwise this parameter may be null) 
     */
    public static Object turnReified(String nameOfProxyClass, 
        Object[] proxyParameters, Object target, Class[] genericParameters)
        throws ClassNotFoundException, ClassNotReifiableException, 
            InvalidProxyClassException, 
            ConstructionOfProxyObjectFailedException {
        try {
            return turnReified(target.getClass().getName(), nameOfProxyClass,
                proxyParameters, target, genericParameters);
            //	 return turnReifiedFAb(target.getClass(), nameOfProxyClass, proxyParameters, target);
        } catch (ReifiedCastException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Reifies an object
     * @param proxyParameters Array holding the proxy parameters
     * @param nameOfStubClass The name of the object's stub class
     * @param target the object to reify
     *
         public static Object turnReified(Object[] proxyParameters, String nameOfStubClass, Object target)
           throws
             ClassNotFoundException,
             ReifiedCastException,
             ClassNotReifiableException,
             CannotGuessProxyNameException,
             InvalidProxyClassException,
             ConstructionOfProxyObjectFailedException {
           String nameOfProxy = guessProxyName(target.getClass());
           return turnReified(nameOfStubClass, nameOfProxy, proxyParameters, target);
         }*/
    /**
     * Reifies an object
     * @param nameOfProxyClass the name of the object's proxy
     * @param nameOfStubClass The name of the object's stub class
     * @param proxyParameters Array holding the proxy parameters
     * @param target the object to reify
     */
    public static Object turnReified(String nameOfStubClass,
        String nameOfProxyClass, Object[] proxyParameters, Object target, Class[] genericParameters)
        throws ClassNotFoundException, ReifiedCastException, 
            ClassNotReifiableException, InvalidProxyClassException, 
            ConstructionOfProxyObjectFailedException {
        // For convenience, allows 'null' to be equivalent to an empty array
        // System.out.println("MOP.turnReified");
        if (proxyParameters == null) {
            proxyParameters = EMPTY_OBJECT_ARRAY;
        }

        // Throws a ClassNotFoundException
        Class targetClass = target.getClass();

        // Instanciates the stub object
        StubObject stub = createStubObject(nameOfStubClass, targetClass, genericParameters);

        // First, build the FakeConstructorCall object to pass to the constructor
        // of the proxy Object
        // FakeConstructorCall fakes a ConstructorCall object by returning
        // an already-existing object as the result of its execution
        ConstructorCall reifiedCall = new FakeConstructorCall(target);

        // Instanciates the proxy object
        Proxy proxy = createProxyObject(nameOfProxyClass, proxyParameters,
                reifiedCall);

        // Connects the proxy to the stub
        stub.setProxy(proxy);
        return stub;
    }

    //  public static Object turnReifiedFAb(Class targetClass, String nameOfProxyClass, Object[] proxyParameters, Object target)
    //	throws ClassNotFoundException, ReifiedCastException, ClassNotReifiableException, InvalidProxyClassException, ConstructionOfProxyObjectFailedException {
    //	 For convenience, allows 'null' to be equivalent to an empty array
    //   System.out.println("MOP.turnReified");
    //  System.out.println("turnReifiedFAb");
    //	if (proxyParameters == null)
    //	  proxyParameters = EMPTY_OBJECT_ARRAY;
    //	 Throws a ClassNotFoundException
    //	Class targetClass = target.getClass();
    //	 Instanciates the stub object
    //	StubObject stub = createStubObjectFAb(targetClass);
    //	 First, build the FakeConstructorCall object to pass to the constructor
    //	 of the proxy Object
    //	 FakeConstructorCall fakes a ConstructorCall object by returning
    //	 an already-existing object as the result of its execution
    //	ConstructorCall reifiedCall = new FakeConstructorCall(target);
    //	 Instanciates the proxy object
    //	Proxy proxy = createProxyObject(nameOfProxyClass, proxyParameters, reifiedCall);
    //	 Connects the proxy to the stub
    //	stub.setProxy(proxy);
    //	return stub;
    //  }

    /**
     * Checks if a stub class can be created for the class <code>cl</code>.
     *
     * A class cannot be reified if at least one of the following conditions are
     *  met : <UL>
     * <LI>This <code>Class</code> objects represents a primitive type
     * (except void)
     * <LI>The class is <code>final</code>
     * <LI>There is an ambiguity in constructors signatures
     * <LI>There is no noargs constructor
     * </UL>
     *
     * @author Julien Vayssi?re, INRIA
     * @param cl Class to be checked
     * @return <code>true</code> is the class exists and can be reified,
     *  <code>false</code> otherwise.
     */
    static void checkClassIsReifiable(String className)
        throws ClassNotReifiableException, ClassNotFoundException {
        checkClassIsReifiable(forName(className));
    }

    public static void checkClassIsReifiable(Class cl)
        throws ClassNotReifiableException {
        int mods = cl.getModifiers();
        if (cl.isInterface()) {
            // Interfaces are always reifiable, although some of the methods
            // they contain may not be reifiable
            return;
        } else {
            // normal case, this is a class
            if (cl.isPrimitive()) {
                throw new ClassNotReifiableException(
                    "Cannot reify primitive types: " + cl.getName());
            } else if (Modifier.isFinal(mods)) {
                throw new ClassNotReifiableException(
                    "Cannot reify final classes: " + cl.getName());
            } else if (!(checkNoArgsConstructor(cl))) {
                throw new ClassNotReifiableException("Class " + cl.getName() +
                    " needs to have an empty noarg constructor.");
            } else {
                return;
            }
        }
    }

    /**
     * Checks if class <code>c</code> has a noargs constructor
     */
    protected static boolean checkNoArgsConstructor(Class cl) {
        try {
            cl.getConstructor(EMPTY_CLASS_ARRAY);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Checks if an object is a stub object
     *
     * Being a stub object is equivalent to implementing the StubObject
     * interface
     *
     * @param o the object to check
     * @return <code>true</code> if it is a stub object, <code>false</code>
     * otherwise */
    public static boolean isReifiedObject(Object o) {
        if (o != null) {
            return (STUB_OBJECT_INTERFACE.isAssignableFrom(o.getClass()));
        } else {
            return false;
        }
    }

    /**
     * Creates a stub class for the specified class
     * @param nameOfBaseClass The name of the class
     * @return A class object representing the class, or NULL if failed
     */
    private static Class createStubClass(String nameOfBaseClass, Class[] genericParameters) {
        try {
            //return Class.forName(Utils.convertClassNameToStubClassName(nameOfClass), true, singleton);
            return singleton.loadClass(Utils.convertClassNameToStubClassName(
                    nameOfBaseClass, genericParameters));
        } catch (ClassNotFoundException e) {
            throw new GenerationOfStubClassFailedException(
                "Cannot create the Stub class : " +
                Utils.convertClassNameToStubClassName(nameOfBaseClass, genericParameters) +
                "\nThe class \"" + nameOfBaseClass +
                "\" must have a public access ");
        }
    }

    private static Class createStubClass(String nameOfClass, Class[] genericParameters, ClassLoader cl) {
        try {
            //return Class.forName(Utils.convertClassNameToStubClassName(nameOfClass), true, singleton);
            return singleton.loadClass(Utils.convertClassNameToStubClassName(
                    nameOfClass, genericParameters), genericParameters, cl);
        } catch (ClassNotFoundException e) {
            throw new GenerationOfStubClassFailedException(
                "Cannot load Stub class : " +
                Utils.convertClassNameToStubClassName(nameOfClass, genericParameters));
        }
    }

    /**
     * Finds the Stub Constructor for a specified class
     * @param nameOfClass the name of the class
     * @return The Constructor object.
     * @throws ClassNotFoundException if the class cannot be located
     */
    static Constructor findStubConstructor(String nameOfClass, Class[] genericParameters)
        throws ClassNotFoundException {
        return findStubConstructor(forName(nameOfClass), genericParameters);
    }

    /**
     * Finds the Stub Constructor for a specified class
     * @param targetClass the representation of the class
     * @return The Constructor object.
     */
    private static Constructor findStubConstructor(Class targetClass, Class[] genericParameters) {
        Constructor stubConstructor;
        String nameOfClass = targetClass.getName();

        // Is it cached in Hashtable ?
        stubConstructor = (Constructor) stubTable.get(new GenericStubKey(nameOfClass, genericParameters));

        //System.out.println("xxxxxx targetClass is " + targetClass);
        // On cache miss, finds the constructor
        
        
        if (stubConstructor == null) {
            Class stubClass;
            try {
                stubClass = forName(Utils.convertClassNameToStubClassName(
                            nameOfClass, genericParameters));
            } catch (ClassNotFoundException e) {
                // No stub class can be found, let's create it from scratch
                stubClass = createStubClass(nameOfClass, genericParameters,
                        targetClass.getClassLoader());
                //                stubClass = createStubClass(nameOfClass,
                //          targetClass.getClassLoader());
            }

            // Verifies that the stub has a noargs constructor and caches it
            try {
                stubConstructor = stubClass.getConstructor(EMPTY_CLASS_ARRAY);
                stubTable.put(new GenericStubKey(nameOfClass, genericParameters), stubConstructor);
            } catch (NoSuchMethodException e) {
                throw new GenerationOfStubClassFailedException(
                    "Stub for class " + nameOfClass +
                    "has no noargs constructor. This is a bug in ProActive.");
            }
        }
        return stubConstructor;
    }

    /**
     * Finds the Constructor of the proxy for a specified class
     * @param proxyClass The represenation of the proxy
     * @return the Constructor
     * @throws InvalidProxyClassException If the class is not a valid Proxy
     */
    private static Constructor findProxyConstructor(Class proxyClass)
        throws InvalidProxyClassException {
        Constructor proxyConstructor;

        // Localizes the proxy class constructor
        proxyConstructor = proxyTable.get(proxyClass.getName());

        //System.out.println("MOP: The class of the proxy is " + proxyClass.getName());
        // Cache miss
        if (proxyConstructor == null) {
            try {
                proxyConstructor = proxyClass.getConstructor(PROXY_CONSTRUCTOR_PARAMETERS_TYPES_ARRAY);
                proxyTable.put(proxyClass.getName(), proxyConstructor);
            } catch (NoSuchMethodException e) {
                throw new InvalidProxyClassException(
                    "No constructor matching (ConstructorCall, Object[]) found in proxy class " +
                    proxyClass.getName());
            }
        }
        return proxyConstructor;
    }

    private static StubObject instantiateStubObject(Constructor stubConstructor)
        throws ConstructionOfStubObjectFailedException {
        try {
            Object o = stubConstructor.newInstance(EMPTY_OBJECT_ARRAY);
            return (StubObject) o;
        } catch (InstantiationException e) {
            throw new ConstructionOfStubObjectFailedException("Constructor " +
                stubConstructor + " belongs to an abstract class.");
        } catch (IllegalArgumentException e) {
            throw new ConstructionOfStubObjectFailedException(
                "Wrapping problem with constructor " + stubConstructor);
        } catch (IllegalAccessException e) {
            throw new ConstructionOfStubObjectFailedException(
                "Access denied to constructor " + stubConstructor);
        } catch (InvocationTargetException e) {
            throw new ConstructionOfStubObjectFailedException("The constructor of the stub has thrown an exception: ",
                e.getTargetException());
        }
    }

    private static StubObject createStubObject(String nameOfBaseClass, 
        Class targetClass, Class[] genericParameters)
        throws ClassNotFoundException, ReifiedCastException, 
            ClassNotReifiableException {
        //System.out.println("StubClass is " + nameOfBaseClass);
        // BUG ID: #327
        //this has been added to deal with downloaded classes
        //if we cannot load the stub class using its name
        //it is probably because it has been downloaded by another classloader
        //thus we ask the classloader of the target class to load it
        Class<?> baseClass = null;
        try {
            baseClass = forName(nameOfBaseClass);
        } catch (ClassNotFoundException e) {
            baseClass = targetClass.getClassLoader().loadClass(nameOfBaseClass);
            MOP.addClassToCache(nameOfBaseClass, baseClass);
        }

        // Class stubClass = forName(nameOfStubClass,targetClass.getClassLoader());
        // Check that the type of the class is compatible with the type of the stub
        if (!(baseClass.isAssignableFrom(targetClass))) {
            throw new ReifiedCastException("Cannot convert " +
                targetClass.getName() + "into " + baseClass.getName());
        }

        // Throws a ClassNotReifiableException exception if not reifiable
        checkClassIsReifiable(baseClass);

        // Finds the constructor of the stub class
        // If the stub class has not yet been created,
        // it is created within this call
        Constructor stubConstructor = findStubConstructor(baseClass, genericParameters);

        // Instanciates the stub object
        return instantiateStubObject(stubConstructor);
    }

    // BUG ID: #327
    protected static void addClassToCache(String name, Class cl) {
        //        System.out.println("MOP: puting " + nameOfStubClass +
        //            " in loadedClass");
        //        loadedClass.put(nameOfStubClass, stubClass);
        //        Field[] clArray = stubClass.getDeclaredFields();
        //        System.out.println("MOP: nuumber of declared classes " +
        //            clArray.length);
        //        for (int i = 0; i < clArray.length; i++) {
        //            Field ob1 = clArray[i];
        //            System.out.println("MOP: field " + ob1.getName());
        //            Class cl = ob1.getType();
        //            System.out.println("MOP: key = " + cl.getName() + " value =  " +
        //                cl);
        //            loadedClass.put(cl.getName(), cl);
        //        }
        loadedClass.put(name, cl);
    }

    // Instanciates the proxy object
    public static Proxy createProxyObject(String nameOfProxy,
        Object[] proxyParameters, ConstructorCall reifiedCall)
        throws ConstructionOfProxyObjectFailedException, ClassNotFoundException, 
            InvalidProxyClassException {
        // Throws a ClassNotFoundException
        Class proxyClass = forName(nameOfProxy);

        // Finds constructor of the proxy class
        Constructor proxyConstructor = findProxyConstructor(proxyClass);

        // Now calls the constructor of the proxy
        Object[] params = new Object[] { reifiedCall, proxyParameters };
        try {
            return (Proxy) proxyConstructor.newInstance(params);
        } catch (InstantiationException e) {
            throw new ConstructionOfProxyObjectFailedException("Constructor " +
                proxyConstructor + " belongs to an abstract class");
        } catch (IllegalArgumentException e) {
            throw new ConstructionOfProxyObjectFailedException(
                "Wrapping problem with constructor " + proxyConstructor);
        } catch (IllegalAccessException e) {
            throw new ConstructionOfProxyObjectFailedException(
                "Access denied to constructor " + proxyConstructor);
        } catch (InvocationTargetException e) {
            throw new ConstructionOfProxyObjectFailedException("The constructor of the proxy object has thrown an exception: ",
                e.getTargetException());
        }
    }

    public static ConstructorCall buildTargetObjectConstructorCall(
        Class targetClass, Object[] constructorParameters)
        throws ConstructionOfReifiedObjectFailedException {
        // First, build the ConstructorCall object to pass to the constructor
        // of the proxy Object. It represents the construction of the reified
        // object.
        Constructor targetConstructor;

        // Locates the right constructor (should use a cache here ?)
        Class[] targetConstructorArgs = new Class[constructorParameters.length];
        for (int i = 0; i < constructorParameters.length; i++) {
            //	System.out.println("MOP: constructorParameters[i] = " + constructorParameters[i]);
            targetConstructorArgs[i] = constructorParameters[i].getClass();
            //	System.out.println("MOP: targetConstructorArgs[i] = " + targetConstructorArgs[i]);
        }

        //System.out.println("MOP: targetClass is " + targetClass);
        //	System.out.println("MOP: targetConstructorArgs = " + targetConstructorArgs);
        //	System.out.println("MOP: targetConstructorArgs.length = " + targetConstructorArgs.length);
        try {
            //MODIFIED 4/5/00
            if (targetClass.isInterface()) {
                //there is no point in looking for the constructor of an interface
                //	System.out.println("MOP: WARNING Interface detected");
                targetConstructor = null;
            } else {
                targetConstructor = targetClass.getDeclaredConstructor(targetConstructorArgs);
            }
        } catch (NoSuchMethodException e) {
            // This may have failed because getConstructor does not allow subtypes
            targetConstructor = findReifiedConstructor(targetClass,
                    targetConstructorArgs);

            if (targetConstructor == null) // This may have failed because some wrappers should be interpreted
            // as primitive types. Let's investigate it
             {
                targetConstructor = investigateAmbiguity(targetClass,
                        targetConstructorArgs);
                if (targetConstructor == null) {
                    throw new ConstructionOfReifiedObjectFailedException(
                        "Cannot locate this constructor in class " +
                        targetClass + " : " + targetConstructorArgs);
                }
            }
        }
        return new ConstructorCallImpl(targetConstructor, constructorParameters);
    }

    /**
     * Try to guess the name of the proxy for a specified class
     * @param targetClass the source class
     * @return the name of the proxy class
     * @throws CannotGuessProxyNameException If the MOP cannot guess the name of the proxy
     *
         private static String guessProxyName(Class targetClass) throws CannotGuessProxyNameException {
           int i;
           Class cl;
           Class myInterface = null;
           Class[] interfaces;
           Field myField = null;
           // Checks the cache
           String nameOfProxy = (String) secondProxyTable.get(targetClass.getName());
           if (nameOfProxy == null) {
             Class currentClass;
             // Checks if this class or any of its superclasses implements an
             //  interface that is a subinterface of ROOT_INTERFACE
             currentClass = targetClass;
             //System.out.println("MOP: guessProxyName for targetClass " + targetClass);
             while ((currentClass != null) && (myInterface == null)) {
               boolean multipleMatches = false;
               interfaces = currentClass.getInterfaces();
               for (i = 0; i < interfaces.length; i++) {
                 if (ROOT_INTERFACE.isAssignableFrom(interfaces[i])) {
                   if (multipleMatches == false) {
                     myInterface = interfaces[i];
                     multipleMatches = true;
                   } else {
                     // There are multiple interfaces in the current class
                     // that inherit from ROOT_INTERFACE.
                     System.err.println(
                       "More than one interfaces declared in class " + currentClass.getName() + " inherit from " + ROOT_INTERFACE + ". Using " + myInterface);
                   }
                 }
               }
               currentClass = currentClass.getSuperclass();
             }
             if (myInterface == null) {
               throw new CannotGuessProxyNameException(
                 "Class " + targetClass.getName() + " does not implement any interface that inherits from org.objectweb.proactive.core.mop.Reflect");
             }
             // Now look for the PROXY_CLASS_NAME field in this interface
             try {
               myField = myInterface.getField("PROXY_CLASS_NAME");
             } catch (NoSuchFieldException e) {
               throw new CannotGuessProxyNameException("No field PROXY_CLASS_NAME in interface " + myInterface);
             }
             try {
               nameOfProxy = (String) myField.get(null);
             } catch (IllegalAccessException e) {
               throw new CannotGuessProxyNameException("Cannot access field PROXY_CLASS_NAME in interface " + myInterface);
             }
             secondProxyTable.put(targetClass.getName(), nameOfProxy);
           }
           return nameOfProxy;
         }*/
    /**
     * Tries to solve ambiguity problems in constructors
     * @param targetClass the class
     * @param targetConstructorArgs The arguments which will determine wich constructor is to be used
     * @return The corresponding Constructor
     */
    private static Constructor investigateAmbiguity(Class targetClass,
        Class[] targetConstructorArgs) {
        // Find the number of possible constructors ambiguities
        int n = 1;
        for (int i = 0; i < targetConstructorArgs.length; i++) {
            if (Utils.isWrapperClass(targetConstructorArgs[i])) {
                n = n * 2;
            }
        }
        if (n == 1) {
            return null; // No wrapper found
        }

        for (int i = 0; i < targetConstructorArgs.length; i++) {
            if (Utils.isWrapperClass(targetConstructorArgs[i])) {
                targetConstructorArgs[i] = Utils.getPrimitiveType(targetConstructorArgs[i]);
            }
        }
        return findReifiedConstructor(targetClass, targetConstructorArgs);
    }

    /**
     * Finds the reified constructor
     * @param targetClass The class
     * @param the effective arguments
     * @return The constructor
     */
    private static Constructor findReifiedConstructor(Class targetClass,
        Class<?>[] targetConstructorArgs) {
        Constructor[] publicConstructors;
        Constructor currentConstructor;
        Class<?>[] currentConstructorParameterTypes;
        boolean match;

        publicConstructors = targetClass.getConstructors();
        // For each public constructor of the reified class
        for (int i = 0; i < publicConstructors.length; i++) {
            currentConstructor = publicConstructors[i];
            currentConstructorParameterTypes = currentConstructor.getParameterTypes();
            match = true;

            // Check if the parameters types of this constructor are
            // assignable from the actual parameter types.
            if (currentConstructorParameterTypes.length == targetConstructorArgs.length) {
                for (int j = 0; j < currentConstructorParameterTypes.length;
                        j++) {
                    if (!(currentConstructorParameterTypes[j].isAssignableFrom(
                                targetConstructorArgs[j]))) {
                        match = false;
                        break;
                    }
                }
            } else {
                match = false;
            }
            if (match == true) {
                return currentConstructor;
            }
        }
        return null;
    }

    /**
     * Dynamic cast
     * @param sourceObject The source object
     * @param targetTypeName the destination class
     * @return The resulting object
     * @throws ReifiedCastException if the class cast is invalid
     */
    private static Object castInto(Object sourceObject, String targetTypeName)
        throws ReifiedCastException {
        try {
            Class cl = forName(targetTypeName);
            return castInto(sourceObject, cl, null);
        } catch (ClassNotFoundException e) {
            throw new ReifiedCastException("Cannot load class " +
                targetTypeName);
            //		throw new ReifiedCastException ("Cannot cast "+sourceObject.getClass().getName()+" into "+targetTypeName);
        }
    }

    /**
     * Dynamic cast
     * @param sourceObject The source object
     * @param targetType the destination class
     * @param genericParameters TODO
     * @return The resulting object
     * @throws ReifiedCastException if the class cast is invalid
     */
    private static Object castInto(Object sourceObject, Class<?> targetType, Class[] genericParameters)
        throws ReifiedCastException {
        // First, check if sourceObject is a reified object
        if (!(isReifiedObject(sourceObject))) {
            throw new ReifiedCastException(
                "Cannot perform a reified cast on an object that is not reified");
        }

        // Gets a Class object representing the type of sourceObject
        Class<?> sourceType = sourceObject.getClass().getSuperclass();

        // Check if types are compatible
        // Here we assume that the 'type of the stub' (i.e, the type of the
        // reified object) is its direct superclass
        if (!((sourceType.isAssignableFrom(targetType)) ||
                (targetType.isAssignableFrom(sourceType)))) {
            throw new ReifiedCastException("Cannot cast " +
                sourceObject.getClass().getName() + " into " +
                targetType.getName());
        }

        // Let's create a stub object for the target type
        Constructor stubConstructor = findStubConstructor(targetType, genericParameters);

        // Instanciates the stub object
        StubObject stub = instantiateStubObject(stubConstructor);

        // Connects the proxy of the old stub to the new stub
        stub.setProxy(((StubObject) sourceObject).getProxy());
        return stub;
    }

    public static Class loadClass(String name) throws ClassNotFoundException {
        //return singleton.loadClass(name);
        return forName(name);
    }
    
    
    static class GenericStubKey {
    	
    	String className;
    	Class[] genericParameters;
    	
    	public GenericStubKey(String className, Class[] genericParameters) {
    		this.className = className;
    		this.genericParameters = genericParameters;
    	}
    	
    	
    	
    	public boolean equals(Object o) {
    		if (! (o instanceof GenericStubKey)) {
    			return false;
    		}
    		// className cannot be null
    		return (className.equals(((GenericStubKey)o).getClassName()) 
    				&& (Arrays.equals(genericParameters, ((GenericStubKey)o).getGenericParameters())));
    	}
    	
    	public int hashCode() {
    		return className.hashCode()+Arrays.deepHashCode(genericParameters);
    	}



		public String getClassName() {
			return className;
		}



		public Class[] getGenericParameters() {
			return genericParameters;
		}



    }
}
