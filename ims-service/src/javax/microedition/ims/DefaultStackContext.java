/*
 * This software code is © 2010 T-Mobile USA, Inc. All Rights Reserved.
 *
 * Unauthorized redistribution or further use of this material is
 * prohibited without the express permission of T-Mobile USA, Inc. and
 * will be prosecuted to the fullest extent of the law.
 *
 * Removal or modification of these Terms and Conditions from the source
 * or binary code of this software is prohibited.  In the event that
 * redistribution of the source or binary code for this software is
 * approved by T-Mobile USA, Inc., these Terms and Conditions and the
 * above copyright notice must be reproduced in their entirety and in all
 * circumstances.
 *
 * No name or trademarks of T-Mobile USA, Inc., or of its parent company,
 * Deutsche Telekom AG or any Deutsche Telekom or T-Mobile entity, may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ON AN “AS IS” AND “WITH ALL FAULTS” BASIS
 * AND WITHOUT WARRANTIES OF ANY KIND.  ALL EXPRESS OR IMPLIED
 * CONDITIONS, REPRESENTATIONS OR WARRANTIES, INCLUDING ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT CONCERNING THIS SOFTWARE, ITS SOURCE OR BINARY CODE
 * OR ANY DERIVATIVES THEREOF ARE HEREBY EXCLUDED.  T-MOBILE USA, INC.
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE
 * OR ITS DERIVATIVES.  IN NO EVENT WILL T-MOBILE USA, INC. OR ITS
 * LICENSORS BE LIABLE FOR LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT
 * OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF T-MOBILE USA,
 * INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * THESE TERMS AND CONDITIONS APPLY SOLELY AND EXCLUSIVELY TO THE USE,
 * MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE, ITS SOURCE OR BINARY
 * CODE OR ANY DERIVATIVES THEREOF, AND ARE SEPARATE FROM ANY WRITTEN
 * WARRANTY THAT MAY BE PROVIDED WITH A DEVICE YOU PURCHASE FROM T-MOBILE
 * USA, INC., AND TO THE EXTENT PERMITTED BY LAW.
 */

package javax.microedition.ims;

import javax.microedition.ims.common.*;
import javax.microedition.ims.common.util.NetUtils;
import javax.microedition.ims.config.Configuration;
import javax.microedition.ims.config.UserInfo;
import javax.microedition.ims.core.*;
import javax.microedition.ims.core.auth.AKAAuthProvider;
import javax.microedition.ims.core.connection.*;
import javax.microedition.ims.core.dialog.DefaultDialogStorage;
import javax.microedition.ims.core.dialog.DialogStorage;
import javax.microedition.ims.core.env.Environment;
import javax.microedition.ims.core.env.GsmLocationService;
import javax.microedition.ims.core.registry.*;
import javax.microedition.ims.core.registry.property.AuthenticationProperty;
import javax.microedition.ims.core.sipservice.register.RegistrationInfo;
import javax.microedition.ims.messages.wrappers.sip.AuthType;
import javax.microedition.ims.transport.*;
import javax.microedition.ims.transport.impl.ConnectionSecurityInfoProvider;
import javax.microedition.ims.transport.impl.MessageTransport;
import javax.microedition.ims.transport.impl.QueueException;
import javax.microedition.ims.transport.messagerouter.Route;
import javax.microedition.ims.transport.messagerouter.Router;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 17-Feb-2010
 * Time: 10:12:32
 */
public class DefaultStackContext implements StackContextExt, Shutdownable {
    private static final String TAG = "DefaultStackContext";

    private final CompositeConfig config;
    private final StackClientRegistry stackUserRegistry;
    private final Environment environment;
    //private final ConnectionManager connManager;
    //private final OptionsRegistry optionsRegistry;
    private final StackRegistry stackRegistry;
    private final TransportIO<IMSMessage> transportIO;
    //private final DefaultRouteResolver defaultRouteResolver;
    private final Router<IMSMessage> messageRouter;

    //TODO review
    private ClientIdentity registrationIdentity;
    private final DialogStorage dialogStorage;
    private final ClientRouter clientRouter;
    private final AKAAuthProvider akaAuthProvider;
    private final AtomicReference<RegistrationInfo> registrationInfo = new AtomicReference<RegistrationInfo>(null);
    private final ConnectionSecurityInfoProvider connectionSecurityInfoProvider;
    private final ConnectionDataProvider connDataProvider;
    //private final GsmLocationService gsmLocationService = new GsmLocationServiceDefaultImpl();
    private AtomicReference<Protocol> currentProtocol = new AtomicReference<Protocol>();

