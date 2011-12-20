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

package com.android.ims.presence;

public final class PresenceDocumentShemas {
    public static final String NAMESPACE_PIDF = "urn:ietf:params:xml:ns:pidf";
    
    public static final String NAMESPACE_OMA_PRES = "urn:oma:xml:prs:pidf:oma-pres";
    public static final String NAMESPACE_OMA_PRES_PREFIX = "op";

    public static final String NAMESPACE_DATA_MODEL = "urn:ietf:params:xml:ns:pidf:data-model";
    public static final String NAMESPACE_DATA_MODEL_PREFIX = "pdm";

    public static final String NAMESPACE_RPID = "urn:ietf:params:xml:ns:pidf:rpid";
    public static final String NAMESPACE_RPID_PREFIX = "rpid";
    
    public static final String NAMESPACE_GP = "urn:ietf:params:xml:ns:pidf:geopriv10";
    public static final String NAMESPACE_GP_PREFIX = "gp";
    
    public static final String NAMESPACE_GML = "urn:opengis:specification:gml:schema-xsd:feature:v3.0";
    public static final String NAMESPACE_GML_PREFIX = "gml";
    
    public static final String NAMESPACE_CL = "urn:ietf:params:xml:ns:pidf:geopriv10:civicLoc";
    public static final String NAMESPACE_CL_PREFIX = "cl";

    public static final String NAMESPACE_GS = "http://www.opengis.net/pidflo/1.0";
    public static final String NAMESPACE_GS_PREFIX = "gs";
    
    private PresenceDocumentShemas() {
        assert false;
    }
}
