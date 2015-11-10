package kr.re.etri.tsdn.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorld;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldReadInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldReadOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldReadOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldWriteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldWriteOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldWriteOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.MultipleOfTens;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.MultipleOfTensBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;

public class HelloWorldImpl implements HelloService, DataChangeListener, HelloListener {

	private static final Logger LOG = LoggerFactory.getLogger(HelloWorldImpl.class);
	public static final InstanceIdentifier<HelloWorld> HELLO_IID = InstanceIdentifier.builder(HelloWorld.class).build();
	private DataBroker db;
	private long helloCounter;
	private NotificationProviderService notificationService;

	@Override
	public Future<RpcResult<HelloWorldOutput>> helloWorld(HelloWorldInput input) {
		// TODO Auto-generated method stub

		HelloWorldOutputBuilder helloBuilder = new HelloWorldOutputBuilder();
		helloBuilder.setGreating("Greetings " + input.getStrin() + "!");

		return RpcResultBuilder.success(helloBuilder.build()).buildFuture();
	}

	@Override
	public Future<RpcResult<Void>> noinputOutput() {
		// TODO Auto-generated method stub
		LOG.info("noinputoutput called - labry :-D");
		return Futures.immediateFuture( RpcResultBuilder.<Void> success().build() );
	}

    @Override
    public Future<RpcResult<HelloWorldReadOutput>> helloWorldRead(HelloWorldReadInput input) {
            final ReadWriteTransaction tx = db.newReadWriteTransaction();

            Future<Optional<HelloWorld>> readFuture = tx.read(LogicalDatastoreType.OPERATIONAL, HELLO_IID);

            HelloWorldReadOutputBuilder helloReadBuilder = new HelloWorldReadOutputBuilder();
            try {
                    helloReadBuilder.setStrout(input.getStrin() + ", " + readFuture.get().get().getValue());
            } catch (InterruptedException | ExecutionException e) {
                    LOG.warn("[labry]Exception: ", e);
                    e.printStackTrace();
            }

//            LOG.info("[labry]helloReadBuilder.build: " + helloReadBuilder.build());


            tx.put(LogicalDatastoreType.CONFIGURATION, HELLO_IID,
                            new HelloWorldBuilder().setCounter(helloCounter++).build());
            try {
                    tx.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                    LOG.warn("[labry]Exception: ", e);
                    e.printStackTrace();
            }
            LOG.info("[labry]helloCount(read): " + helloCounter);

            return RpcResultBuilder.success(helloReadBuilder.build()).buildFuture();
    }


    @Override
    public Future<RpcResult<HelloWorldWriteOutput>> helloWorldWrite(HelloWorldWriteInput input) {
            final ReadWriteTransaction tx = db.newReadWriteTransaction();

            tx.put(LogicalDatastoreType.OPERATIONAL, HELLO_IID, new HelloWorldBuilder().setValue(input.getStrin()).build());

            tx.put(LogicalDatastoreType.CONFIGURATION, HELLO_IID, new HelloWorldBuilder().setCounter(++helloCounter).build());

            try {
                    tx.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                    LOG.warn("[labry]Exception: ", e);
                    e.printStackTrace();
            }
            LOG.info("[labry]helloCount(write): " + helloCounter);

            HelloWorldWriteOutputBuilder helloWriteBuilder = new HelloWorldWriteOutputBuilder();
            helloWriteBuilder.setStrout(input.getStrin());

            return RpcResultBuilder.success(helloWriteBuilder.build()).buildFuture();
    }


	public void setDB(DataBroker db) {
		// TODO Auto-generated method stub
		this.db = db;
	}

	public void setNotificationService(
			NotificationProviderService notificationService) {
		// TODO Auto-generated method stub
		this.notificationService = notificationService;
	}

	@Override
	public void onMultipleOfTens(MultipleOfTens notification) {
		// TODO Auto-generated method stub
		LOG.info("on Multiple of Tens.");
	}

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> arg0) {
                    DataObject dataObject = arg0.getUpdatedSubtree();
                    if (dataObject instanceof HelloWorld) {
                            HelloWorld helloWorld = (HelloWorld) dataObject;
                            Long helloCount = helloWorld.getCounter();
                            if (helloCount != null) {
//                                    this.helloCounter = helloCount;
                                    LOG.info("[labry]onDataChanged - HelloWorldImpl: " + helloCounter);
                            }

                            if((helloCount % 10L) == 0L) {
                                    MultipleOfTens multipleOfTensNotification = new MultipleOfTensBuilder().build();
                                    notificationService.publish(multipleOfTensNotification);
                            }
                    }

    }


}