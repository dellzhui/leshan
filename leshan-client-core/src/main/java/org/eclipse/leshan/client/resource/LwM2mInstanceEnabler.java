/*******************************************************************************
 * Copyright (c) 2015 Sierra Wireless and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *     Kai Hudalla (Bosch Software Innovations GmbH) - add documentation
 *     Achim Kraus (Bosch Software Innovations GmbH) - add reset() for
 *                                                     REPLACE/UPDATE implementation
 *******************************************************************************/
package org.eclipse.leshan.client.resource;

import java.util.List;

import org.eclipse.leshan.client.LwM2mClient;
import org.eclipse.leshan.client.resource.listener.ResourceListener;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.Destroyable;
import org.eclipse.leshan.core.Startable;
import org.eclipse.leshan.core.Stoppable;
import org.eclipse.leshan.core.link.lwm2m.attributes.LwM2mAttributeSet;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mResourceInstance;
import org.eclipse.leshan.core.request.WriteRequest.Mode;
import org.eclipse.leshan.core.request.argument.Arguments;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteAttributesResponse;
import org.eclipse.leshan.core.response.WriteResponse;

/**
 * A contract for managing a LWM2M object instance on a LWM2M client.
 * <p>
 * LWM2M clients should implement this interface for each LWM2M object type they support in order to take advantage of
 * automatic routing of requests from a LWM2M server to specific LWM2M object instances of that type and forwarding of
 * notifications to LWM2M servers observing resources on these instances.
 * <p>
 * Clients can register instances of this interface (representing the client's instances of a particular LWM2M object
 * type) using {@link ObjectsInitializer#setInstancesForObject(int, LwM2mInstanceEnabler...)} and then use
 * {@link ObjectsInitializer#create(int)} to create a {@link LwM2mObjectEnabler} instance for managing them.
 * <p>
 * Implementations of this interface should adhere to the definition of the implemented LWM2M Object type regarding
 * acceptable resource IDs for the <code>read, write</code> and <code>execute</code> methods.
 * <p>
 * An instance that implements this interface synchronizes with the lifecycle of the LeshanClient. This means when
 * {@code LeshanClient#destroy()} is called, {@code LwM2mInstanceEnabler#destroy()} is also called if it implements the
 * {@link Destroyable} interface. And {@link Startable} ({@code #start()}) and {@link Stoppable} ({@code #stop()}) are
 * also same as this. If you need to restart the instance, please implement {@link Startable} with {@link Stoppable}
 * together.
 */
public interface LwM2mInstanceEnabler {

    /**
     * @return the id of this instance.
     */
    Integer getId();

    /**
     * Set this id of this instance. It should be called only by
     * {@link LwM2mInstanceEnablerFactory#create(ObjectModel, Integer, java.util.Collection)}.
     *
     * @param id this id of this instance.
     */
    void setId(int id);

    /**
     * Set the model of this instance. It should be called only by
     * {@link LwM2mInstanceEnablerFactory#create(ObjectModel, Integer, java.util.Collection)}.
     *
     * @param model the model of this instance
     */
    void setModel(ObjectModel model);

    /**
     * Set the lwm2mclient linked to this instance. It should only be called by {@link ObjectEnabler}.
     *
     * @param client the {@link LwM2mClient} which hold this instance.
     */
    void setLwM2mClient(LwM2mClient client);

    /**
     * Adds a callback handler that gets notified about changes to any of this LWM2M object instance's resources.
     *
     * @param listener the handler to add, a <code>null</code> value is silently ignored
     */
    void addResourceListener(ResourceListener listener);

    /**
     * Stops a callback handler from getting notified about changes to any of this LWM2M object instance's resources.
     *
     * @param listener the handler to remove, a <code>null</code> value is silently ignored
     */
    void removeResourceListener(ResourceListener listener);

    /**
     * Gets values of all readable resources of this instance.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     *
     * @return a success response with an {@link LwM2mObjectInstance} as content or a failure response with optional
     *         explanation message.
     */
    ReadResponse read(ServerIdentity identity);

    /**
     * Gets the current value of one of this LWM2M object instance's resources.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @param resourceId the ID of the resource to get the value of
     * @return the response object representing the outcome of the operation. An implementation should set the result's
     *         {@link ReadResponse#getCode() response code} to either reflect the success or reason for failure to
     *         retrieve the value.
     */
    ReadResponse read(ServerIdentity identity, int resourceId);

