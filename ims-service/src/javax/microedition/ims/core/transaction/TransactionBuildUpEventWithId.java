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

import javax.microedition.ims.common.ListenerSupportTypeHolder;
import javax.microedition.ims.common.ListenerSupportTypesHolder;
import javax.microedition.ims.core.IMSEntity;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 28.02.2010
 * Time: 10:48:54
 */
public class TransactionBuildUpEventWithId implements TransactionBuildUpEvent,
        ListenerSupportTypeHolder<TransactionType.Name>, ListenerSupportTypesHolder {

    private final IMSEntity entity;
    private final Transaction transaction;
    private final TransactionDescription trnsDescr;
    private final Set<?> supportedTypes;

    public TransactionBuildUpEventWithId(
            final IMSEntity entity,
            final Transaction transaction,
            final TransactionDescription trnsDescr) {

        this.entity = entity;
        this.transaction = transaction;
        this.trnsDescr = trnsDescr;
        this.supportedTypes = Collections.unmodifiableSet(new HashSet<IMSEntity>(Arrays.asList(entity)));
    }

    
    public IMSEntity getEntity() {
        return entity;
    }

    
    public Transaction getTransaction() {
        return transaction;
    }

    public TransactionDescription getTransactionDescription() {
        return trnsDescr;
    }

    public TransactionType.Name getType() {
        return transaction.getTransactionType().getName();
    }

    public Set<?> getTypes() {
        return supportedTypes;
    }

    public String toString() {
        return "TransactionBuildUpEventWithId{" +
                "entity=" + entity +
                ", transaction=" + transaction +
                '}';
    }
}
