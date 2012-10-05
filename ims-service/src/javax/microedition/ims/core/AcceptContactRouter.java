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

import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.util.CollectionsUtils;
import javax.microedition.ims.core.AcceptContactDescriptor.Pair;
import javax.microedition.ims.core.registry.ClientRegistry;
import javax.microedition.ims.core.registry.property.BasicProperty;
import javax.microedition.ims.core.registry.property.CoreServiceProperty;
import javax.microedition.ims.core.registry.property.EventProperty;
import javax.microedition.ims.core.registry.property.StreamProperty;
import javax.microedition.ims.core.registry.property.StreamProperty.StreamType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AcceptContactRouter {
    private AcceptContactRouter() {
        assert false;
    }

    enum FeatureTag {
        VIDEO("video"),
        AUDIO("audio"),
        MESSAGE("message"),
        EVENTS("events"),
        APPLICATION("application"),
        APP_SUBTYPE("app-subtype"),
        IARI_REF("+g.3gpp.iari-ref"),
//        ISCI_REF("+3gpp.icsi-ref"),
        ISCI_REF("+g.3gpp.icsi-ref"),
        APP_REF("+g.3gpp.app-ref");

        private static final Map<String, FeatureTag> mapping = new HashMap<String, FeatureTag>();

        static {
            for (FeatureTag featureTag : values()) {
                mapping.put(featureTag.stringValue.toLowerCase(), featureTag);
            }
        }

        private final String stringValue;

        private FeatureTag(String stringValue) {
            this.stringValue = stringValue;
        }

        public static FeatureTag parse(String value) {
            return mapping.get(value.toLowerCase());
        }
    }

    public static String lookupBestComsumer(ClientRegistry[] clientRegistries, AcceptContactDescriptor[] contacts) {
        final String retValue;

        Map<String, Float> comsumers = lookupComsumers(clientRegistries, contacts);

        //when all cs have been processed:
        //select the best cs to receive the request
        Entry<String, Float> maxScoreEntry = null;

        for (Entry<String, Float> entry : comsumers.entrySet()) {
            if (maxScoreEntry == null || entry.getValue() > maxScoreEntry.getValue()) {
                maxScoreEntry = entry;
            }
        }

        if (maxScoreEntry != null) {
            Logger.log("lookupComsumer#maxScore: " + maxScoreEntry.getValue());
            Logger.log("lookupComsumer#appId: " + maxScoreEntry.getKey());
            retValue = maxScoreEntry.getKey();
        }
        else {
            Logger.log("lookupComsumer#Can't retrieve consumer");
            retValue = null;
        }

        return retValue;
    }

    /**
     * @param stackRegistry
     * @param acceptHeaders
     * @return
     */
    public static Map<String, Float> lookupComsumers(ClientRegistry[] clientRegistries, AcceptContactDescriptor[] contacts) {
        /*
         * R.prop - gets the value of IMS property with name prop from the Registry R
         * npf - number of features in "Accept-Contact"
         * ncf - number of "Accept-Contact" feature tags where the Registry implies the same tag
         * nvm - number of value matches between "Accept-Contact" and Registry for a feature tag
         * nvm_cs - number of matches for IARI, ICSI and Feature Tags fields in the CoreService property for a given cs
         * nf_cs - total number of features implied from IARI, ICSI and Feature Tags fields in the CoreService property for a given cs
         * Score[h] - an Accept-Contact header score for the cs
         */

        /*
        * Accept-Contact: *; application; app-subtype="myChess"; message;events="Presence"
        * Accept-Contact: *; +g.3gpp.iari-ref="urn:IMSAPI:com.myCompany.iari.myChess"; require
        */

        final Map<String, Float> csScores = new HashMap<String, Float>(clientRegistries.length);

        //For each registry R with an active core service cs
        for (ClientRegistry registry : clientRegistries) {

            int nvm_cs = 0;
            int nf_cs = 0;

            boolean isClientDropped = false;

            //For each accept-contact header
            float[] acScores = new float[contacts.length];
            for (int h = 0; h < contacts.length && !isClientDropped; h++) {
                final AcceptContactDescriptor acceptContact = contacts[h];

                int npf = 0;
                int ncf = 0;
                int nvm = 0;

                //For each feature f in that accept-contact
                final Pair[] features = acceptContact.getRecords();
                for (Pair feature : features) {
                    final String featureKey = feature.getKey();
                    final String featureValue = feature.getValue();
                    final FeatureTag featureTag = FeatureTag.parse(featureKey);

                    if (featureTag != null) {
                        npf++;
                        switch (featureTag) {
                            case VIDEO: {
                                final StreamProperty streamProperty = registry.getStreamProperty();
                                final BasicProperty basicProperty = registry.getBasicProperty();

                                //if Video is in R.Stream or video is in R.Basic then
                                if ((streamProperty != null && streamProperty.getTypes().contains(StreamType.VIDEO))
                                        || (basicProperty != null && Arrays.binarySearch(basicProperty.getContentTypes(), "video") > -1)) {
                                    ncf++;
                                    nvm++;
                                }
                                break;
                            }
                            case AUDIO: {
                                final StreamProperty streamProperty = registry.getStreamProperty();
                                final BasicProperty basicProperty = registry.getBasicProperty();

                                //if Audio is in R.Stream or audio is in R.Basic then
                                if ((streamProperty != null && streamProperty.getTypes().contains(StreamType.AUDIO))
                                        || (basicProperty != null && Arrays.binarySearch(basicProperty.getContentTypes(), "audio") > -1)) {
                                    ncf++;
                                    nvm++;
                                }
                                break;
                            }
                            case MESSAGE: {
                                //if R.Framed is defined then
                                if (registry.getFrameProperty() != null) {
                                    ncf++;
                                    nvm++;
                                }
                                break;
                            }
                            case EVENTS: {
                                //if R.Event is defined then
                                final EventProperty property = registry.getEventProperty();
                                if (property != null) {
                                    ncf++;
                                    //if f.value is_in R.Event then
                                    if (Arrays.binarySearch(property.getPackages(), featureValue) > -1) {
                                        nvm++;
                                    }
                                }
                                break;
                            }
                            case APPLICATION: {
                                //if application is a top-level type in R.Basic then
                                final BasicProperty basicProperty = registry.getBasicProperty();
                                if (basicProperty != null) {
                                    final List<String> types = Arrays.asList(basicProperty.getContentTypes());
                                    boolean applicationExists = CollectionsUtils.exists(types, new CollectionsUtils.Predicate<String>() {

                                        public boolean evaluate(String type) {
                                            return type.startsWith("application/");
                                        }
                                    });
                                    if (applicationExists) {
                                        ncf++;
                                        nvm++;
                                    }
                                }
                                break;
                            }
                            case APP_SUBTYPE: {
                                //if application is a top-level type in R.Basic then
                                final BasicProperty basicProperty = registry.getBasicProperty();
                                if (basicProperty != null) {
                                    List<String> types = Arrays.asList(basicProperty.getContentTypes());
                                    boolean applicationExists = CollectionsUtils.exists(types, new CollectionsUtils.Predicate<String>() {

                                        public boolean evaluate(String type) {
                                            return type.startsWith("application/");
                                        }
                                    });

                                    if (applicationExists) {
                                        ncf++;

                                        //if some value of f occurs as sub-level of an application content type in R.Basic then
                                        boolean subtypeExists = types.contains("application/" + featureValue);

                                        if (subtypeExists) {
                                            nvm++;
                                        }
                                    }
                                }
                                break;
                            }
                            case IARI_REF: {
                                final CoreServiceProperty coreServiceProperty = registry.getCoreServiceProperty();

                                //if R.CoreService contains an iari
                                if (coreServiceProperty != null) {
                                    final String[] iAris = coreServiceProperty.getIARIs();
                                    if (iAris.length > 0) {
                                        ncf++;
                                        //if f.value is_in iari for R.CoreService with name cs then
                                        boolean iAriExists = CollectionsUtils.exists(Arrays.asList(iAris), new CollectionsUtils.Predicate<String>() {

                                            public boolean evaluate(String iAri) {
                                                return featureValue.equalsIgnoreCase(iAri);
                                            }
                                        });
                                        if (iAriExists) {
                                            nvm++;
                                            nvm_cs++;
                                        }
                                    }
                                }
                                break;
                            }
                            case ISCI_REF: {
                                final CoreServiceProperty coreServiceProperty = registry.getCoreServiceProperty();

                                //if R.CoreService contains some ICSI
                                if (coreServiceProperty != null) {
                                    final String[] iCsis = coreServiceProperty.getICSIs();
                                    if (iCsis.length > 0) {
                                        ncf++;
                                        //if f.value is_in icsi for R.CoreService with name cs then
                                        boolean iCsisExists = CollectionsUtils.exists(Arrays.asList(iCsis), new CollectionsUtils.Predicate<String>() {

                                            public boolean evaluate(String iCsi) {
                                                return featureValue.equalsIgnoreCase(iCsi);
                                            }
                                        });
                                        if (iCsisExists) {
                                            nvm++;
                                            nvm_cs++;
                                        }
                                    }
                                }
                                break;
                            }
                            case APP_REF: {
                                final CoreServiceProperty coreServiceProperty = registry.getCoreServiceProperty();

                                //if R.CoreService contains some IARI or some ICSI
                                if (coreServiceProperty != null) {
                                    final String[] iAris = coreServiceProperty.getIARIs();
                                    final String[] iCsis = coreServiceProperty.getICSIs();
                                    if (iAris.length > 0 || iCsis.length > 0) {
                                        ncf++;
                                        //if f.value is_in iari or f.value is_in icsi for R.CoreService with name cs then

                                        boolean iAriExists = CollectionsUtils.exists(Arrays.asList(iAris), new CollectionsUtils.Predicate<String>() {
                                            public boolean evaluate(String iAri) {
                                                return featureValue.equalsIgnoreCase(iAri);
                                            }
                                        });

                                        boolean iCsisExists = CollectionsUtils.exists(Arrays.asList(iCsis), new CollectionsUtils.Predicate<String>() {
                                            public boolean evaluate(String iCsi) {
                                                return featureValue.equalsIgnoreCase(iCsi);
                                            }
                                        });


                                        if (iAriExists || iCsisExists) {
                                            nvm++;
                                            nvm_cs++;
                                        }
                                    }
                                }
                                break;
                            }
                            default: {
                                Logger.log("lookupComsumer#unhandled featureTag: " + featureTag);
                            }
                        }
                    }
                    else {
                        final CoreServiceProperty coreServiceProperty = registry.getCoreServiceProperty();

                        //if f.tag is contained in Feature Tags for R.CoreService with name cs 
                        boolean featureTagExists = coreServiceProperty != null
                                && CollectionsUtils.exists(Arrays.asList(coreServiceProperty.getFeatureTags()), new CollectionsUtils.Predicate<String>() {
                            public boolean evaluate(String featureValue) {
                                return featureKey.equals(featureValue);
                            }
                        });

                        if (featureTagExists) {
                            ncf++;

                            //String featureTag = featureValue.split(";")[0];
                            boolean featureValueExists = CollectionsUtils.exists(Arrays.asList(coreServiceProperty.getFeatureTags()), new CollectionsUtils.Predicate<String>() {
                                
                                public boolean evaluate(String featureTagValue) {
                                    String[] fValues = featureTagValue.split("=");
                                    return fValues.length == 2 && featureValue.equals(fValues[1]);
                                }
                            });
                            if (featureValueExists) {
                                nvm++;
                                nvm_cs++;
                            }
                        }
                        else {
                            //else if f.tag is handled by the device
                            Logger.log("lookupComsumer#unhandled featureKey: " + featureKey);
                        }
                    }
                }

                //when an "Accept-Contact" header has been processed for this cs:
                float asScore = ((float) nvm) / npf;
                if (asScore < 1.0) {
                    final List<Pair> records = Arrays.asList(acceptContact.getRecords());
                    boolean isRequireExists = CollectionsUtils.exists(records, new CollectionsUtils.Predicate<Pair>() {
                        public boolean evaluate(Pair pair) {
                            return pair.getKey().equals("require");
                        }
                    });

                    boolean isExplicitExists = CollectionsUtils.exists(records, new CollectionsUtils.Predicate<Pair>() {
                        public boolean evaluate(Pair pair) {
                            return pair.getKey().equals("explicit");
                        }
                    });

                    //if the "require" flag is present for this "Accept-Contact" header, then
                    if (isRequireExists) {
                        //cs is dropped, and next candidate cs is tried.
                        isClientDropped = true;
                        break;
                    }
                    //else  if the "explicit" flag is present, then
                    else if (isExplicitExists) {
                        asScore = 0;
                    }
                }

                acScores[h] = asScore;
            }

            if (!isClientDropped) {
                //when all headers for a cs is done: 
                if (nvm_cs < nf_cs) {
                    //cs is dropped
                }
                else {
                    //caller preferences score of this cs (Sc[cs] ) is the arithmetic average of Score[h] for all its "Accept-Contact" headers h
                    float averageACScore = calculateAverage(acScores);
                    csScores.put(registry.getAppId(), averageACScore);
                }
            }
        }

        return csScores;
    }

    private static float calculateAverage(float[] source) {
        final float retValue;

        float sum = 0;
        for (float acScore : source) {
            sum += acScore;
        }

        retValue = sum / source.length;
        return retValue;
    }
}
