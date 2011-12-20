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

package com.android.ims.core.media.util;

import javax.microedition.ims.core.media.Media;

/**
 * Utility class for direction media attribute.
 *
 * @author ext-akhomush
 */
public final class DirectionUtils {
    private static final String DIRECTION_INACTIVE = "inactive";
    private static final String DIRECTION_SEND = "sendonly";
    private static final String DIRECTION_RECEIVE = "recvonly";
    private static final String DIRECTION_SEND_RECEIVE = "sendrecv";

    private DirectionUtils() {
        assert false;
    }

    public static String convertToString(int directionType) {
        String direction = null;
        switch (directionType) {
            case Media.DIRECTION_INACTIVE:
                direction = DIRECTION_INACTIVE;
                break;
            case Media.DIRECTION_SEND:
                direction = DIRECTION_SEND;
                break;
            case Media.DIRECTION_RECEIVE:
                direction = DIRECTION_RECEIVE;
                break;
            case Media.DIRECTION_SEND_RECEIVE:
                direction = DIRECTION_SEND_RECEIVE;
                break;
            default:
                assert false;
                break;
        }

        return direction;
    }

    public static int convertToType(String direction) {
        int type = 0;

        if (DIRECTION_INACTIVE.equals(direction)) {
            type = Media.DIRECTION_INACTIVE;
        } else if (DIRECTION_RECEIVE.equals(direction)) {
            type = Media.DIRECTION_RECEIVE;
        } else if (DIRECTION_SEND.equals(direction)) {
            type = Media.DIRECTION_SEND;
        } else if (DIRECTION_SEND_RECEIVE.equals(direction)) {
            type = Media.DIRECTION_SEND_RECEIVE;
        } else {
            type = Media.DIRECTION_SEND_RECEIVE; //TODO add check for "sendrecv" SHOULD be assumed as the
            //default for sessions that are not of the conference type
            //"broadcast" or "H332" (see below).
        }

        return type;
    }

    public static boolean isDirectionValid(int direction) {
        return direction == Media.DIRECTION_INACTIVE || direction == Media.DIRECTION_RECEIVE
                || direction == Media.DIRECTION_SEND || direction == Media.DIRECTION_SEND_RECEIVE;
    }

    public static int reverseDirection(int direction) {
        int ret = direction;
        if (direction == Media.DIRECTION_SEND) {
            ret = Media.DIRECTION_RECEIVE;
        } else if (direction == Media.DIRECTION_RECEIVE) {
            ret = Media.DIRECTION_SEND;
        }
        return ret;
    }
}
