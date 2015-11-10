/*
 * ETRI@copy 2015 all rights reserved. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package kr.re.etri.tsdn.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloRuntimeMXBean;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorld;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloProvider implements BindingAwareProvider, AutoCloseable, HelloRuntimeMXBean {

    private static final Logger LOG = LoggerFactory.getLogger(HelloProvider.class);
    private DataBroker db;
    private NotificationProviderService notificationService;
    private ListenerRegistration<org.opendaylight.yangtools.yang.binding.NotificationListener> listenerRegistration;

    @Override
    public void onSessionInitiated(ProviderContext session) {
    	HelloWorldImpl helloWorldImpl = new HelloWorldImpl();

		session.addRpcImplementation(HelloService.class, helloWorldImpl);
		db = session.getSALService(DataBroker.class);
		notificationService = session.getSALService(NotificationProviderService.class);
		helloWorldImpl.setDB(db);
		helloWorldImpl.setNotificationService(notificationService);

        listenerRegistration = notificationService.registerNotificationListener(helloWorldImpl);

        final InstanceIdentifier<HelloWorld> path = helloWorldImpl.HELLO_IID;
        final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration =
                        db.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                                        path, helloWorldImpl, DataChangeScope.ONE);

        LOG.info("HelloProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        LOG.info("HelloProvider Closed");
    }

	@Override
	public void updateSbNode(String id) {
		// TODO Auto-generated method stub
		LOG.info("jmx updateSbNode called");
	}

}