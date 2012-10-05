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

package javax.microedition.ims.android.msrp;

import android.os.RemoteException;
import android.util.Log;

import javax.microedition.ims.android.IReasonInfo;
import javax.microedition.ims.android.util.RemoteListenerHolder;
import javax.microedition.ims.common.IMSMessage;
import javax.microedition.ims.common.Logger;
import javax.microedition.ims.common.util.SIPUtil;
import javax.microedition.ims.core.ClientIdentity;
import javax.microedition.ims.core.IMSStack;
import javax.microedition.ims.core.IMSStackException;
import javax.microedition.ims.core.dialog.Dialog;
import javax.microedition.ims.core.dialog.DialogCallIDImpl;
import javax.microedition.ims.core.dialog.DialogStateListenerAdapter;
import javax.microedition.ims.core.msrp.MSRPService;
import javax.microedition.ims.core.msrp.MSRPSession;
import javax.microedition.ims.core.msrp.MSRPSessionType;
import javax.microedition.ims.core.msrp.filetransfer.FileDescriptor;
import javax.microedition.ims.core.msrp.listener.*;
import javax.microedition.ims.core.sipservice.Acceptable;
import javax.microedition.ims.core.transaction.*;
import javax.microedition.ims.messages.utils.MsrpUtils;
import javax.microedition.ims.messages.wrappers.sip.BaseSipMessage;
import java.util.*;

public class FileTransferManagerImpl extends IFileTransferManager.Stub {
    private static final String TAG = "Service - FileTransferManagerImpl";

    private final RemoteListenerHolder<IFileTransferManagerListener> listenerHolder = new RemoteListenerHolder<IFileTransferManagerListener>(IFileTransferManagerListener.class);

    private final MSRPService msrpService;
    private final IMSStack<IMSMessage> imsStack;

    private final FilesInProgressRegistry filesInProgressRegistry = new FilesInProgressRegistry();


    /*
      * Class represents information about requests and files which transferring is in progress.
      */
    private class FilesInProgressRegistry {
        private Map<String, Set<String>> requestRepository = new HashMap<String, Set<String>>();
        private Map<String, FileTransferEnv> fileRepository = new HashMap<String, FileTransferEnv>();

        private final Object mutex = new Object();

        public void addRecords(String requestId, FileDescriptor fileDescr, final MSRPSession msrpSession) {
            synchronized (mutex) {
                String fileId = fileDescr.getFileId();

                //add record to file repository
                fileRepository.put(fileId, new FileTransferEnv(msrpSession, fileDescr));

                //add record to file transfer request repository
                Set<String> fileIds;
                if (requestRepository.containsKey(requestId)) {
                    fileIds = requestRepository.get(requestId);
                } else {
                    fileIds = new HashSet<String>();
                    requestRepository.put(requestId, fileIds);
                }
                fileIds.add(fileId);
            }
        }

        public boolean isRequestDefined(String requestId) {
            synchronized (mutex) {
                return requestRepository.containsKey(requestId);
            }
        }

        public boolean isFileDefined(String fileId) {
            synchronized (mutex) {
                return fileRepository.containsKey(fileId);
            }
        }

        public Set<String> getRequestInfo(String requestId) {
            synchronized (mutex) {
                return requestRepository.get(requestId);
            }
        }

        public FileTransferEnv getFileInfo(String fileId) {
            synchronized (mutex) {
                return fileRepository.get(fileId);
            }
        }
    }


