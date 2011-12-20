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

package com.android.ims.util;

import android.util.Log;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Utils {
    private static final String TAG = "Utils";

    private static final int PORT_FIRST = 1024;
    private static final int PORT_LAST = 65535;

    private static Random random = new Random();

    private static ThreadLocal<Calendar> calendarThreadLocal = new ThreadLocal<Calendar>() {
        @Override
        protected Calendar initialValue() {
            return GregorianCalendar.getInstance();
        }
    };

    private static ThreadLocal<DateFormat> yearFormaterThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private static ThreadLocal<DateFormat> timeFormaterThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss");
        }
    };


    private Utils() {
        assert false;
    }

    public static String retrieveLocalAddress() {
        String localAddress = null;

        Enumeration<NetworkInterface> interfaces = null;

        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (interfaces != null) {
            while (interfaces.hasMoreElements()) {
                NetworkInterface intrf = interfaces.nextElement();
                Enumeration<InetAddress> enumIpAddr = intrf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        localAddress = inetAddress.getHostAddress();
                        break;
                    }
                }
                if (localAddress != null) {
                    break;
                }
            }
        }
        Log.i(TAG, "retrieveLocalAddress#localAddress: " + localAddress);
        return localAddress;
    }

    /**
     * Generate a random port number (TCP or UDP).
     *
     * @return a random port number
     */
    public static int generateRandomPortNumber() {
        return generateRandomPortNumber(PORT_FIRST, PORT_LAST);
    }

    /**
     * Generate a random port number (TCP or UDP).
     *
     * @param first - random start
     * @param last  - random end
     * @return a random port number
     */
    public static int generateRandomPortNumber(int first, int last) {
        int port = random.nextInt(last - first) + first;
        if (port % 2 != 0) {
            port += 1;
        }
        return port;
    }

    public static boolean isEmpty(String source) {
        return source == null || "".equals(source.trim());
    }

    public static boolean isExactlyEmpty(String source) {
        return source != null && "".equals(source.trim());
    }

    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generatePseudoUUID() {
        String generateUUID = generateUUID();
        return "p" + generateUUID.substring(1);
    }

    //http://www.ietf.org/rfc/rfc3339.txt
    /*
    The following profile of ISO 8601 [ISO8601] dates SHOULD be used in
   new protocols on the Internet.  This is specified using the syntax
   description notation defined in [ABNF].

   date-fullyear   = 4DIGIT
   date-month      = 2DIGIT  ; 01-12
   date-mday       = 2DIGIT  ; 01-28, 01-29, 01-30, 01-31 based on
                             ; month/year
   time-hour       = 2DIGIT  ; 00-23
   time-minute     = 2DIGIT  ; 00-59
   time-second     = 2DIGIT  ; 00-58, 00-59, 00-60 based on leap second
                             ; rules
   time-secfrac    = "." 1*DIGIT
   time-numoffset  = ("+" / "-") time-hour ":" time-minute
   time-offset     = "Z" / time-numoffset

   partial-time    = time-hour ":" time-minute ":" time-second
                     [time-secfrac]
   full-date       = date-fullyear "-" date-month "-" date-mday
   full-time       = partial-time time-offset

   date-time       = full-date "T" full-time

      NOTE: Per [ABNF] and ISO8601, the "T" and "Z" characters in this
      syntax may alternatively be lower case "t" or "z" respectively.

      This date/time format may be used in some environments or contexts
      that distinguish between the upper- and lower-case letters 'A'-'Z'
      and 'a'-'z' (e.g. XML).  Specifications that use this format in
      such environments MAY further limit the date/time syntax so that
      the letters 'T' and 'Z' used in the date/time syntax must always
      be upper case.  Applications that generate this format SHOULD use
      upper case letters.

      NOTE: ISO 8601 defines date and time separated by "T".
      Applications using this syntax may choose, for the sake of
      readability, to specify a full-date and full-time separated by
      (say) a space character.
     */
    public static Date convertInetTimeFormatToJavaTime(final String internetTimeStamp) {
        //2010-07-29T09:41:32Z

        //date-time = full-date "T" full-time
        String[] timestampParts = internetTimeStamp.split("[T|t]");

        Date retValue = null;
        if (timestampParts.length >= 2) {

            //full-date = date-fullyear "-" date-month "-" date-mday
            String fullDate = timestampParts[0];
            String fullTime = timestampParts[1];

            //full-time = partial-time time-offset
            //partial-time  = time-hour ":" time-minute ":" time-second  [time-secfrac]
            //time-numoffset  = ("+" / "-") time-hour ":" time-minute
            //time-offset  = "Z" / time-numoffset
            // Z - A suffix which, when applied to a time, denotes a UTC
            //offset of 00:00; often spoken "Zulu" from the ICAO
            //phonetic alphabet representation of the letter "Z".
            String timeOffsetPattern = "([z|Z|])|([+|-]\\d\\d:\\d\\d)$";
            //String secondsFracPattern = "\\.\\d+";

            String partialTimePart = fullTime.replaceFirst(timeOffsetPattern, "");
            TimeZone timeZone = stringOffsetToTimeZone(fullTime.replace(partialTimePart, ""));

            String[] timeParts = partialTimePart.split("\\.");
            String timePart = timeParts[0];
            int secondsFrac = stringSecFracToMillis(timeParts.length > 1 ? timeParts[1] : null);

            String[] dateParts = fullDate.split("-");
            String[] timePartStrings = timePart.split(":");
            if (dateParts.length >= 3 && timePartStrings.length >= 3) {
                String yearPart = dateParts[0];
                String monthPart = dateParts[1];
                String datePart = dateParts[2];
                String hourPart = timePartStrings[0];
                String minutePart = timePartStrings[1];
                String secondPart = timePartStrings[2];


                Integer year = Integer.parseInt(yearPart);
                Integer month = Integer.parseInt(monthPart);
                Integer day = Integer.parseInt(datePart);
                Integer hour = Integer.parseInt(hourPart);
                Integer min = Integer.parseInt(minutePart);
                Integer sec = Integer.parseInt(secondPart);
                Integer millis = secondsFrac;

                final Calendar calendar = calendarThreadLocal.get();
                calendar.set(year, month - 1, day, hour, min, sec);
                calendar.set(Calendar.MILLISECOND, millis);

                if (timeZone != null) {
                    calendar.setTimeZone(timeZone);
                }

                retValue = calendar.getTime();
            } else {
                throw new IllegalArgumentException("'" + internetTimeStamp + "' can't be parsed");
            }
        } else {
            throw new IllegalArgumentException("'" + internetTimeStamp + "' can't be parsed");
        }

        return retValue;
    }

    private static TimeZone stringOffsetToTimeZone(final String offsetPart) {
        Integer offset = 0;
        if (offsetPart != null && !offsetPart.toUpperCase().equals("Z")) {
            String[] offsetParts = offsetPart.split(":");
            offset = Integer.parseInt(offsetParts[0]) * 1000 * 60 * 60;
            if (offsetParts.length > 1) {
                int minutesOffset = Integer.parseInt(offsetParts[1]) * 1000 * 60;
                offset = offset < 0 ? offset - minutesOffset : offset + minutesOffset;
            }
        }

        TimeZone retValue = null;
        String[] availableIDs = TimeZone.getAvailableIDs(offset);
        if (availableIDs != null && availableIDs.length > 0) {
            retValue = TimeZone.getTimeZone(availableIDs[0]);
        }

        return retValue;
    }

    private static int stringSecFracToMillis(final String millis) {

        String retValue = "0";
        if (millis != null) {

            if (millis.length() > 3) {
                String errMsg = "Fractional part of seconds must not be more than 3 digits. Now it has value '" + millis + "'";
                throw new IllegalArgumentException(errMsg);
            }

            int zerosToAppend = 3 - millis.length();
            retValue = appendString(millis, zerosToAppend < 0 ? 0 : zerosToAppend, "0");
        }
        return Integer.parseInt(retValue);
    }

    private static String appendString(final String source, final int count, final String appendix) {
        StringBuilder retValue = new StringBuilder(source);
        for (int i = 0; i < count; i++) {
            retValue.append(appendix);
        }
        return retValue.toString();
    }

    public static String convertJavaTimeToInetTimeFormat(Date date) {
        DateFormat yearDateFormat = yearFormaterThreadLocal.get();
        String formatedYear = yearDateFormat.format(date);

        DateFormat timeDateFormat = timeFormaterThreadLocal.get();
        String formatedTime = timeDateFormat.format(date);
        return String.format("%sT%sZ", formatedYear, formatedTime);
    }

    public static void main(String[] args) {
        String time = "2011-02-18T08:13:11.3Z";
        String time2 = "1996-12-19T16:39:57-08:00";

        //"1985-04-12T23:20:50.52Z"
        //"1996-12-19T16:39:57-08:00"
        //"1990-12-31T23:59:60Z"
        Date date = convertInetTimeFormatToJavaTime(time);
        Date date2 = convertInetTimeFormatToJavaTime(time2);
    }
}
