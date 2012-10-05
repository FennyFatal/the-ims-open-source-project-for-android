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

package javax.microedition.ims.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 19-Feb-2010
 * Time: 14:33:51
 */
public final class Logger {

    public static volatile boolean SUPRESS_LOGGING = false;

    public enum Tag {
        SHUTDOWN, COMMON, TRANSACTION, SIP_MESSAGE_OUT, SIP_MESSAGE_IN, MESSAGE_DISPATCHER, MESSAGE_HISTORY, PARSER, WARNING
    }

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final Map<Tag, Boolean[]> TAG_TABLE = Collections.synchronizedMap(new HashMap<Tag, Boolean[]>(Tag.values().length * 2));

    static {
        //[0] - show log message, [1] - show timing
        enableFullLogging();
    }

    public static void disableLogging() {
        TAG_TABLE.put(Tag.SHUTDOWN, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.COMMON, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.TRANSACTION, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_OUT, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_IN, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.MESSAGE_DISPATCHER, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.MESSAGE_HISTORY, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.PARSER, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.WARNING, new Boolean[]{false, true});
    }

    public static void enableFullLogging() {
        TAG_TABLE.put(Tag.SHUTDOWN, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.COMMON, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.TRANSACTION, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_OUT, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_IN, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.MESSAGE_DISPATCHER, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.MESSAGE_HISTORY, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.PARSER, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.WARNING, new Boolean[]{true, true});
    }

    public static void enableLoggingMode1() {
        TAG_TABLE.put(Tag.SHUTDOWN, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.COMMON, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.TRANSACTION, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_OUT, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_IN, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.MESSAGE_DISPATCHER, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.MESSAGE_HISTORY, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.PARSER, new Boolean[]{false, true});
        TAG_TABLE.put(Tag.WARNING, new Boolean[]{true, true});
    }

    public static void enableFullMode2() {
        TAG_TABLE.put(Tag.SHUTDOWN, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.COMMON, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.TRANSACTION, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_OUT, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_IN, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.MESSAGE_DISPATCHER, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.MESSAGE_HISTORY, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.PARSER, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.WARNING, new Boolean[]{true, true});
    }

    public static void enableFullMode3() {
        TAG_TABLE.put(Tag.SHUTDOWN, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.COMMON, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.TRANSACTION, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_OUT, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.SIP_MESSAGE_IN, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.MESSAGE_DISPATCHER, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.MESSAGE_HISTORY, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.PARSER, new Boolean[]{true, true});
        TAG_TABLE.put(Tag.WARNING, new Boolean[]{true, true});
    }

    private static final Method androidLogMethod = getAndroidLogger();

    private Logger() {
    }

    public static void log(Class<?> clazz, Tag tag, String prefix, String msg) {

        if (!SUPRESS_LOGGING) {

            //if show message for this tag
            if (TAG_TABLE.get(tag)[0]) {

                prefix = correctPrefix(clazz, tag, prefix);
                msg = correctMessage(clazz, tag, msg);

                //if show timing for this tag
                String time = null;
                if (TAG_TABLE.get(tag)[1]) {
                    long timeStamp = System.currentTimeMillis();
                    time = TIME_FORMAT.format(new Date(timeStamp));
                }

                if (androidLogMethod == null) {
                    String logMsg = time != null ? String.format("(%s)%s: %s", time, prefix, msg) : String.format("%s: %s", prefix, msg);

                    System.out.println(logMsg);
                }
                else {
                    androidLog(prefix, msg);
                }
            }
        }
    }

    private static Method getAndroidLogger() {
        String androidFile = System.getProperties().getProperty("android.vm.dexfile");
        String osName = System.getProperties().getProperty("os.name");

        Method androidLogMethod = null;
        if (osName.matches("Linux|linux")/* && Boolean.valueOf(androidFile) == Boolean.TRUE*/) {
            try {
                Class<?> androidLogger = Class.forName("android.util.Log");
                androidLogMethod = androidLogger.getMethod("i", String.class, String.class);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        return androidLogMethod;
    }

    private static void androidLog(String prefix, String msg) {
        try {
            androidLogMethod.invoke(null, prefix, msg);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static String correctMessage(final Class<?> clazz, final Tag tag, String msg) {
        if (Tag.SIP_MESSAGE_OUT == tag) {
            final String clazzName = clazz == null ? "?" : clazz.getSimpleName();
            msg = new StringBuilder("\n\nMessage being sent(" + clazzName + "):\n").
                    append("---------------------------------------------y>>>>\n").
                    append(msg).
                    append("\n---------------------------------------------^>>>>\n").
                    toString();

        }
        else if (Tag.SIP_MESSAGE_IN == tag) {
            final String clazzName = clazz == null ? "?" : clazz.getSimpleName();
            msg = new StringBuilder("\n\nIncoming message(" + clazzName + "):\n").
                    append("---------------------------------------------y<<<<\n").
                    append(msg).
                    append("\n---------------------------------------------^<<<<\n").
                    toString();
        }
        return msg;
    }


    private static String correctPrefix(final Class<?> clazz, final Tag tag, String prefix) {
        if (Tag.SHUTDOWN == tag) {
            prefix = clazz != null ? String.format("shutdown(%s)", clazz.getSimpleName()) : "shutdown";
        }
        else if (Tag.SIP_MESSAGE_OUT == tag) {
            //prefix = clazz.getSimpleName();
        }

        if (prefix == null || "".equals(prefix.trim())) {
            prefix = tag.toString();
        }
        return prefix;
    }

    public static void log(Class<?> clazz, Tag tag, String msg) {
        log(clazz, tag, "", msg);
    }

    public static void log(Class<?> clazz, String prefix, String msg) {
        log(clazz, Tag.COMMON, prefix, msg);
    }

    public static void log(Tag tag, String prefix, String msg) {
        log(null, tag, prefix, msg);
    }

    public static void log(String prefix, String msg) {
        log(Tag.COMMON, prefix, msg);
    }

    public static void logAsSplittedStrings(String prefix, String msg) {
        final String[] strings = msg.split("\r\n|\r|\n");
        for (String string : strings) {
            log(Tag.COMMON, prefix, string);
        }
    }

    public static void log(String msg) {
        log(Tag.COMMON, "", msg);
    }

    public static void log(Tag tag, String msg) {
        log(null, tag, "", msg);
    }
}
