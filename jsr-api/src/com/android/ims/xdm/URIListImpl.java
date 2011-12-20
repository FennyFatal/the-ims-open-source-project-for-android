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

package com.android.ims.xdm;

import com.android.ims.util.CollectionsUtils;

import javax.microedition.ims.ServiceClosedException;
import javax.microedition.ims.xdm.ListEntry;
import javax.microedition.ims.xdm.URIList;
import javax.microedition.ims.xdm.XCAPException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default implementation URIList interface.
 * {@link URIList}
 * 
 * @author Andrei Khomushko
 *
 */
public class URIListImpl implements URIList{
    private final String listName;
    private final List<ListEntry> listEntries = new ArrayList<ListEntry>(); 
    private String displayName;
    
    private final List<URIListModificationListener> modificationListeners = new ArrayList<URIListModificationListener>();
    
    public interface URIListModificationListener {
        void listEntryAdd(URIList uriList, ListEntry listEntry) 
            throws ServiceClosedException, XCAPException, IOException;
        void listEntryDelete(URIList uriList, ListEntry listEntry) 
            throws ServiceClosedException, XCAPException, IOException;;
        void displayNameChange(URIList uriList, String oldDisplayName, String newName) 
            throws ServiceClosedException, XCAPException, IOException;;
    }

    public URIListImpl(String listName) {
        this(listName, null);
    }
    
    public URIListImpl(String listName, String displayName) {
        this(listName, displayName, null);
    }
    
    public URIListImpl(String listName, String displayName, ListEntry[] listEntry) {
        this.listName = listName;
        this.displayName = displayName;
        
        if(listEntry != null) {
            listEntries.addAll(Arrays.asList(listEntry));    
        }
    }
    
    public void addURIListModificationListener(final URIListModificationListener listener) {
        if(listener != null && !modificationListeners.contains(listener)) {
            modificationListeners.add(listener);
        }
    }
    
    public void removeURIListModificationListener(URIListModificationListener listener) {
        if(listener != null) {
            modificationListeners.remove(listener);
        }
    }  
    
    public void addListEntry(final ListEntry listEntry)
            throws ServiceClosedException, XCAPException, IOException {
        if(listEntry == null) {
            throw new IllegalArgumentException("The listEntry argument is null");
        }
        
        //addListEntryInternally(listEntry);
        
        CollectionsUtils.replaceOrAdd(listEntry, listEntries, new CollectionsUtils.Predicate<ListEntry>() {
            public boolean evaluate(ListEntry entry) {
                return listEntry.getUri().equals(entry.getUri());
            }
        });
        
        notifyListEntryAdded(listEntry);
    }
    
    public void addListEntryInternally(ListEntry listEntry) {
        listEntries.add(listEntry);
    }
    
    
    public String getDisplayName() {
        return displayName;
    }

    
    public ListEntry[] getListEntries() {
        return listEntries.toArray(new ListEntry[0]);
    }

    
    public ListEntry getListEntry(final String uri) {
        return CollectionsUtils.find(listEntries, new CollectionsUtils.Predicate<ListEntry>() {
            
            public boolean evaluate(ListEntry listEntry) {
                return listEntry.getUri().equals(uri);
            }
        });
    }

    
    public String getListName() {
        return listName;
    }

    
    public void removeListEntry(final String uri) throws ServiceClosedException,
            XCAPException, IOException {
        
        final ListEntry listEntry = CollectionsUtils.find(listEntries, new CollectionsUtils.Predicate<ListEntry>() {
            
            public boolean evaluate(ListEntry listEntry) {
                return listEntry.getUri().equals(uri);
            }
        });
        
        if(listEntry == null) {
            throw new IllegalArgumentException("The ListEntry identified by the URI does not exist in this URIList");
        }
        
        removeListEntryInternally(listEntry);
        
        notifyListEntryDeleted(listEntry);
    }
    
    private void removeListEntryInternally(ListEntry listEntry) {
        listEntries.remove(listEntry);
    }

    public void setDisplayName(String newDispalyName) throws ServiceClosedException,
            XCAPException, IOException {
        String oldDisplayName = displayName;
        this.displayName = newDispalyName;
        notifyDisplayNameChanged(oldDisplayName, newDispalyName);
    }
    
    private void notifyListEntryAdded(ListEntry listEntry) throws ServiceClosedException, XCAPException, IOException {
        for(URIListModificationListener listener: modificationListeners) {
            listener.listEntryAdd(this, listEntry);
        }
    }
    
    private void notifyListEntryDeleted(ListEntry listEntry) throws ServiceClosedException, XCAPException, IOException {
        for(URIListModificationListener listener: modificationListeners) {
            listener.listEntryDelete(this, listEntry);
        }
    }

    private void notifyDisplayNameChanged(String oldDisplayName, String newName) throws ServiceClosedException, XCAPException, IOException {
        for(URIListModificationListener listener: modificationListeners) {
            listener.displayNameChange(this, oldDisplayName, newName);
        }
    }
    
    
    public String toString() {
        return "URIListImpl [displayName=" + displayName + ", listEntries="
                + listEntries + ", listName=" + listName + "]";
    }
}
