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

import java.util.ArrayList;
import java.util.List;


public class AcceptContactDescriptor {
    private final List<Pair> pairs = new ArrayList<Pair>();
    private boolean explicit = false;
    private boolean require = false;

    public static AcceptContactDescriptor valueOf(String source) {
        AcceptContactDescriptor descriptor = new AcceptContactDescriptor();

        String[] params = source.split(";");
        for (String param : params) {
            Pair pair = Pair.valueOf(param.trim());
            descriptor.addRecord(pair);
        }
        return descriptor;
    }

    public void addRecord(Pair pair) {
        pairs.add(pair);
        if (pair.getKey().compareToIgnoreCase("explicit") == 0) {
            explicit = true;
        }
        else if (pair.getKey().compareToIgnoreCase("require") == 0) {
            require = true;
        }
    }

    public Pair[] getRecords() {
        return pairs.toArray(new Pair[pairs.size()]);
    }

    public boolean isExplicit() {
        return explicit;
    }

    public boolean isRequire() {
        return require;
    }

    public boolean contains(Pair pair) {
        for (Pair p : pairs) {
            if (p.equals(pair)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFreeFromTag(String tag) {
        for (Pair p : pairs) {
            if (p.getKey().equalsIgnoreCase(tag)) {
                return false;
            }
        }
        return true;
    }

    
    public String toString() {
        return "AcceptContactDescriptor [pairs=" + pairs + "]";
    }

    public String getContent() {
        StringBuilder res = new StringBuilder();
        boolean first = true;

        for (Pair p : pairs) {
            if (first) {
                first = false;
            }
            else {
                res.append(";");
            }
            res.append(p.getContent());
        }
        return res.toString();
    }


    public static class Pair {
        private final String key;
        private final String value;

        private static Pair valueOf(String source) {
            final Pair pair;

            String[] values = source.split("=");

            if (values.length > 1) {
                pair = new Pair(values[0], values[1].replace("\"", ""));
            }
            else {
                pair = new Pair(values[0]);
            }
            return pair;
        }

        public Pair(String key) {
            this(key, null);
        }

        public Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getContent() {
            if (value != null) {
                return key + "=\"" + value + "\"";
            }
            return key;
        }

        
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Pair other = (Pair) obj;
            if (key == null) {
                if (other.key != null) {
                    return false;
                }
            }
            else if (!key.equals(other.key)) {
                return false;
            }
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            }
            else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Pair [key=" + key + ", value=" + value + "]";
        }
    }

}
