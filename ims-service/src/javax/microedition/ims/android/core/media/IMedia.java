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

package javax.microedition.ims.android.core.media;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class IMedia implements Parcelable {
    //private static final String TAG = "IMedia-Service";

    private String type;
    private String address;
    private boolean isOwnAddress;
    private int port;
    private int numberOfPorts;
    private String protocol;
    private String format;
    private String information;
    private String direction;
    private String cryptoParam;
    //private String cryptoSuit;

    /**
     * Syntax:
     * <modifier>:<bandwidth-value>
     * <modifier> is a single alphanumeric word giving the meaning of the bandwidth figure.
     * <bandwidth-value> - is in kilobits per second
     */
    private final String[] bandwidth;
    
    private final String encryptionKey;
    
    /**
     * Syntax:
     * a=<attribute>
     * a=<attribute>:<value>
     */
    private final String[] attributes;
    
    private final ICryptoParam[] cryptoParams; 

    public static final Parcelable.Creator<IMedia> CREATOR = new Parcelable.Creator<IMedia>() {
        public IMedia createFromParcel(Parcel in) {
            return readFromParcel(in);
        }

        public IMedia[] newArray(int size) {
            return new IMedia[size];
        }
    };

    private IMedia(final IMediaBuilder builder) {
        this.type = builder.type;
        this.port = builder.port;
        this.address = builder.address;
        this.isOwnAddress = builder.isOwnAddress;
        this.numberOfPorts = builder.numberOfPorts;
        this.protocol = builder.protocol;
        this.format = builder.format;
        this.information = builder.information;
        this.bandwidth = builder.bandwidth;
        this.encryptionKey = builder.encryptionKey;
        this.attributes = builder.attributes;
        this.direction = builder.direction;
        this.cryptoParams = builder.cryptoParams;
    }
    
    public String getType() {
        return type;
    }

    public int getPort() {
        return port;
    }

    public int getNumberOfPorts() {
        return numberOfPorts;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getFormat() {
        return format;
    }

    public String getInformation() {
        return information;
    }

    public String[] getBandwidth() {
        return bandwidth;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public String getDirection() {
        return direction;
    }
    
    public String getAddress() {
        return address;
    }
    
    public boolean isOwnAddress() {
        return isOwnAddress;
    }
    
    public ICryptoParam[] getCryptoParams() {
        return cryptoParams;
    }

/*    public void setAddress(String address) {
        this.address = address;
    }
*/

    public void writeToParcel(Parcel dest, int flags) {
        //Logger.log(TAG, "writeToParcel#started");
        dest.writeString(type);
        dest.writeInt(port);
        dest.writeString(address);
        dest.writeByte((byte)(isOwnAddress? 1: 0));
        dest.writeInt(numberOfPorts);
        dest.writeString(protocol);
        dest.writeString(format);
        dest.writeString(information);
        dest.writeString(encryptionKey);
        dest.writeString(direction);
        
        int cryptoSize = cryptoParams != null? cryptoParams.length: -1;
        dest.writeInt(cryptoSize);
        if(cryptoSize > -1) {
            for(ICryptoParam cryptoParam: cryptoParams) {
            	cryptoParam.writeToParcel(dest, flags);
            }
        }
        
        if(bandwidth == null  || bandwidth.length == 0){
            dest.writeInt(0);
        }
        else {
            dest.writeInt(bandwidth.length);
            dest.writeStringArray(bandwidth);
        }
        if (attributes == null || attributes.length == 0) {
            dest.writeInt(0);
        }
        else {
            dest.writeInt(attributes.length);
            dest.writeStringArray(attributes);
        }
        //Logger.log(TAG, "writeToParcel#finished");
    }

    protected static IMedia readFromParcel(Parcel dest) {
        //Logger.log(TAG, "readFromParcel#started");
    	IMediaBuilder builder = new IMediaBuilder()
    		.type(dest.readString())
    		.port(dest.readInt())
        	.address(dest.readString())
        	.isOwnAddress(dest.readByte() == 0? false: true)
        	.numberOfPorts(dest.readInt())
        	.protocol(dest.readString())
        	.format(dest.readString())
        	.information(dest.readString())
        	.encryptionKey(dest.readString())       
        	.direction(dest.readString());
    	
        int cryproSize = dest.readInt();
        if(cryproSize > -1){
        	ICryptoParam[] cryptoParams = ICryptoParam.CREATOR.newArray(cryproSize);
        	for(int  i = 0; i < cryproSize; i++) {
        		ICryptoParam cryptoParam = ICryptoParam.CREATOR.createFromParcel(dest);
        		cryptoParams[i] = cryptoParam;
        	}
            builder.cryptoParams(cryptoParams);
        } 
    	
        int bandSize = dest.readInt();
        //Logger.log("Reading band: ",length+" ");
        if(bandSize > 0){
            String[] bandwidthes = new String[bandSize];
            dest.readStringArray(bandwidthes);
            builder.bandwidthes(bandwidthes);
        } 
        
        int attrSize = dest.readInt();
        //Logger.log("Reading attributes: ",length+" ");
        if(attrSize > 0){
            String[] attributes = new String[attrSize];
            dest.readStringArray(attributes);
            builder.attributes(attributes);
        }
        //Logger.log("Reading done"," ");
        //Logger.log(TAG, "readFromParcel#finished");
        return  builder.build();
    } 
    
    public int describeContents() {
        return 0;
    }

    public static class IMediaBuilder {
        private String type;
        private String address;
        private boolean isOwnAddress;
        private int port;
        private int numberOfPorts;
        private String protocol;
        private String format;

        private String information;
        //private ConnectionInfo  connectionInfo;
        private String[] bandwidth;
        private String encryptionKey;
        private String[] attributes;
        private String direction; 
        private ICryptoParam[] cryptoParams;

        public IMediaBuilder type(String type) {
            this.type = type;
            return this;
        }
        
        public IMediaBuilder cryptoParams(ICryptoParam... cryptoParams){
            this.cryptoParams = cryptoParams;
            return this;
        }
        
        public IMediaBuilder address(String address){
            this.address = address;
            return this;
        }

        public IMediaBuilder isOwnAddress(boolean isOwnAddress){
            this.isOwnAddress = isOwnAddress;
            return this;
        }
        
        public IMediaBuilder port(int port) {
            this.port = port;
            return this;
        }

        public IMediaBuilder numberOfPorts(int numberOfPorts) {
            this.numberOfPorts = numberOfPorts;
            return this;
        }

        public IMediaBuilder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public IMediaBuilder format(String format) {
            this.format = format;
            return this;
        }

        public IMediaBuilder information(String information) {
            this.information = information;
            return this;
        }
        
        public IMediaBuilder bandwidthes(String... bandwidthes){
            this.bandwidth = bandwidthes;
            return this;
        }

        public IMediaBuilder encryptionKey(String encryptionKey) {
            this.encryptionKey = encryptionKey;
            return this;
        }

        public IMediaBuilder attributes(String... attributes){
            this.attributes = attributes;
            return this;
        }

        public IMediaBuilder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public IMedia build() {
            return new IMedia(this);
        }
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + Arrays.hashCode(attributes);
        result = prime * result + Arrays.hashCode(bandwidth);
        result = prime * result + ((cryptoParam == null) ? 0 : cryptoParam.hashCode());
        result = prime * result + Arrays.hashCode(cryptoParams);
        result = prime * result + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result + ((encryptionKey == null) ? 0 : encryptionKey.hashCode());
        result = prime * result + ((format == null) ? 0 : format.hashCode());
        result = prime * result + ((information == null) ? 0 : information.hashCode());
        result = prime * result + numberOfPorts;
        result = prime * result + port;
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IMedia other = (IMedia)obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (!Arrays.equals(attributes, other.attributes))
            return false;
        if (!Arrays.equals(bandwidth, other.bandwidth))
            return false;
        if (cryptoParam == null) {
            if (other.cryptoParam != null)
                return false;
        } else if (!cryptoParam.equals(other.cryptoParam))
            return false;
        if (!Arrays.equals(cryptoParams, other.cryptoParams))
            return false;
        if (direction == null) {
            if (other.direction != null)
                return false;
        } else if (!direction.equals(other.direction))
            return false;
        if (encryptionKey == null) {
            if (other.encryptionKey != null)
                return false;
        } else if (!encryptionKey.equals(other.encryptionKey))
            return false;
        if (format == null) {
            if (other.format != null)
                return false;
        } else if (!format.equals(other.format))
            return false;
        if (information == null) {
            if (other.information != null)
                return false;
        } else if (!information.equals(other.information))
            return false;
        if (numberOfPorts != other.numberOfPorts)
            return false;
        if (port != other.port)
            return false;
        if (protocol == null) {
            if (other.protocol != null)
                return false;
        } else if (!protocol.equals(other.protocol))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "IMedia [type=" + type + ", address=" + address + ", isOwnAddress=" + isOwnAddress
                + ", port=" + port + ", numberOfPorts=" + numberOfPorts + ", protocol=" + protocol
                + ", format=" + format + ", information=" + information + ", direction="
                + direction + ", cryptoParam=" + cryptoParam + ", bandwidth="
                + Arrays.toString(bandwidth) + ", encryptionKey=" + encryptionKey + ", attributes="
                + Arrays.toString(attributes) + ", cryptoParams=" + Arrays.toString(cryptoParams)
                + "]";
    }
}
