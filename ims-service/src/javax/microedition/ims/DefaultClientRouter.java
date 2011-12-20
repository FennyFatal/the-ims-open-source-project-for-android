/*
 * This software code is (c) 2010 T-Mobile USA, Inc. All Rights Reserved.
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
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" AND "WITH ALL FAULTS" BASIS
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

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.core.*;
import javax.microedition.ims.core.registry.ClientRegistry;
import javax.microedition.ims.core.registry.StackRegistry;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import javax.microedition.ims.messages.wrappers.sip.Header;
import java.util.*;
import java.util.Map.Entry;

public class DefaultClientRouter implements ClientRouter {
    private static final String TAG = "DefaultClientRouter";

    private final StackRegistry stackRegistry;
    private final StackClientRegistry stackClientRegistry;

    public DefaultClientRouter(StackRegistry stackRegistry, StackClientRegistry stackClientRegistry) {
        this.stackRegistry = stackRegistry;
        this.stackClientRegistry = stackClientRegistry;
    }

    
    public AcceptContactDescriptor[] buildClientPreferences(ClientIdentity clientIdentity) {
        ClientRegistry clientRegistry = stackRegistry.getClientRegistry(clientIdentity.getAppID());
        AcceptContactDescriptor[] headers = null;
        if (clientRegistry != null) {
            headers = AcceptContactDescriptorCreator.create(clientRegistry);
        }
        return headers;
    }

    public ClientIdentity findAddressee(BaseSipMessage message) {
        final ClientIdentity addressee;

        MessageType messageType = MessageType.parse(message.getMethod());
        Logger.log(TAG, "findAddressee#message = " + messageType);


        if (messageType == MessageType.SIP_MESSAGE) {
            final String contentType = message.getContentType().getValue();
            String[] consumers = IncomingPageMessageRouter.lookupComsumers(stackRegistry.getClientRegistries(), contentType);
            Logger.log(TAG, "findAddressee#consumers.size = " + consumers.length);
            addressee = consumers.length > 0 ? stackClientRegistry.getStackClientbyAppId(consumers[0]) : null;
        }
        else {
            Collection<String> acceptHeaders = message.getCustomHeader(Header.AcceptContact);
            Collection<AcceptContactDescriptor> acceptContactDescriptors = CollectionsUtils.transform(
                    acceptHeaders,
                    new CollectionsUtils.Transformer<String, AcceptContactDescriptor>() {
                        public AcceptContactDescriptor transform(String header) {
                            return AcceptContactDescriptor.valueOf(header.trim());
                        }
                    }
            );

            Collection<String> rejectHeaders = message.getCustomHeader(Header.RejectContact);
            Collection<AcceptContactDescriptor> rejectContactDescriptors = CollectionsUtils.transform(
                    rejectHeaders,
                    new CollectionsUtils.Transformer<String, AcceptContactDescriptor>() {
                        public AcceptContactDescriptor transform(String header) {
                            return AcceptContactDescriptor.valueOf(header.trim());
                        }
                    }
            );

            addressee = findAddressee(
                    acceptContactDescriptors.toArray(new AcceptContactDescriptor[acceptContactDescriptors.size()]),
                    rejectContactDescriptors.toArray(new AcceptContactDescriptor[rejectContactDescriptors.size()])
            );
        }

        return addressee;
    }

    private ClientIdentity findAddressee(AcceptContactDescriptor[] acceptContacts,
                                         AcceptContactDescriptor[] rejectContacts) {
        final ClientIdentity retValue;

        List<ClientRegistry> clientRegistries = Arrays.asList(stackRegistry.getClientRegistries());

        if (rejectContacts != null && rejectContacts.length > 0) {
            final Set<String> rejectedClients = retrieveRejectedClients(rejectContacts);

            CollectionsUtils.removeElements(clientRegistries,
                    new CollectionsUtils.Predicate<ClientRegistry>() {
                        
                        public boolean evaluate(ClientRegistry clientRegistry) {
                            return rejectedClients.contains(clientRegistry
                                    .getAppId());
                        }
                    });
        }

        String appId = null;
        if (acceptContacts != null && acceptContacts.length > 0) {
            ClientRegistry[] registries = clientRegistries.toArray(new ClientRegistry[clientRegistries.size()]);
            appId = AcceptContactRouter.lookupBestComsumer(registries, acceptContacts);
            Logger.log(TAG, "retrieveClient#retValue = " + appId);
        }
        else if (clientRegistries.size() > 0) {
            appId = clientRegistries.get(0).getAppId();
            Logger.log(TAG, "retrieveClient#Can't retrieve client by accept header, appId = " + appId);
        }

        if (appId != null) {
            retValue = stackClientRegistry.getStackClientbyAppId(appId);
        }
        else {
            Logger.log(TAG, "retrieveClient#Can't find client");
            retValue = null;
        }

        return retValue;
    }

    private Set<String> retrieveRejectedClients(
            AcceptContactDescriptor[] contacts) {
        Set<String> rejectedClients = new HashSet<String>();

        Map<String, Float> rejectConsumers = AcceptContactRouter
                .lookupComsumers(stackRegistry.getClientRegistries(), contacts);
        for (Entry<String, Float> entry : rejectConsumers.entrySet()) {
            if (entry.getValue() == 1.0) {
                rejectedClients.add(entry.getKey());
            }
        }

        return rejectedClients;
    }
}
