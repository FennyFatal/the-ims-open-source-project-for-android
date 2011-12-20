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

package javax.microedition.ims.messages.utils;

public final class StatusCode {
    public static final int TRYING = 100;
    public static final int RINGING = 180;
    public static final int CALL_BEING_FORWARDED = 181;
    public static final int CALL_QUEUED = 182;
    public static final int CALL_SESSION_PROGRESS = 183;
    public static final int OK = 200;
    public static final int ACCEPTED = 202;
    public static final int MULTIPLE_CHOICES = 300;
    public static final int MOVED_PERMANENTLY = 301;
    public static final int MOVED_TEMPORARILY = 302;
    public static final int USE_PROXY = 305;
    public static final int ALTERNATIVE_SERVICE = 380;
    public static final int BAD_REQUEST = 400;
    public static final int UNATHORIZED = 401;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PROXY_AUTH_REQUIRED = 407;
    public static final int REQUEST_TIMEOUT = 408;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int LENGTH_REQUIRED = 411;
    public static final int CONDITIONAL_REQUEST_FAILED = 412;
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;
    public static final int REQUEST_URI_TOO_LONG = 414;
    public static final int UNSUPPORTED_MEDIA_TIME = 415;
    public static final int UNSUPPORTED_URI_SCHEMA = 416;
    public static final int BAD_EXTENTION = 420;
    public static final int EXTENTION_REQUIRED = 421;
    public static final int SESSION_INTERVAL_TOO_SMALL = 422;
    public static final int INTERVAL_TOO_BRIEF = 423;
    public static final int TEMPORARY_UNAVAILABLE = 480;
    public static final int CALL_OR_TRANSACTION_DOESNOT_EXISTS = 481;
    public static final int LOOP_DETECTED = 482;
    public static final int TOO_MANY_HOPS = 483;
    public static final int ADDRESS_INCOMPLETE = 484;
    public static final int AMBIGUOUS = 485;
    public static final int BUSY_HERE = 486;
    public static final int REQUEST_TERMINATED = 487;
    public static final int NOT_ACCEPTABLE_HERE = 488;
    public static final int REQUEST_PENDING = 491;
    public static final int UNDECIPHERABLE = 493;
    public static final int SERVER_INTERNAL_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int SERVER_TIMEOUT = 504;
    public static final int VERSION_NOT_SUPPORTED = 505;
    public static final int MESSAGE_TOO_LARGE = 513;
    public static final int BUSY_EVERYWHERE = 600;
    public static final int DECLINED = 603;
    public static final int STATUS_DOESNOT_EXISTS_ANYWHERE = 604;

    private StatusCode() {
    }
}
