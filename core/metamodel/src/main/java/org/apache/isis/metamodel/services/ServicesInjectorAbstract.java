/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.metamodel.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.lang.CastUtils;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.exceptions.ReflectionException;


public abstract class ServicesInjectorAbstract implements ServicesInjector {

    private static final Logger LOG = Logger.getLogger(ServicesInjectorAbstract.class);

    private DomainObjectContainer container;
    private final List<Object> services = new ArrayList<Object>();

    /**
     * Ensure that all services are wired into each other.
     */
    public void open() {
        autowireServicesAndContainer();
    }

    public void close() {}

    // /////////////////////////////////////////////////////////
    // Container
    // /////////////////////////////////////////////////////////

    public DomainObjectContainer getContainer() {
        return container;
    }

    public void setContainer(final DomainObjectContainer container) {
        ensureThatArg(container, is(not(nullValue())));
        this.container = container;
    }

    // /////////////////////////////////////////////////////////
    // Services
    // /////////////////////////////////////////////////////////

    public void setServices(final List<Object> services) {
        this.services.clear();
        addServices(services);
        autowireServicesAndContainer();
    }

    public List<Object> getRegisteredServices() {
        return Collections.unmodifiableList(services);
    }

    private void addServices(final List<Object> services) {
        for (final Object service : services) {
            if (service instanceof List) {
                final List<Object> serviceList = CastUtils.listOf(service, Object.class);
                addServices(serviceList);
            } else {
                addService(service);
            }
        }
    }

    private boolean addService(final Object service) {
        return services.add(service);
    }

    // /////////////////////////////////////////////////////////
    // Inject Dependencies
    // /////////////////////////////////////////////////////////

    public void injectDependencies(final Object object) {
        Assert.assertNotNull("no container", container);
        Assert.assertNotNull("no services", services);
        
        ArrayList<Object> servicesCopy = new ArrayList<Object>(services);
        servicesCopy.add(container);
        injectServices(object, servicesCopy);
    }

    public void injectDependencies(final List<Object> objects) {
        for (final Object object : objects) {
            injectDependencies(object);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // injectInto
    // ////////////////////////////////////////////////////////////////////

    /**
     * That is, injecting this injector...
     */
    public void injectInto(Object candidate) {
        if (ServicesInjectorAware.class.isAssignableFrom(candidate.getClass())) {
            ServicesInjectorAware cast = ServicesInjectorAware.class.cast(candidate);
            cast.setServicesInjector(this);
        }
    }

    // /////////////////////////////////////////////////////////
    // Helpers
    // /////////////////////////////////////////////////////////

    private static void injectServices(final Object object, final List<Object> services) {
        final Class<?> cls = object.getClass();
        for (final Object service : services) {
            final Class<?> serviceClass = service.getClass();
            final Method[] methods = cls.getMethods();
            for (int j = 0; j < methods.length; j++) {
                if (!methods[j].getName().startsWith("set")) {
                    continue;
                }
                final Class<?>[] parameterTypes = methods[j].getParameterTypes();
                if (parameterTypes.length != 1 || parameterTypes[0] == Object.class
                        || !parameterTypes[0].isAssignableFrom(serviceClass)) {
                    continue;
                }
                
                methods[j].setAccessible(true);
                
                invokeSetMethod(methods[j], object, service);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("injected service " + service + " into " + new ToString(object));
                }
            }
        }
    }

    private static void invokeMethod(final Method method, final Object target, final Object[] parameters) {
        try {
            method.invoke(target, parameters);
        } catch (final SecurityException e) {
            throw new ReflectionException(String.format("Cannot access the %s method in %s", method.getName(), target.getClass()
                    .getName()));
        } catch (final IllegalArgumentException e1) {
            throw new ReflectionException(e1);
        } catch (final IllegalAccessException e1) {
            throw new ReflectionException(String.format("Cannot access the %s method in %s", method.getName(), target.getClass()
                    .getName()));
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            } else {
                throw new ReflectionException(targetException);
            }
        }
    }

    private static void invokeSetMethod(final Method set, final Object target, final Object parameter) {
        final Object[] parameters = new Object[] { parameter };
        invokeMethod(set, target, parameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("injected " + parameter + " into " + new ToString(target));
        }
    }

    private void autowireServicesAndContainer() {
        injectDependencies(this.services);
        injectDependencies(this.container);
    }

}

