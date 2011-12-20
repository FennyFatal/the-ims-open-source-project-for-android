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

package javax.microedition.ims;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry is the storage for configurational data for each application QoS
 * level selection is incremental; level 1 (QOS_LEVEL_BACKGROUND) covers only
 * level 1, level 2 covers levels 1 and 2, level 3 covers levels 1, 2 and 3.
 * Level 4 (QOS_LEVEL_STREAMING) covers all levels from 1 to 4.
 */
public final class Registry {
    /**
     * Indicates that the the level of QoS only covers background activities,
     * i.e. REGISTER, SUBSCRIBE, NOTIFY, PUBLISH
     */
    public static final int QOS_LEVEL_BACKGROUND = 1;

    /**
     * Indicates that the the level of QoS covers background activities and
     * interactive actions like SIP MESSAGE based chats.
     */
    public static final int QOS_LEVEL_INTERACITVE = 2;

    /**
     * Indicates that the the level of QoS covers background
     * activities,interactive actions and conversational elements like
     * MSRP-Sessions, VoIP calls etc.
     */
    public static final int QOS_LEVEL_CONVERSATIONAL = 3;

    /**
     * Indicates that the the level of QoS covers background
     * activities,interactive and conversational actions and streaming like
     * video-calls and similar media streams.
     */
    public static final int QOS_LEVEL_STREAMING = 4;

    // TODO ?
    private final String[] contentTypes;
    private final int qosLevel;
    private final String[][] properties;

    /**
     * Create registry instance
     * 
     * @param properties
     *            - array of properties
     * @param contentTypes
     *            Application to propose QosLevels prior REGISTER
     * @param qosLevel
     *            qosLevel indicates the requested level of QoS for the lifetime
     *            of the application. via the RegistryListener IMS Stack
     *            indicates which level is available for delivery. The intended
     *            level is not guaranteed until the
     *            <code>RegistryListener.QosLevelStatusSignal(int)</code> has
     *            signaled what level is available. The discovery is based on
     *            network selection that might take place during the SIP
     *            REGISTER and around that time the level is indicated via the
     *            Listener-interface.
     * 
     * @throws IllegalArgumentException
     *             - if the properties argument is null
     */
    public Registry(String[][] properties, String[] contentTypes, int qosLevel) {
        if (properties == null) {
            throw new IllegalArgumentException(
                    "If the properties argument is null");
        }
        this.properties = properties;

        this.contentTypes = contentTypes;
        this.qosLevel = qosLevel;
    }

    /**
     * QoS level is determined by the IMS Stack based on discovered network
     * capabilities etc.. might differ from the initial proposition the
     * application did prior register during constructing the instance of this
     * class.
     * 
     * @return
     */
    public int getQosLevel() {
        return qosLevel;
    }

    /**
     * List of content-types the application is registered to accept.
     * 
     * @return
     */
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    public String[][] getProperties() {
        return properties;
    }

    boolean isValid() {
        // TODO not implemented yet
        boolean isValid = true;
        
        Map<PropertyValidator, Integer> frequencyCounter = new HashMap<PropertyValidator, Integer>();
        
        //check property uniqueness
        for(String[] property : properties) {
            String propertyKey = property[0];
            PropertyValidator validator = PropertyValidator.parse(propertyKey);
            if(validator != null) {
                Integer counter = frequencyCounter.get(validator);
                if(counter == null) {
                    frequencyCounter.put(validator, counter = new Integer(0));
                }
                counter++;
                
                if(validator.isUnique() && counter > 1) {
                    isValid = false;
                    break;
                }
            }
        }
        
        //check property validity
        if(isValid) {
            for(String[] property : properties) {
                String propertyKey = property[0];
                PropertyValidator validator = PropertyValidator.parse(propertyKey);
                if(validator != null) {
                    isValid = validator.isValid();
                    if(!isValid) break;
                }
            }    
        }
        return isValid;
    }

    private static enum PropertyValidator {
        Stream(true) {
           
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            } 
        }, 
        Framed(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, 
        Basic(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, 
        Event(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        },
        CoreService(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, 
        Qos(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, 
        Reg(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, 
        Write(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        },
        Read(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, 
        Cap(false) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, Mprof(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, Connection(true) {
            
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, Pager(true) {
            @Override
            public boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, Auth(true) {
            @Override
            boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }, XdmAuth(true) {
            @Override
            boolean isValid() {
                // TODO Auto-generated method stub
                return true;
            }
        }
        ;
        
        private final boolean isUnique;
        
        private PropertyValidator(boolean isUnique) {
            this.isUnique = isUnique;
        }
        
        abstract boolean isValid(); 

        private static PropertyValidator parse(String property) {
            return Enum.valueOf(PropertyValidator.class, property);
        }
        
        private boolean isUnique() {
            return isUnique;
        }
    }

    
    public String toString() {
        return "Registry [contentTypes=" + Arrays.toString(contentTypes)
                + ", properties=" + Arrays.toString(properties) + ", qosLevel="
                + qosLevel + "]";
    }
}