    /*
    * Listener for sending of a file
    */
    private class FileSendingListenersImpl implements MSRPSessionStartListener,
            MSRPSessionStopListener,
            MSRPFileSendingListener, MSRPFileSendingProgressListener {
        private MSRPSession msrpSession;
        private FileDescriptor fileDescriptor;
        private String requestId;

        public FileSendingListenersImpl(MSRPSession msrpSession, FileDescriptor fileDescriptor, String requestId) {
            this.msrpSession = msrpSession;
            this.fileDescriptor = fileDescriptor;
            this.requestId = requestId;
        }


        public void onMSRPSessionStarted() {
            Logger.log(TAG, "FileSendingListenersImpl.onMSRPSessionStarted#started");
            try {
                msrpSession.sendFile(fileDescriptor);
            } catch (IMSStackException e) {
                Logger.log(TAG, "FileSendingListenersImpl.onMSRPSessionStarted#failed to send " + fileDescriptor);
            }
            Logger.log(TAG, "FileSendingListenersImpl.onMSRPSessionStarted#finished");
        }


        public void onMSRPSessionStartFailed(int reasonType, String reasonPhrase, int statusCode) {
            Logger.log(TAG, "FileSendingListenersImpl.onMSRPSessionStartFailed#started");

            IReasonInfo reasonInfoImpl = new IReasonInfo(reasonPhrase, reasonType, statusCode);
            notifyFileSendFailed(requestId, fileDescriptor.getFileId(), reasonInfoImpl);

            unSubscribe();
            Logger.log(TAG, "FileSendingListenersImpl.onMSRPSessionStartFailed#finished");
        }

        public void onMSRPSessionFinished() {
            Logger.log(TAG, "FileSendingListenersImpl.onMSRPSessionFinished#started");
            IReasonInfo reasonInfoImpl = new IReasonInfo("777", IReasonInfo.REASONTYPE_RESPONSE, 777);
            notifyFileSendFailed(requestId, fileDescriptor.getFileId(), reasonInfoImpl);
            unSubscribe();
            Logger.log(TAG, "FileSendingListenersImpl.onMSRPSessionFinished#finished");
        }

        public void onFileSent(final FileDescriptor fileDescriptor) {
            Logger.log(TAG, "FileSendingListenersImpl.onFileSent#started");
            notifyFileSent(requestId, fileDescriptor.getFileId());
            unSubscribe();

            msrpSession.close();
            Logger.log(TAG, "FileSendingListenersImpl.onFileSent#finished");
        }

        public void onFileSendFailed(FileDescriptor fileDescriptor, int failCode, String failReasonPhrase) {
            Logger.log(TAG, "FileSendingListenersImpl.onFileSendFailed#started");
            IReasonInfo reasonInfoImpl = new IReasonInfo(failReasonPhrase, 0, failCode);
            notifyFileSendFailed(requestId, fileDescriptor.getFileId(), reasonInfoImpl);
            unSubscribe();

            msrpSession.close();
            Logger.log(TAG, "FileSendingListenersImpl.onFileSendFailed#finished");
        }

        public void onBytesTransfered(FileDescriptor fileDescriptor, long bytesTransferred, long bytesTotal) {
            Logger.log(TAG, "FileSendingListenersImpl.onBytesTransfered#started");
            notifyTransferProgress(requestId, fileDescriptor.getFileId(), bytesTransferred, bytesTotal);
            Logger.log(TAG, "FileSendingListenersImpl.onBytesTransfered#finished");
        }


        private void unSubscribe() {
            msrpSession.removeMSRPSessionStartListener(this);
            msrpSession.removeMSRPSessionStopListener(this);

            msrpSession.removeMSRPFileSendingListener(this);
            msrpSession.removeMSRPFileSendingProgressListener(this);
        }

        public void subscribe() {
            msrpSession.addMSRPSessionStartListener(this);
            msrpSession.addMSRPSessionStopListener(this);

            msrpSession.addMSRPFileSendingListener(this);
            msrpSession.addMSRPFileSendingProgressListener(this);
        }
    }

    /*
      * Listener for incoming file push request
      */
    private class MSRPFilePushRequestListener implements IncomingMSRPFilePushInviteListener {
        public void onIncomingInvite(IncomingMSRPFilePushInviteEvent event) {
            Logger.log(TAG, "MSRPFilePushRequestListener.onIncomingInvite#started");

            MSRPSession msrpSession = event.getMsrpSession();
            List<FileDescriptor> fileDescriptors = event.getFileDescriptors();
            String requestId = MsrpUtils.generateFileRequestId();

            //subscribe to file receiving events and progress
            new MSRPFileReceivingListeners(msrpSession, fileDescriptors, requestId).subscribe();

            //add records to file repository and to file transfer request repository
            for (FileDescriptor fileDescr : fileDescriptors) {
                filesInProgressRegistry.addRecords(requestId, fileDescr, msrpSession);
            }


            notifyIncomingFilePushRequest(event.getAcceptable(), msrpSession.getMsrpDialog(), requestId, fileDescriptors);
            Logger.log(TAG, "MSRPFilePushRequestListener.onIncomingInvite#finished");
        }
    }


    /*
      * Listeners for file receiving events and progress
      */
    private class MSRPFileReceivingListeners implements MSRPFileReceivingListener, MSRPFileReceivingProgressListener {
        private MSRPSession msrpSession;
        private List<FileDescriptor> fileDescriptors;
        private String requestId;

        public MSRPFileReceivingListeners(MSRPSession msrpSession, List<FileDescriptor> fileDescriptors, String requestId) {
            this.msrpSession = msrpSession;
            this.fileDescriptors = fileDescriptors;
            this.requestId = requestId;
        }

        public void onFileReceived(
                final FileDescriptor fileDescriptor,
                final String filePath) {

            Logger.log(TAG, "MSRPFileReceivingListeners.onFileReceived#started");

            notifyFileReceived(requestId, fileDescriptor.getFileId(), filePath);

            fileDescriptors.remove(fileDescriptor);
            if (fileDescriptors.isEmpty()) {
                unSubscribe();
            }
            Logger.log(TAG, "MSRPFileReceivingListeners.onFileReceived#finished");
        }

