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

package javax.microedition.ims.core.xdm.data;

import javax.microedition.ims.common.Logger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: Pavel Laboda (pavel.laboda@gmail.com)
 * Date: 26.4.2010
 * Time: 16.18.04
 */
public class DocumentDataBean implements DocumentBean {
    private static final SimpleDateFormat df;

    private final String uri;
    private final String etag;
    private final Long lastModified;
    private final Integer size;
    private final String documentSelector;

    static {
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
    }

    public DocumentDataBean(String uri, String etag, String lastModified, String size) {
        this.uri = uri;
        this.documentSelector = uri.replaceAll("^https?://.*?/", "");

        this.etag = etag;

        Date date = parseISO8601Date(lastModified);
        this.lastModified = (date != null ? date.getTime() : 0);

        Integer sizeValue = null;

        try {
            sizeValue = Integer.parseInt(size);
        }
        catch (NumberFormatException e) {
            //do nothing. Use null in case of parsing error.
        }

        this.size = sizeValue;
    }

    public String getUri() {
        return uri;
    }

    
    public String getEtag() {
        return etag;
    }

    
    public Long getLastModified() {
        return lastModified;
    }

    
    public Integer getSize() {
        return size;
    }

    
    public String getDocumentSelector() {
        return documentSelector;
    }

    /**
     * Parse date in ISO8601 format.
     * For more details see http://www.ietf.org/rfc/rfc3339.txt
     *
     * @param input
     * @return
     */
    private static Date parseISO8601Date(String input) {
        Date retValue = null;

        if (input.endsWith("Z")) {
            input = input.substring(0, input.length() - 1) + "GMT-00:00";
        }
        else {
            int inset = 6;

            String s0 = input.substring(0, input.length() - inset);
            String s1 = input.substring(input.length() - inset, input.length());

            input = s0 + "GMT" + s1;
        }

        try {
            retValue = df.parse(input);
        }
        catch (ParseException e) {
            Logger.log("DocumentDateBean#parseDate", e.getMessage());
        }

        return retValue;
    }


    public String toString() {
        return "DocumentDataBean{" +
                "uri='" + uri + '\'' +
                ", etag='" + etag + '\'' +
                ", lastModified='" + lastModified + '\'' +
                ", size='" + size + '\'' +
                '}';
    }
}
