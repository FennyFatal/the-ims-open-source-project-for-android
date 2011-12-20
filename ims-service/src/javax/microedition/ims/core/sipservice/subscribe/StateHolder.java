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

package javax.microedition.ims.core.sipservice.subscribe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 31.5.2010
 * Time: 17.31.24
 */
class StateHolder {

    private static final Map<State, Map<TransitReason, State>> transitionTable
            = Collections.unmodifiableMap(createTransitionMap());

    private final AtomicReference<State> state = new AtomicReference<State>(State.NO_SUBSCRIPTION);

    public StateHolder() {
    }

    public State getState() {
        return state.get();
    }

    public boolean compareAndTransitNextState(
            final State expectedState,
            final boolean successiveTransit,
            final SubscribeTransactionDescription description) {

        State nextState;
        boolean retValue = false;

        synchronized (state) {
            State currentState = state.get();

            if (successiveTransit) {
                final Map<TransitReason, State> transitReasonStateMap = transitionTable.get(currentState);
                final State state = transitReasonStateMap.get(mapToTransitionPath(description));

                nextState = currentState == expectedState ? state : currentState;
            }
            else {
                nextState = State.UNSUBSCRIBED;
            }

            state.set(nextState);
            retValue = currentState != nextState;
        }

        return retValue;
    }

    private TransitReason mapToTransitionPath(SubscribeTransactionDescription description) {
        TransitReason retValue = TransitReason.GENERAL_TRANSITION_PATH;

        if (description != null &&
                description.getType() != null &&
                SubscribeTransactionDescription.Type.REFRESH == description.getType()) {

            retValue = TransitReason.REFRESH_TRANSITION_PATH;
        }

        return retValue;
    }

    public boolean transitFinalState() {

        State nextState;
        boolean retValue = false;

        synchronized (state) {
            State currentState = state.get();
            nextState = State.UNSUBSCRIBED;

            state.set(nextState);
            retValue = currentState != nextState;
        }

        return retValue;
    }

    private static Map<State, Map<TransitReason, State>> createTransitionMap() {
        Map<State, Map<TransitReason, State>> retValue = new HashMap<State, Map<TransitReason, State>>();

        final Map<TransitReason, State> nsTransitions = new HashMap<TransitReason, State>();
        nsTransitions.put(TransitReason.GENERAL_TRANSITION_PATH, State.SUBSCRIBING);
        retValue.put(State.NO_SUBSCRIPTION, nsTransitions);

        final Map<TransitReason, State> spTransitions = new HashMap<TransitReason, State>();
        spTransitions.put(TransitReason.GENERAL_TRANSITION_PATH, State.SUBSCRIBED);
        retValue.put(State.SUBSCRIBING, spTransitions);

        final Map<TransitReason, State> sdTransitions = new HashMap<TransitReason, State>();
        sdTransitions.put(TransitReason.GENERAL_TRANSITION_PATH, State.UNSUBSCRIBING);
        sdTransitions.put(TransitReason.REFRESH_TRANSITION_PATH, State.RESUBSCRIBING);
        retValue.put(State.SUBSCRIBED, sdTransitions);

        final Map<TransitReason, State> srTransitions = new HashMap<TransitReason, State>();
        srTransitions.put(TransitReason.GENERAL_TRANSITION_PATH, State.SUBSCRIBED);
        srTransitions.put(TransitReason.FAILURE_TRANSITION_PATH, State.UNSUBSCRIBED);
        retValue.put(State.RESUBSCRIBING, srTransitions);

        final Map<TransitReason, State> upTransitions = new HashMap<TransitReason, State>();
        upTransitions.put(TransitReason.GENERAL_TRANSITION_PATH, State.UNSUBSCRIBED);
        retValue.put(State.UNSUBSCRIBING, upTransitions);

        final Map<TransitReason, State> udTransitions = new HashMap<TransitReason, State>();
        udTransitions.put(TransitReason.GENERAL_TRANSITION_PATH, State.UNSUBSCRIBED);
        retValue.put(State.UNSUBSCRIBED, udTransitions);

        return retValue;
    }

    
    public String toString() {
        return "StateHolder{" +
                "state=" + state +
                '}';
    }
}