    private final RepetitiousTaskManager repetitiousTaskManager;

    private final ConnStateListener connectionRestorer = new ConnStateListenerAdapter() {
        public void onConnected(final ConnStateEvent event) {
            
            //messageRouter.refresh();
            if(connDataProvider.getConnectionData() == null) {
                try {
                    Logger.log(TAG, "*** ConnStateListener.onConnected#before - ConnectionDataProviderConfigVsDnsImpl.obtainSecurityInfo()");
                    connDataProvider.refresh();
                    Logger.log(TAG, "*** ConnStateListener.onConnected#after - ConnectionDataProviderConfigVsDnsImpl.obtainSecurityInfo()");
                } catch (IMSStackException e) {
                    Logger.log(TAG, "Cann't update connection data provider after the connection was restored, message = " + e.getMessage());
                }    
            }

            //Reestablish SIP subsystem connection
            //final Route defaultSipRoute = defaultRouteResolver.getDefaultRoute(IMSEntityType.SIP);
            final Collection<Route> activeRoutes = messageRouter.getActiveRoutes();
            for (Route route : activeRoutes) {

                assert route != null : "Active routes contains null element: " + activeRoutes;

                //TODO: consider to introduce shutdownAll() method.
                transportIO.shutdownRoute(route);
                try {
                    transportIO.establishRoute(route);
                }
                catch (ChannelIOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    
    private final NetworkInterfaceListener interfaceChangeListener = new NetworkInterfaceListener() {
        
        public void onInterfaceChanged(IpChangeEvent event) {
            Logger.log(TAG, "onInterfaceChanged");
            if(NetUtils.isAddressLocal(event.getCurrentAddress())
                    || event.getCurrentNetworkType() == NetworkType.NONE) {
                return;
            }
            
            Logger.log(TAG, "onInterfaceChanged# force transport shutdown");
            TransportIO<IMSMessage> transportIO = getTransportIO();
            transportIO.shutdownRoutes();
        }
    };

    private RegistryChangeListener registryIdentityChangeListener = new RegistryChangeAdapter() {
        
        public void commonRegistryChanged(
                RegistryChangeEvent<CommonRegistry> event) {
            Logger.log(TAG, "commonConfigChanged#");
            final AuthenticationProperty oldAuth = event.getOldConfig().getAuthenticationProperty();
            final AuthenticationProperty newAuth = event.getNewConfig().getAuthenticationProperty();
            if (newAuth != null && !newAuth.equals(oldAuth)) {
                registrationIdentity = createRegisterIdentity();
            }
        }
    };

    public static class Builder {
        private Configuration config;
        private Environment environment;
        private StackRegistry stackRegistry;
        private AKAAuthProvider akaAuthProvider;
        private Router<IMSMessage> router;
        private javax.microedition.ims.core.connection.GsmLocationInfo gsmLocationInfo;
        private ConnectionSecurityInfoProvider connectionSecurityInfoProvider;
        private ConnectionDataProvider connDataProvider;

        private RepetitiousTaskManager repetitiousTaskManager;

        public Builder() {
        }

        public Builder configuration(final Configuration config) {
            this.config = config;
            return this;
        }

        public Builder environment(final Environment env) {
            this.environment = env;
            return this;
        }

        public Builder stackRegistry(final StackRegistry stackRegistry) {
            this.stackRegistry = stackRegistry;
            return this;
        }

        public Builder akaAuthProvider(final AKAAuthProvider akaAuthProvider) {
            this.akaAuthProvider = akaAuthProvider;
            return this;
        }

        public Builder router(final Router<IMSMessage> router) {
            this.router = router;
            return this;
        }

        public Builder gsmLocationInfo(final javax.microedition.ims.core.connection.GsmLocationInfo gsmLocationInfo) {
            this.gsmLocationInfo = gsmLocationInfo;
            return this;
        }

        public Builder connectionSecurityInfoProvider(final ConnectionSecurityInfoProvider securityInfoProvider) {
            this.connectionSecurityInfoProvider = securityInfoProvider;
            return this;
        }
        
        public Builder connectionDataProvider(final ConnectionDataProvider connDataProvider) {
            this.connDataProvider = connDataProvider;
            return this;
        }

        public Builder repetitiousTaskManager(final RepetitiousTaskManager repetitiousTaskManager) {
            this.repetitiousTaskManager = repetitiousTaskManager;
            return this;
        }

        public DefaultStackContext build() throws IMSStackException {
            return new DefaultStackContext(this);
        }
    }

    private DefaultStackContext(final Builder builder) throws IMSStackException {

        this.config = new CompositeConfig(builder.config);
        if (this.config == null) {
            throw new IllegalArgumentException("Config can not be null. Now it has value '" + config + "'");
        }

        /*
        this.connManager = builder.connManager;
        if (this.connManager == null) {
            throw new IllegalArgumentException("Connection  manager can not be null. Now it has value '" + connManager + "'");
        }*/
        this.environment = builder.environment;
        if (this.environment == null) {
            throw new IllegalArgumentException("Environment  can not be null. Now it has value '" + this.environment + "'");
        }


        this.stackRegistry = builder.stackRegistry;
        if (this.stackRegistry == null) {
            throw new IllegalArgumentException("Stack regestry can not be null. Now it has value '" + stackRegistry + "'");
        }

        this.akaAuthProvider = builder.akaAuthProvider;
        if (this.akaAuthProvider == null) {
            throw new IllegalArgumentException("Auth provider can not be null. Now it has value '" + akaAuthProvider + "'");
        }

        this.messageRouter = builder.router;
        if (this.messageRouter == null) {
            throw new IllegalArgumentException("Message messageRouter can not be null. Now it has value '" + this.messageRouter + "'");
        }
        checkGBAIntegrity();
        checkAkaIntegrity();

        this.connectionSecurityInfoProvider = builder.connectionSecurityInfoProvider;


        this.stackUserRegistry = new StackClientRegistryImpl();
        //connManager.addConnStateListener(connectionRestorer);
        this.environment.getConnectionManager().addConnStateListener(connectionRestorer);
        this.environment.getConnectionManager().addNetworkInterfaceListener(interfaceChangeListener);

        stackRegistry.addRegistryChangeListener(this.config);
        stackRegistry.addRegistryChangeListener(registryIdentityChangeListener);

        this.clientRouter = new DefaultClientRouter(stackRegistry, stackUserRegistry);
        //this.optionsRegistry = optionsRegistry;

        this.registrationIdentity = createRegisterIdentity();
        this.dialogStorage = new DefaultDialogStorage(this);

        this.repetitiousTaskManager = builder.repetitiousTaskManager;
        if (this.repetitiousTaskManager == null) {
            throw new IllegalArgumentException("Repetitious task manager can not be null. Now it has value '" + this.repetitiousTaskManager + "'");
        }

        /*
        final GsmLocationServiceDefaultImpl gsmLocationServiceDefault =
                (GsmLocationServiceDefaultImpl) this.gsmLocationService;
        gsmLocationServiceDefault.updateLocationInfo(builder.gsmLocationInfo);
        */
        GsmLocationService gsmLocationService = this.environment.getGsmLocationService();
        if(gsmLocationService instanceof GsmLocationServiceDefaultImpl){
            GsmLocationServiceDefaultImpl locationServiceDefault = (GsmLocationServiceDefaultImpl) gsmLocationService;
            locationServiceDefault.updateLocationInfo(builder.gsmLocationInfo);
        }

        this.connDataProvider = builder.connDataProvider;
        if (this.connDataProvider == null) {
            throw new IllegalArgumentException("Message connDataProvider can not be null.");
        }
        this.connDataProvider.addListener(this.connectionSecurityInfoProvider);
        
        this.currentProtocol.set(config.getConnectionType());

        try {
            transportIO = createAndPrepareTransport(messageRouter);
        }
        catch (IOException e) {
            throw new IMSStackException(e.getMessage());
        }
        catch (QueueException e) {
            throw new IMSStackException(e.getMessage());
        }
    }
    
    private void checkGBAIntegrity() throws AkaException {
		AuthType type = getConfig().getUserPassword().getPasswordType();
        if (AuthType.AKA == type) {
			throw new AkaException(ReasonCode.GBA_U_MISSIN);
		}
    }

    private void checkAkaIntegrity() throws AkaException {
		AuthType type = getConfig().getUserPassword().getPasswordType();
        if (AuthType.AKA == type) {
			throw new AkaException(ReasonCode.IMPU_MISSING);
		}
    }


    public Configuration getConfig() {
        return config;
    }


    public StackClientRegistry getStackClientRegistry() {
        return stackUserRegistry;
    }


    public StackRegistry getStackRegistry() {
        return stackRegistry;
    }


    /*
    public ConnectionManager getConnectionManager() {
        return connManager;
    }
    */

    public Environment getEnvironment() {
        return environment;
    }

    public Router<IMSMessage> getMessageRouter() {
        return messageRouter;
    }

    public TransportIO<IMSMessage> getTransportIO() {
        return transportIO;
    }


    public ClientIdentity getRegistrationIdentity() {
        return registrationIdentity;
    }


    public DialogStorage getDialogStorage() {
        return dialogStorage;
    }

    public AKAAuthProvider getAkaAuthProvider() {
        return akaAuthProvider;
    }

    public RegistrationInfo getRegistrationInfo() {
        return registrationInfo.get();
    }

    public void updateRegistrationInfo(final RegistrationInfo registrationInfo) {
        this.registrationInfo.set(registrationInfo);
    }

    private MessageTransport<IMSMessage> createAndPrepareTransport(final Router<IMSMessage> router) throws IOException, QueueException {

        IMSMessageContextRegistry<IMSMessage> registry = new IMSMessageContextRegistry<IMSMessage>(
                new DummyIMSMessage()
        );

        registry.registerMessageContext(new SipMessageContext(this));
        registry.registerMessageContext(new XdmMessageContext(this));
        registry.registerMessageContext(new MsrpMessageContext(this));

        MessageTransport<IMSMessage> retValue = new MessageTransport<IMSMessage>(router, registry, connectionSecurityInfoProvider);

        retValue.addTransportListener(
                new TransportListenerAdapter<IMSMessage>() {

                    public void onIncomingConnection(final Route incomingRout, final IMSMessage firstMessage) {
                        super.onIncomingConnection(incomingRout, firstMessage);
                        log("New server connection detected. Route : " + incomingRout + " message: " + firstMessage, "ENTRYPOINT");
                    }


                    public void onChannelError(final Route route, final Exception e) {
                        log("Route " + route + " encountered an error. All 'inprogress' transactions will be shutdown", "ENTRYPOINT");
                    }
                }
        );

        retValue.addUASListener(
                new UASListener() {

                    public void onUASShutdown(final int port, final Protocol protocol, final Exception e) {
                        log("" + protocol + " UAS on port " + port + " shutdown", "ENTRYPOINT");
                    }
                }
        );

        return retValue;
    }


    public void shutdown() {
        ((Shutdownable) transportIO).shutdown();

        environment.getConnectionManager().removeNetworkInterfaceListener(interfaceChangeListener);
        stackRegistry.removeRegistryChangeListener(config);
        stackRegistry.removeRegistryChangeListener(registryIdentityChangeListener);
        
        repetitiousTaskManager.shutdown();
    }

/*    
    public OptionsRegistry getOptionsRegistry() {
        return optionsRegistry;
    }
*/

    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(TAG);
        sb.append("{id=").append(hashCode());
        sb.append(", configuration=").append(config);
        sb.append('}');
        return sb.toString();
    }

    private static void log(String msg, String prefix) {
        Logger.log(prefix, msg);
    }


    public ClientRouter getClientRouter() {
        return clientRouter;
    }

    @Override
    public ConnectionDataProvider getConnectionDataProvider() {
        return connDataProvider;
    }

    /*
    public GsmLocationService getGsmLocationService() {
        return gsmLocationService;
    }
    */
    
    private ClientIdentityImpl createRegisterIdentity() {
        final AuthType passwordType = config.getUserPassword().getPasswordType();
        
        final UserInfo userInfo; 
        switch (passwordType) {
            case DIGEST:
                userInfo = config.getRegistrationName();
                break;
            case AKA:
            default:
                throw new IllegalArgumentException(String.format("Authentication type '%s' isn't supported", passwordType));
        }
        
        return ClientIdentityImpl.Creator.createFromUserInfo("register_service", userInfo);
    }
    
    @Override
    public Protocol getConnectionType() {
        return currentProtocol.get();
    }
    
    @Override
    public void updateProtocol(Protocol protocol) {
        Logger.log(TAG, "updateConnectionData#new connection data is " + protocol);
        currentProtocol.set(protocol);
    }

    @Override
    public RepetitiousTaskManager getRepetitiousTaskManager() {
        return repetitiousTaskManager;
    }
}