        public void onFileReceiveFailed(
                final FileDescriptor fileDescriptor,
                final int failCode,
                final String failReasonPhrase,
                final boolean localy) {

            Logger.log(TAG, "MSRPFileReceivingListeners.onFileReceiveFailed#started");

            IReasonInfo reasonInfoImpl = new IReasonInfo(failReasonPhrase, 0, failCode);
            notifyFileReceiveFailed(requestId, fileDescriptor.getFileId(), reasonInfoImpl);

            fileDescriptors.remove(fileDescriptor);
            if (fileDescriptors.isEmpty()) {
                unSubscribe();
            }

            if (localy) {
                msrpSession.close();
            }
            Logger.log(TAG, "MSRPFileReceivingListeners.onFileReceiveFailed#finished");
        }

        public void onBytesReceived(
                final FileDescriptor fileDescriptor,
                final long bytesTransferred,
                final long bytesTotal,
                final byte[] data) {

            Logger.log(TAG, "MSRPFileReceivingListeners.onBytesReceived#started");

            notifyTransferProgress(requestId, fileDescriptor.getFileId(), bytesTransferred, bytesTotal);
            Logger.log(TAG, "MSRPFileReceivingListeners.onBytesReceived#finished");
        }

        private void unSubscribe() {
            msrpSession.removeMSRPFileReceivingListener(this);
            msrpSession.removeMSRPFileReceivingProgressListener(this);
        }

        public void subscribe() {
            msrpSession.addMSRPFileReceivingListener(this);
            msrpSession.addMSRPFileReceivingProgressListener(this);
        }
    }


    public FileTransferManagerImpl(final IMSStack<IMSMessage> imsStack) {
        this.imsStack = imsStack;
        this.msrpService = imsStack.getMSRPService();

        //subscribe to incoming file push requests
        msrpService.addIncomingMSRPFilePushInviteListener(new MSRPFilePushRequestListener());
    }

