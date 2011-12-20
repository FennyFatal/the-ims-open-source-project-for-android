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

package javax.microedition.ims.core;

import javax.microedition.ims.ServiceClosedException;

/**
 * A Subscription is used for subscribing to events of the event package sent 
 * from the remote server endpoint. The application receives event 
 * notifications via callbacks to SubscriptionListener.subscriptionNotify 
 * method while being subscribed. There are two types of a subscription: 
 * A durative subscription start with a call to subscribe and ends with 
 * a call to unsubscribe. An instant subscription starts with a call 
 * to poll and ends when the first notification is received.
 * <p/>
 * The Subscription life cycle consist of three states, STATE_INACTIVE, 
 * STATE_PENDING and STATE_ACTIVE. A durative subscription can be updated 
 * with a call to subscribe while in STATE_ACTIVE.
 * <p/>
 * To establish a subscription, a Subscription has to be created. 
 * The event package to subscribe to must be set, and the subscription 
 * is started by calling subscribe. If the remote endpoint accepts the 
 * request, a notification will immediately be sent with the current 
 * event state that corresponds to the subscribed event package.
 * <p/>
 * <pre>
 *   try {
 *     sub = service.createSubscription(null, "sip:bob@home.net", "presence");
 *     sub.setListener(this); 
 *     sub.subscribe();
 *   } catch(Exception e){
 *     // handle Exceptions
 *   }
 *     
 *   public void subscriptionStarted(Subscription sub){
 *     // if the subscription was successfull
 *   }
 *   
 *   public void subscriptionNotify(Subscription sub, Message notify){
 *     // check the subscribed event state
 *   }
 * </pre>
 */
public interface Subscription extends ServiceMethod {
    
    /**
     * The Subscription is not active.
     */
    static final int STATE_INACTIVE = 1;
    
    /**
     * A Subscription request is sent and the IMS engine is waiting for a response.
     */
    static final int STATE_PENDING = 2;
    
    /**
     * The Subscription is active.
     */
    static final int STATE_ACTIVE = 3;
    
    /**
     * Starts or updates a durative subscription.
     * <p/>
     * In STATE_INACTIVE or STATE_ACTIVE the Subscription transits to STATE_PENDING after calling this method. 
     * 
     * @throws IllegalStateException - if the Subscription is not in STATE_INACTIVE or STATE_ACTIVE 
     * @throws ServiceClosedException - if the Service is closed
     */
    void subscribe() throws ServiceClosedException;
    
    /**
     * Makes an instant subscription.
     * <p/>
     * The Subscription will transit to STATE_PENDING after calling this method. 
     * 
     * @throws IllegalStateException - if the Subscription is not in STATE_INACTIVE 
     * @throws ServiceClosedException - if the Service is closed
     */
    void poll() throws ServiceClosedException;
    
    /**
     * Terminates this durative subscription.
     * <p/>
     * The Subscription will transit to STATE_PENDING after calling this method. 
     * 
     * @throws IllegalStateException - if the Subscription  is not in STATE_ACTIVE 
     * @throws ServiceClosedException - if the Service is closed
     */
    void unsubscribe() throws ServiceClosedException;
    
    /**
     * Sets a listener for this Subscription, replacing any previous SubscriptionListener. 
     * A null reference is allowed and has the effect of removing any existing listener. 
     * 
     * @param listener - the listener to set, or null
     */
    void setListener(SubscriptionListener listener);
    
    /**
     * Returns the event package corresponding to this Subscription. 
     * 
     * @return the event package
     */
    String getEvent();
    
    /**
     * Returns the current state of the state machine of the Subscription. 
     * 
     * @return the current state
     */
    int getState();

}
