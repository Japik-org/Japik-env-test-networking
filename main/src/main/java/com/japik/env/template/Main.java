package com.japik.env.template;

import com.japik.Japik;
import com.japik.extension.IExtension;
import com.japik.extensions.rmiprotocol.shared.IRMIProtocolExtensionConnection;
import com.japik.networking.Remote;
import com.japik.service.IService;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");

        final Japik japik = new Japik(Paths.get(""));
        japik.getLiveCycle().init();
        japik.getLiveCycle().start();

        final IExtension<IRMIProtocolExtensionConnection> rmiProtocolExt = (IExtension<IRMIProtocolExtensionConnection>)
                japik.getExtensionLoader().load("RMIProtocol", "rmiProtocol");
        rmiProtocolExt.getSettings().put("enableLookup", true);
        rmiProtocolExt.getSettings().put("port", 13500);

        rmiProtocolExt.getLiveCycle().init();
        rmiProtocolExt.getLiveCycle().start();

        final Remote remote = japik.getNetworking().getRemoteCollection().add(
                new Remote.Builder()
                        .setRemoteName("RMIRemote")
                        .setProtocolName("RMI")
        );
        remote.getProtocolSettings().put("host", "127.0.0.1");
        remote.getProtocolSettings().put("port", 13500);

        final IService<?> service = japik.getServiceLoader().load("Simple", "simple");
        service.getLiveCycle().init();
        service.getLiveCycle().start();

        try {
            remote.getLiveCycle().init();
            remote.getLiveCycle().start();

            final boolean pingRes = remote.getProtocolInstance().getServiceConnection("simple").ping();
            japik.getLoggerManager().getMainLogger().info("RMI protocol works! Ping result is: " + pingRes + ".");

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        Thread.sleep(150);

        service.getLiveCycle().stopForce();
        service.getLiveCycle().destroy();

        if (remote.getLiveCycle().getStatus().isStarted())
            remote.getLiveCycle().stopForce();
        remote.getLiveCycle().destroy();

        rmiProtocolExt.getLiveCycle().stopForce();
        rmiProtocolExt.getLiveCycle().destroy();

        japik.getLiveCycle().stopForce();
        japik.getLiveCycle().destroy();
    }
}