    public String sendFiles(String sender, String[] recipients, String subject,
                            IFileInfo[] files, boolean deliveryReport) throws RemoteException {
        Logger.log(TAG, "sendFiles#started");

        String requestId = MsrpUtils.generateFileRequestId();

        FileDescriptor[] fileDescrs = new FileDescriptor[files.length];
        for (int i = 0; i < files.length; i++) {
            fileDescrs[i] = IFileInfoConverter.convert(files[i]);
        }
        Logger.log(TAG, "sendFiles#converted");

        for (String recipient : recipients) {
            for (int i = 0; i < fileDescrs.length; i++) {
                //recipient
                //fileDescrs[i]

                try {
                    final ClientIdentity callingParty = imsStack.getContext().getRegistrationIdentity();

                    Dialog msrpDialog_A_B = imsStack.getContext().getDialogStorage().getDialog(
                            callingParty,
                            recipient,
                            new DialogCallIDImpl(SIPUtil.newCallId())
                    );

                    final MSRPSession msrpSession = msrpService.obtainMSRPSession(msrpDialog_A_B, MSRPSessionType.FILE_OUT);

                    //subscribe to SESSION start and stop events, file sending events and progress
                    new FileSendingListenersImpl(msrpSession, fileDescrs[i], requestId).subscribe();

                    //try to start SESSION
                    msrpSession.openSendFileSession(fileDescrs[i]);

                    //add records to file repository and to file transfer request repository
                    filesInProgressRegistry.addRecords(requestId, fileDescrs[i], msrpSession);

                    Logger.log(TAG, "sendFiles#sent file " + (i + 1) + "/" + fileDescrs.length + " to " + recipient);
                } catch (Throwable e) {
                    Logger.log(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        Logger.log(TAG, "sendFiles#finished");
        return requestId;
    }

    public String requestFiles(String requestSender, String requestRecipient,
                               String subject, IFileSelector[] files) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public void cancel(String identifier) throws RemoteException {
        Logger.log(TAG, "cancel#started");
        if (filesInProgressRegistry.isRequestDefined(identifier)) {

            Set<String> fileIds = filesInProgressRegistry.getRequestInfo(identifier);
            for (String id : fileIds) {
                FileTransferEnv fileTransferEnv = filesInProgressRegistry.getFileInfo(id);
                if (fileTransferEnv.getMsrpSession().getSessionType() == MSRPSessionType.FILE_OUT) {
                    fileTransferEnv.getMsrpSession().cancelFileSending(fileTransferEnv.getFileDescriptor());
                } else if (fileTransferEnv.getMsrpSession().getSessionType() == MSRPSessionType.FILE_IN) {
                    fileTransferEnv.getMsrpSession().cancelFileReceiving(fileTransferEnv.getFileDescriptor());
                }
            }

        } else if (filesInProgressRegistry.isFileDefined(identifier)) {
            FileTransferEnv fileTransferEnv = filesInProgressRegistry.getFileInfo(identifier);
            if (fileTransferEnv.getMsrpSession().getSessionType() == MSRPSessionType.FILE_OUT) {
                fileTransferEnv.getMsrpSession().cancelFileSending(fileTransferEnv.getFileDescriptor());
            } else if (fileTransferEnv.getMsrpSession().getSessionType() == MSRPSessionType.FILE_IN) {
                fileTransferEnv.getMsrpSession().cancelFileReceiving(fileTransferEnv.getFileDescriptor());
            }
        }
        Logger.log(TAG, "cancel#finished");
    }

    public void addListener(IFileTransferManagerListener listener)
            throws RemoteException {
        if (listener != null) {
            listenerHolder.addListener(listener);
        }
    }

    public void removeListener(IFileTransferManagerListener listener)
            throws RemoteException {
        if (listener != null) {
            listenerHolder.removeListener(listener);
        }
    }

    public boolean isAvailable4Cancel(String identifier) throws RemoteException {
        boolean res = filesInProgressRegistry.isRequestDefined(identifier);
        if (res) {
            return res;
        }
        res = filesInProgressRegistry.isFileDefined(identifier);
        return res;
    }

    private void notifyFileSent(String requestId, String fileId) {
        Logger.log(TAG, "notifyFileSent#started");
        try {
            listenerHolder.getNotifier().fileSent(requestId, fileId);
        } catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyFileSent#finished");
    }

    private void notifyFileSendFailed(String requestId, String fileId, IReasonInfo reason) {
        Logger.log(TAG, "notifyFileSendFailed#started");
        try {
            listenerHolder.getNotifier().fileSendFailed(requestId, fileId, reason);
        } catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyFileSendFailed#finished");
    }

    private void notifyFileReceived(String requestId, String fileId, String filePath) {
        Logger.log(TAG, "notifyFileReceived#started");
        try {
            listenerHolder.getNotifier().fileReceived(requestId, fileId, filePath);
        } catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyFileReceived#finished");
    }

    private void notifyFileReceiveFailed(String requestId, String fileId, IReasonInfo reason) {
        Logger.log(TAG, "notifyFileReceiveFailed#started");
        try {
            listenerHolder.getNotifier().fileReceiveFailed(requestId, fileId, reason);
        } catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyFileReceiveFailed#finished");
    }

    private void notifyIncomingFilePushRequest(final Acceptable acceptable, final Dialog dialog,
                                               final String requestId, final List<FileDescriptor> fileDescrList) {

        IFileInfo[] fileInfos = null;
        if (fileDescrList != null && !fileDescrList.isEmpty()) {
            fileInfos = new IFileInfo[fileDescrList.size()];

            int i = 0;
            for (FileDescriptor fileDescriptor : fileDescrList) {
                fileInfos[i] = IFileInfoConverter.convert(fileDescriptor);
                i++;
            }
        }

        //TODO replace null values by real values
        FilePushRequestImpl filePushRequestImpl = new FilePushRequestImpl(
                dialog, acceptable,
                requestId, null, null, null, fileInfos);

        Logger.log(TAG, "notifyIncomingFilePushRequest#started");
        try {
            listenerHolder.getNotifier().incomingFilePushRequest(filePushRequestImpl);
        } catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyIncomingFilePushRequest#finished");
    }

    /*
      * TODO call this method
      */
    private void notifyIncomingFilePullRequest(IFilePullRequest filePullRequest) {
        Logger.log(TAG, "notifyIncomingFilePullRequest#started");
        try {
            listenerHolder.getNotifier().incomingFilePullRequest(filePullRequest);
        } catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyIncomingFilePullRequest#finished");
    }

    private void notifyTransferProgress(String requestId, String fileId, long bytesTransferred, long bytesTotal) {
        Logger.log(TAG, "notifyTransferProgress#started");
        Date timeStart = new Date();
        try {
            listenerHolder.getNotifier().transferProgress(requestId, fileId, bytesTransferred, bytesTotal);
        } catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Date timeStop = new Date();
        long timeDelta = timeStop.getTime() - timeStart.getTime();
        Logger.log(TAG, "notifyTransferProgress#finished   MILLISECONDS=" + timeDelta);
    }

    /*
      * TODO call this method
      */
    private void notifyFileTransferFailed(String requestId, IReasonInfo reason) {
        Logger.log(TAG, "notifyFileTransferFailed#started");
        try {
            listenerHolder.getNotifier().fileTransferFailed(requestId, reason);
        } catch (RemoteException e) {
            Logger.log(TAG, e.getMessage());
            e.printStackTrace();
        }
        Logger.log(TAG, "notifyFileTransferFailed#finished");
    }
}