    /**
     * Gets the current value of one of this LWM2M object instance's resources instance.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @param resourceId the ID of the resource to get the value of
     * @param resourceInstance the ID of the resource instance to get the value of
     * @return the response object representing the outcome of the operation. An implementation should set the result's
     *         {@link ReadResponse#getCode() response code} to either reflect the success or reason for failure to
     *         retrieve the value.
     */
    ReadResponse read(ServerIdentity identity, int resourceId, int resourceInstance);

    /**
     * Sets all resources of this LWM2M object instance.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @param replace if replace is true a {@link Mode#REPLACE} should be done, else {@link Mode#UPDATE} should be done.
     * @param value all the resources to be written.
     * @return the response object representing the outcome of the operation. An implementation should set the result's
     *         {@link WriteResponse#getCode() response code} to either reflect the success or reason for failure to set
     *         the value.
     */
    WriteResponse write(ServerIdentity identity, boolean replace, LwM2mObjectInstance value);

    /**
     * Sets the value of one of this LWM2M object instance's resources.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @param replace If replace is true that means that the resource value completely replace the previous value. The
     *        behavior difference is only for Multiple resource. If replace is false the existing array of Resource
     *        Instances is updated meaning some Instances may be created or overwritten but not deleted.
     * @param resourceid the ID of the resource to set the value for
     * @param value the value to set the resource to
     * @return the response object representing the outcome of the operation. An implementation should set the result's
     *         {@link WriteResponse#getCode() response code} to either reflect the success or reason for failure to set
     *         the value.
     */
    WriteResponse write(ServerIdentity identity, boolean replace, int resourceid, LwM2mResource value);

    /**
     * Sets the value of one of this LWM2M object instance's resources instance.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @param addIfAbsent If resource instance does not already exist it must be addded only if addIfAbsent is True
     * @param resourceid the ID of the resource to set the value for
     * @param resourceInstance the ID of the resource instance to set the value of
     * @param value the value to set the resource instance to
     * @return the response object representing the outcome of the operation. An implementation should set the result's
     *         {@link WriteResponse#getCode() response code} to either reflect the success or reason for failure to set
     *         the value.
     */
    WriteResponse write(ServerIdentity identity, boolean addIfAbsent, int resourceid, int resourceInstance,
            LwM2mResourceInstance value);

    /**
     * Executes the operation represented by one of this LWM2M object instance's resources.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @param resourceid the ID of the resource to set the value for
     * @param arguments the input arguments of the operation
     * @return the response object representing the outcome of the operation. An implementation should set the result's
     *         {@link ExecuteResponse#getCode() response code} to either reflect the success or reason for failure to
     *         execute the operation.
     */
    ExecuteResponse execute(ServerIdentity identity, int resourceid, Arguments arguments);

    /**
     * Performs an observe register the whole LWM2M object instance.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @return a success response with an {@link LwM2mObjectInstance} as content or a failure response with optional
     *         explanation message.
     */
    ObserveResponse observe(ServerIdentity identity);

    /**
     * Performs an observe register on one of this LWM2M object instance's resources.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @param resourceid the ID of the resource to observe
     */
    ObserveResponse observe(ServerIdentity identity, int resourceid);

    /**
     * Performs an observe register on one of this LWM2M object instance's resource instance.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @param resourceid the ID of the resource to observe
     * @param resourceInstanceId the ID of the resource instance to observe
     */
    ObserveResponse observe(ServerIdentity identity, int resourceid, int resourceInstanceId);

    /**
     * Sets the attitubes of one of this LWM2M object instance's resources.
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     * @param resourceId the ID of the resource to get the value of
     * @return the response object representing the outcome of the operation. An implementation should set the result's
     *         {@link WriteAttributesResponse#getCode() response code} to either reflect the success or reason for
     *         failure to retrieve the value.
     */
    WriteAttributesResponse writeAttributes(ServerIdentity identity, int resourceId, LwM2mAttributeSet attributes);

    /**
     * A callback called when this instance is deleted
     *
     * @param identity the identity of the requester. This could be an internal call in this case
     *        <code> identity == ServerIdentity.SYSTEM</code>.
     */
    void onDelete(ServerIdentity identity);

    /**
     * @param objectModel the model of this instance
     * @return the list of the implemented resources of this instances mainly used for discover operation
     */
    List<Integer> getAvailableResourceIds(ObjectModel objectModel);

    /**
     * Reset the current value of one of this LWM2M object instance's resources. Only used for implementation of REPLACE
     * to cleanup none mandatory resources.
     *
     * @param resourceId the ID of the resource to be reseted
     */
    void reset(int resourceId);
}
