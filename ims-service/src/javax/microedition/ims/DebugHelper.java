/*
 * This software code is � 2010 T-Mobile USA, Inc. All Rights Reserved.
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
 * THIS SOFTWARE IS PROVIDED ON AN �AS IS� AND �WITH ALL FAULTS� BASIS
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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Labada
 * Date: 21.9.2010
 * Time: 14.09.49
 * To change this template use File | Settings | File Templates.
 */
public final class DebugHelper {

    private static final ClassLoader RESOURCE_CLASS_LOADER = obtainClassLoader();
    private static final int BUFF_SIZE = 1024 * 64;

    private static ClassLoader obtainClassLoader() {

        final ClassLoader classLoader = DebugHelper.class.getClassLoader();
        Collection<URL> finalURLSet = null;

        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            final URL[] currentURLSet = urlClassLoader.getURLs();
            finalURLSet = new ArrayList<URL>(currentURLSet.length);

            for (URL url : currentURLSet) {
                final String urlExternalForm = url.toExternalForm();
                try {
                    if (urlExternalForm.endsWith("bin/")) {
                        URL newURL = new URL(urlExternalForm.replaceFirst("bin/", "src/"));
                        finalURLSet.add(newURL);
                    }
                    if(urlExternalForm.contains("out/production")){
                        String srcFolderCandidate = url.getPath().replace("out/production/", "")+"src/";
                        File folderCandidate = new File(srcFolderCandidate);
                        if(folderCandidate.exists() && folderCandidate.isDirectory()){
                            finalURLSet.add(new URL(url.toExternalForm().replace("out/production/", "")+"src/"));
                        }
                    }
                    finalURLSet.add(url);
                }
                catch (MalformedURLException e) {
                    System.out.println("Unable to create sibling for " + urlExternalForm);
                }
            }
        }


        return finalURLSet == null || finalURLSet.isEmpty() ?
                classLoader :
                new URLClassLoader(finalURLSet.toArray(new URL[finalURLSet.size()]));
    }

    public static final byte[] getResourceBytes(final String resourcePath) {
        byte[] retValue = null;

        try {
            final InputStream stream = RESOURCE_CLASS_LOADER.getResourceAsStream(resourcePath);
            if (stream != null) {
                BufferedInputStream in = new BufferedInputStream(stream, BUFF_SIZE);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(stream.available());
                byte[] dataChunk = new byte[BUFF_SIZE];

                int redBytes = 0;
                while ((redBytes = in.read(dataChunk)) != -1) {
                    byteArrayOutputStream.write(dataChunk, 0, redBytes);
                }
                in.close();
                byteArrayOutputStream.close();

                retValue = byteArrayOutputStream.toByteArray();
            }
        }
        catch (IOException e) {
            System.out.println("Unbale read stream for " + resourcePath);
        }

        return retValue;
    }

    public static final byte[] getResourceBytes(final Class<?> siblingClass, final String resourceName) {
        return getResourceBytes(siblingClass.getPackage().getName().replaceAll("\\.", "/") + "/" + resourceName);
    }

    private DebugHelper() {
    }
}
