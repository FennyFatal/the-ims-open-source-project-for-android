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

import java.util.Set;

public interface Environment {
    /*static Codec[] codecs = new Codec[]{
    {  0,  "PCMU",    8000, 1, 0 },
    {  3,  "GSM",     8000, 1, 0 },
    {  4,  "G723",    8000, 1, 0 },
    {  5,  "DVI4",    8000, 1, 0 },
    {  6,  "DVI4",   16000, 1, 0 },
    {  7,   "LPC",    8000, 1, 0 },
    {  8,  "PCMA",    8000, 1, 0 },
    {  9,  "G722",    8000, 1, 0 },
    { 10,   "L16",   44100, 2, 0 },
    { 11,   "L16",   44100, 1, 0 },
    { 12, "QCELP",    8000, 1, 0 },
    { 13,    "CN",    8000, 1, 0 },
    { 15,  "G728",    8000, 1, 0 },
    { 16,  "DVI4",   11025, 1, 0 },
    { 17,  "DVI4",   22050, 1, 0 },
    { 18,  "G729",    8000, 1, 0 },
    { 25,  "CelB",   90000, 1, 1 },
    { 26,  "JPEG",   90000, 1, 1 },
    { 28,  "nv",   90000, 1, 1 },
    { 31,  "H261",   90000, 1, 1 },
    { 32,  "MPV",   90000, 1, 1 },
    { 32,  "MP2T",   90000, 1, 1 },
    { 34,  "H263",   90000, 1, 1 }
};*/
    
    static enum MediaType {
        AUDIO, VIDEO
    }

    Set<Codec> getCodecs(MediaType mediaType);
    
    boolean isForceSrtp();
    void setForceSrtp(boolean forceSrtp);
    
    String getSrtpCryptoAlgorithm();
}
