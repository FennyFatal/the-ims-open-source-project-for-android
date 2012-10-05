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

package javax.microedition.ims.core.transaction;

import javax.microedition.ims.common.IMSEntityType;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.MessageType;
import javax.microedition.ims.core.IMSEntity;
import javax.microedition.ims.core.StackContext;
import javax.microedition.ims.core.transaction.client.*;
import javax.microedition.ims.core.transaction.client.msrp.MsrpSendClientTransaction;
import javax.microedition.ims.core.transaction.server.*;
import javax.microedition.ims.core.transaction.server.msrp.MsrpSendServerTransaction;
import javax.microedition.ims.core.xdm.XDMRequestTransaction;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 04-Feb-2010
 * Time: 16:14:18
 */

public class TransactionType<T, V extends T> {

    public static enum Name {
        SIP_INVITE_CLIENT,
        SIP_REINVITE_CLIENT,
        SIP_INVITE_SERVER,
        SIP_REINVITE_SERVER,
        SIP_UPDATE_SERVER,
        SIP_LOGIN,
        SIP_LOGOUT,
        SIP_SUBSCRIBE,
        SIP_UNSUBSCRIBE,
        SIP_BYE_CLIENT,
        SIP_BYE_SERVER,
        SIP_REFER_CLIENT,
        SIP_REFER_SERVER,
        SIP_NOTIFY_CLIENT,
        SIP_NOTIFY_SERVER,
        SIP_MESSAGE_CLIENT,
        SIP_MESSAGE_SERVER,
        SIP_OPTIONS_CLIENT,
        SIP_OPTIONS_SERVER,
        SIP_PUBLISH_CLIENT,

        XDM_REQUEST,

        MSRP_SEND_CLIENT,
        MSRP_REPORT_CLIENT,
        MSRP_STATUS_CLIENT,
        MSRP_SEND_SERVER
    }

    public static enum Type {
        SERVER,
        CLIENT
    }

    private static final Map<TransactionType, Map<MessageType, Class<? extends IMSMessage>>> applicableMessages =
            new HashMap<TransactionType, Map<MessageType, Class<? extends IMSMessage>>>(10);

    public static final TransactionType<InviteClntTransaction, InviteClientTransaction> SIP_INVITE_CLIENT =
            new TransactionType<InviteClntTransaction, InviteClientTransaction>(
                    Name.SIP_INVITE_CLIENT,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    InviteClntTransaction.class,
                    InviteClientTransaction.class,
                    TransactionTypeData.INVITE_CLIENT_APPLICABLE_MESSAGES
            );

    public static final TransactionType<InviteClntTransaction, ReInviteClientTransaction> SIP_REINVITE_CLIENT =
            new TransactionType<InviteClntTransaction, ReInviteClientTransaction>(
                    Name.SIP_REINVITE_CLIENT,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    InviteClntTransaction.class,
                    ReInviteClientTransaction.class,
                    TransactionTypeData.REINVITE_CLIENT_APPLICABLE_MESSAGES
            );

    public static final TransactionType<InviteSrvTransaction, InviteServerTransaction> SIP_INVITE_SERVER =
            new TransactionType<InviteSrvTransaction, InviteServerTransaction>(
                    Name.SIP_INVITE_SERVER,
                    Type.SERVER,
                    IMSEntityType.SIP,
                    InviteSrvTransaction.class,
                    InviteServerTransaction.class,
                    TransactionTypeData.INVITE_SERVER_APPLICABLE_MESSAGES
            );

    public static final TransactionType<InviteSrvTransaction, ReInviteServerTransaction> SIP_REINVITE_SERVER =
            new TransactionType<InviteSrvTransaction, ReInviteServerTransaction>(
                    Name.SIP_REINVITE_SERVER,
                    Type.SERVER,
                    IMSEntityType.SIP,
                    InviteSrvTransaction.class,
                    ReInviteServerTransaction.class,
                    TransactionTypeData.REINVITE_SERVER_APPLICABLE_MESSAGES
            );

