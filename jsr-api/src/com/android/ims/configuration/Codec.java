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

package com.android.ims.configuration;


/**
 * Wrapper for codec.
 *
 * @author ext-akhomush
 */
public class Codec {
    private int type;
    private String name;
    private int clockRate;
    private int channels;

    public Codec(int type, String name, int clockRate, int channels) {
        this.type = type;
        this.name = name;
        this.clockRate = clockRate;
        this.channels = channels;
    }

    /**
     * Create from string.
     * Format: <type> <name>/<clockRate> <channels>
     *
     * @param source - source string
     * @return
     * @throws IllegalArgumentException - if string cann't be parsed properly
     */
    public static Codec valueOf(String source) {
        Codec codec;
        try {
            codec = createFromString(source);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NumberFormatException, exp: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Illegal argument, source: " + source);
        }
        return codec;

    }

    private static Codec createFromString(String source) throws NumberFormatException {
        String[] exps = source.split("/");
        String[] firstPart = exps[0].split(" ");
        String[] secondPart = exps[1].split(" ");

        int type = Integer.parseInt(firstPart[0]);
        String name = firstPart[1];

        int clockRate = Integer.parseInt(secondPart[0]);
        int channels = secondPart.length == 2 ? Integer.parseInt(secondPart[1]) : 0;

        return new Codec(type, name, clockRate, channels);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClockRate() {
        return clockRate;
    }

    public void setClockRate(int clockRate) {
        this.clockRate = clockRate;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + clockRate;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Codec other = (Codec) obj;
        if (clockRate != other.clockRate)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    
    public String toString() {
        return "Codec [channels=" + channels + ", clockRate=" + clockRate
                + ", name=" + name + ", type=" + type + "]";
    }

    /**
     * Format Codec object to string:
     * Format:
     * if channels > 0
     * <type> <name>/<clockRate> <channels>
     * otherwise
     * <type> <name>/<clockRate>
     *
     * @return
     */
    public String getContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(" ").append(name).append("/").append(clockRate);
        if (channels > 0) {
            sb.append(" ").append(channels);
	    }
	    return sb.toString();
	}
}