    public static final TransactionType<UpdateSrvTransaction, UpdateServerTransaction> SIP_UPDATE_SERVER =
        new TransactionType<UpdateSrvTransaction, UpdateServerTransaction>(
                Name.SIP_UPDATE_SERVER,
                Type.SERVER,
                IMSEntityType.SIP,
                UpdateSrvTransaction.class,
                UpdateServerTransaction.class,
                TransactionTypeData.UPDATE_SERVER_APPLICABLE_MESSAGES
        );

    
    public static final TransactionType<ClientTransaction, LoginTransaction> SIP_LOGIN =
            new TransactionType<ClientTransaction, LoginTransaction>(
                    Name.SIP_LOGIN,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    null,
                    LoginTransaction.class,
                    TransactionTypeData.LOGIN_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ClientTransaction, LogoutTransaction> SIP_LOGOUT =
            new TransactionType<ClientTransaction, LogoutTransaction>(
                    Name.SIP_LOGOUT,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    null,
                    LogoutTransaction.class,
                    TransactionTypeData.LOGOUT_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ClientTransaction, SubscribeTransaction> SIP_SUBSCRIBE =
            new TransactionType<ClientTransaction, SubscribeTransaction>(
                    Name.SIP_SUBSCRIBE,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    null,
                    SubscribeTransaction.class,
                    TransactionTypeData.SUBSCRIBE_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ClientTransaction, UnsubscribeTransaction> SIP_UNSUBSCRIBE =
            new TransactionType<ClientTransaction, UnsubscribeTransaction>(
                    Name.SIP_UNSUBSCRIBE,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    null,
                    UnsubscribeTransaction.class,
                    TransactionTypeData.UNSUBSCRIBE_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ClientTransaction, ByeClientTransaction> SIP_BYE_CLIENT =
            new TransactionType<ClientTransaction, ByeClientTransaction>(
                    Name.SIP_BYE_CLIENT,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    null,
                    ByeClientTransaction.class,
                    TransactionTypeData.BYE_CLIENT_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ServerTransaction, ByeServerTransaction> SIP_BYE_SERVER =
            new TransactionType<ServerTransaction, ByeServerTransaction>(
                    Name.SIP_BYE_SERVER,
                    Type.SERVER,
                    IMSEntityType.SIP,
                    null,
                    ByeServerTransaction.class,
                    TransactionTypeData.BYE_SERVER_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ClientTransaction, PageMessageClientTransaction> SIP_MESSAGE_CLIENT =
            new TransactionType<ClientTransaction, PageMessageClientTransaction>(
                    Name.SIP_MESSAGE_CLIENT,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    null,
                    PageMessageClientTransaction.class,
                    TransactionTypeData.MESSAGE_CLIENT_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ServerTransaction, PageMessageServerTransaction> SIP_MESSAGE_SERVER =
            new TransactionType<ServerTransaction, PageMessageServerTransaction>(
                    Name.SIP_MESSAGE_SERVER,
                    Type.SERVER,
                    IMSEntityType.SIP,
                    null,
                    PageMessageServerTransaction.class,
                    TransactionTypeData.MESSAGE_SERVER_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ClientTransaction, OptionsClientTransaction> SIP_OPTIONS_CLIENT =
            new TransactionType<ClientTransaction, OptionsClientTransaction>(
                    Name.SIP_OPTIONS_CLIENT,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    null,
                    OptionsClientTransaction.class,
                    TransactionTypeData.OPTIONS_CLIENT_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ServerTransaction, OptionsServerTransaction> SIP_OPTIONS_SERVER =
            new TransactionType<ServerTransaction, OptionsServerTransaction>(
                    Name.SIP_OPTIONS_SERVER,
                    Type.SERVER,
                    IMSEntityType.SIP,
                    null,
                    OptionsServerTransaction.class,
                    TransactionTypeData.OPTIONS_SERVER_APPLICABLE_MESSAGES
            );


    public static final TransactionType<ClientTransaction, PublishClientTransaction> SIP_PUBLISH_CLIENT =
            new TransactionType<ClientTransaction, PublishClientTransaction>(
                    Name.SIP_PUBLISH_CLIENT,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    null,
                    PublishClientTransaction.class,
                    TransactionTypeData.PUBLISH_CLIENT_APPLICABLE_MESSAGES
            );


    public static final TransactionType<ClientTransaction, ReferClientTransaction> SIP_REFER_CLIENT =
            new TransactionType<ClientTransaction, ReferClientTransaction>(
                    Name.SIP_REFER_CLIENT,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    null,
                    ReferClientTransaction.class,
                    TransactionTypeData.REFER_CLIENT_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ReferSrvTransaction, ReferServerTransaction> SIP_REFER_SERVER =
            new TransactionType<ReferSrvTransaction, ReferServerTransaction>(
                    Name.SIP_REFER_SERVER,
                    Type.SERVER,
                    IMSEntityType.SIP,
                    ReferSrvTransaction.class,
                    ReferServerTransaction.class,
                    TransactionTypeData.REFER_SERVER_APPLICABLE_MESSAGES
            );

    public static final TransactionType<NotifyClntTransaction, NotifyClientTransaction> SIP_NOTIFY_CLIENT =
            new TransactionType<NotifyClntTransaction, NotifyClientTransaction>(
                    Name.SIP_NOTIFY_CLIENT,
                    Type.CLIENT,
                    IMSEntityType.SIP,
                    NotifyClntTransaction.class,
                    NotifyClientTransaction.class,
                    TransactionTypeData.NOTIFY_CLIENT_APPLICABLE_MESSAGES
            );

    public static final TransactionType<ServerTransaction, NotifyServerTransaction> SIP_NOTIFY_SERVER =
            new TransactionType<ServerTransaction, NotifyServerTransaction>(
                    Name.SIP_NOTIFY_SERVER,
                    Type.SERVER,
                    IMSEntityType.SIP,
                    null,
                    NotifyServerTransaction.class,
                    TransactionTypeData.NOTIFY_SERVER_APPLICABLE_MESSAGES
            );

    public static final TransactionType<Transaction, XDMRequestTransaction> XDM_REQUEST =
            new TransactionType<Transaction, XDMRequestTransaction>(
                    Name.XDM_REQUEST,
                    Type.CLIENT,
                    IMSEntityType.XDM,
                    null,
                    XDMRequestTransaction.class,
                    TransactionTypeData.XDM_REQUEST_APPLICABLE_MESSAGES
            );


    public static final TransactionType<Transaction, MsrpSendClientTransaction> MSRP_SEND_CLIENT =
            new TransactionType<Transaction, MsrpSendClientTransaction>(
                    Name.MSRP_SEND_CLIENT,
                    Type.CLIENT,
                    IMSEntityType.MSRP,
                    null,
                    MsrpSendClientTransaction.class,
                    TransactionTypeData.MSRP_SEND_APPLICABLE_MESSAGES
            );

    public static final TransactionType<Transaction, MsrpSendServerTransaction> MSRP_SEND_SERVER =
            new TransactionType<Transaction, MsrpSendServerTransaction>(
                    Name.MSRP_SEND_SERVER,
                    Type.SERVER,
                    IMSEntityType.MSRP,
                    null,
                    MsrpSendServerTransaction.class,
                    TransactionTypeData.MSRP_SEND_APPLICABLE_MESSAGES
            );


    private final Name name;
    private final Type type;
    private final IMSEntityType entityType;
    private final Class<T> publicInterface;
    private final Class<V> implementationClass;


    private TransactionType(
            final Name name,
            final Type type,
            final IMSEntityType entityType,
            final Class<T> publicInterface,
            final Class<V> implementationClass,
            final Map<MessageType, Class<? extends IMSMessage>> applicableMessages) {

        this.name = name;
        this.type = type;
        this.entityType = entityType;
        this.publicInterface = publicInterface;
        this.implementationClass = implementationClass;
        TransactionType.applicableMessages.put(this, applicableMessages);
    }

    public V instantiate(
            final IMSEntity entity,
            final StackContext transactionContext,
            final TransactionDescription description) throws TransactionInstantiationException {

        final V retValue;
        Class<V> implClass = getImplementationClass();
        try {
            retValue = doInstantiate(
                    implClass,
                    entity,
                    transactionContext,
                    description
            );
        } catch (Exception e) {
            throw new TransactionInstantiationException(e);
        }

        return retValue;
    }

    public T wrap(V hostObject) {
        return TransactionUtils.wrap(hostObject, getPublicInterface(), Transaction.class);
    }

    private static <T> T doInstantiate(
            final Class<T> clazz,
            final IMSEntity entity,
            final StackContext stackContext,
            final TransactionDescription description)

            throws InvocationTargetException,
            IllegalAccessException, InstantiationException {

        //final Constructor<T> constructor = clazz.getConstructor(StackContext.class, entity.getClass());
        final Constructor<T> constructor = (Constructor<T>) clazz.getConstructors()[0];
        return constructor.newInstance(stackContext, entity, description);
    }

    public Name getName() {
        return name;
    }

    public boolean isServer() {
        return type == Type.SERVER;
    }

    public IMSEntityType getEntityType() {
        return entityType;
    }

    public Class<T> getPublicInterface() {
        return publicInterface;
    }

    public Class<V> getImplementationClass() {
        return implementationClass;
    }

    public static Map<MessageType, Class<? extends IMSMessage>> getApplicableMessages(TransactionType transactionType) {
        return applicableMessages.get(transactionType);
    }


    public String toString() {
        return "TransactionType [entityType=" + entityType
                + ", implementationClass=" + implementationClass + ", name="
                + name + ", publicInterface=" + publicInterface + ", type="
                + type + "]";
    }
}
