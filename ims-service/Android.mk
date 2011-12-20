LOCAL_PATH:= $(call my-dir)

ifeq ($(IMS_SETTINGS), 1)
    $(shell ln -f -s AndroidManifest-wLaunch.xml $(LOCAL_PATH)/AndroidManifest.xml)
else
    $(shell ln -f -s AndroidManifest-woLaunch.xml $(LOCAL_PATH)/AndroidManifest.xml)
endif

$(shell touch $(LOCAL_PATH)/AndroidManifest.xml)

include $(CLEAR_VARS)

# build static library ims-service, to be used by jsr-api as well
LOCAL_MODULE_TAGS := optional debug eng
LOCAL_SRC_FILES := src/javax/microedition/ims/android/msrp/IDeliveryReport.aidl \
	src/javax/microedition/ims/android/msrp/IMessageManagerListener.aidl \
	src/javax/microedition/ims/android/msrp/IFilePushRequest.aidl \
	src/javax/microedition/ims/android/msrp/IIMSessionListener.aidl \
	src/javax/microedition/ims/android/msrp/IHistoryManager.aidl \
	src/javax/microedition/ims/android/msrp/IIMServiceListener.aidl \
	src/javax/microedition/ims/android/msrp/IIMSession.aidl \
	src/javax/microedition/ims/android/msrp/IConferenceManager.aidl \
	src/javax/microedition/ims/android/msrp/IFilePullRequest.aidl \
	src/javax/microedition/ims/android/msrp/IConferenceManagerListener.aidl \
	src/javax/microedition/ims/android/msrp/IFileTransferManagerListener.aidl \
	src/javax/microedition/ims/android/msrp/IChat.aidl \
	src/javax/microedition/ims/android/msrp/IConferenceInvitation.aidl \
	src/javax/microedition/ims/android/msrp/IIMService.aidl \
	src/javax/microedition/ims/android/msrp/IFileTransferManager.aidl \
	src/javax/microedition/ims/android/msrp/IChatInvitation.aidl \
	src/javax/microedition/ims/android/msrp/IMessageManager.aidl \
	src/javax/microedition/ims/android/msrp/IDeferredMessageManager.aidl \
	src/javax/microedition/ims/android/msrp/ILargeMessageRequest.aidl \
	src/javax/microedition/ims/android/msrp/IConference.aidl \
	src/javax/microedition/ims/android/msrp/IChatListener.aidl \
	src/javax/microedition/ims/android/IConfiguration.aidl \
	src/javax/microedition/ims/android/core/IServiceMethod.aidl \
	src/javax/microedition/ims/android/core/IReference.aidl \
	src/javax/microedition/ims/android/core/ISessionDescriptor.aidl \
	src/javax/microedition/ims/android/core/IPublication.aidl \
	src/javax/microedition/ims/android/core/IPageMessage.aidl \
	src/javax/microedition/ims/android/core/ICapabilities.aidl \
	src/javax/microedition/ims/android/core/IReferenceListener.aidl \
	src/javax/microedition/ims/android/core/IPublicationListener.aidl \
	src/javax/microedition/ims/android/core/ICapabilitiesListener.aidl \
	src/javax/microedition/ims/android/core/ICoreServiceListener.aidl \
	src/javax/microedition/ims/android/core/ICoreService.aidl \
	src/javax/microedition/ims/android/core/IPageMessageListener.aidl \
	src/javax/microedition/ims/android/core/ISubscriptionListener.aidl \
	src/javax/microedition/ims/android/core/IMessageBodyPart.aidl \
	src/javax/microedition/ims/android/core/ISession.aidl \
	src/javax/microedition/ims/android/core/IMessage.aidl \
	src/javax/microedition/ims/android/core/ISubscription.aidl \
	src/javax/microedition/ims/android/core/ISessionListener.aidl \
	src/javax/microedition/ims/android/xdm/IDocumentSubscriber.aidl \
	src/javax/microedition/ims/android/xdm/IPresenceListDocument.aidl \
	src/javax/microedition/ims/android/xdm/IPresenceAuthorizationDocument.aidl \
	src/javax/microedition/ims/android/xdm/IXDMServiceListener.aidl \
	src/javax/microedition/ims/android/xdm/IXDMService.aidl \
	src/javax/microedition/ims/android/xdm/IDocumentSubscriberListener.aidl \
	src/javax/microedition/ims/android/xdm/IURIListDocument.aidl \
	src/javax/microedition/ims/android/presence/IPresenceSourceListener.aidl \
	src/javax/microedition/ims/android/presence/IPresenceServiceListener.aidl \
	src/javax/microedition/ims/android/presence/IWatcherListener.aidl \
	src/javax/microedition/ims/android/presence/IPresenceSource.aidl \
	src/javax/microedition/ims/android/presence/IPresenceService.aidl \
	src/javax/microedition/ims/android/presence/IWatcher.aidl \
	src/javax/microedition/ims/android/presence/IWatcherInfoSubscriber.aidl \
	src/javax/microedition/ims/android/presence/IWatcherInfoSubscriberListener.aidl \
	src/javax/microedition/ims/android/IConnectionStateListener.aidl \
	src/javax/microedition/ims/android/IConnectionState.aidl \
	src/javax/microedition/ims/android/IConnector.aidl \
	src/javax/microedition/ims/android/IReasonInfo.java \
	src/javax/microedition/ims/android/presence/IPresentity.java \
	src/javax/microedition/ims/android/presence/IEvent.java \
	src/javax/microedition/ims/android/xdm/IDocumentEntry.java \
	src/javax/microedition/ims/android/xdm/IXCAPResponse.java \
	src/javax/microedition/ims/android/xdm/IListEntry.java \
	src/javax/microedition/ims/android/xdm/IPresenceList.java \
	src/javax/microedition/ims/android/xdm/IPresenceAuthorizationRulesHolder.java \
	src/javax/microedition/ims/android/xdm/IURIList.java \
	src/javax/microedition/ims/android/xdm/IUniquenessError.java \
	src/javax/microedition/ims/android/xdm/IXCAPException.java \
	src/javax/microedition/ims/android/xdm/IURIListsHolder.java \
	src/javax/microedition/ims/android/xdm/IXCAPRequest.java \
	src/javax/microedition/ims/android/xdm/IIdentity.java \
	src/javax/microedition/ims/android/xdm/IXCAPError.java \
	src/javax/microedition/ims/android/xdm/IPresenceAuthorizationRule.java \
	src/javax/microedition/ims/android/xdm/IPresenceListsHolder.java \
	src/javax/microedition/ims/android/IGsmLocationInfo.java \
	src/javax/microedition/ims/android/IRegistry.java \
	src/javax/microedition/ims/android/IExceptionHolder.java \
	src/javax/microedition/ims/android/msrp/IMessage.java \
	src/javax/microedition/ims/android/msrp/IContentPart.java \
	src/javax/microedition/ims/android/msrp/IFileInfo.java \
	src/javax/microedition/ims/android/msrp/IFileSelector.java \
	src/javax/microedition/ims/android/IError.java \
	src/javax/microedition/ims/android/IStackError.java \
	src/javax/microedition/ims/android/core/media/IMedia.java \
	src/javax/microedition/ims/android/core/media/ICryptoParam.java

# LOCAL_STATIC_JAVA_LIBRARIES := ims-common ims-parser
LOCAL_MODULE := ims-service

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_STATIC_JAVA_LIBRARY)

# build ims-service.apk module
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional debug eng
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_SRC_FILES := $(filter-out %/IReasonInfo.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IPresentity.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IEvent.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IDocumentEntry.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IXCAPResponse.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IListEntry.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IPresenceList.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IPresenceAuthorizationRulesHolder.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IURIList.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IUniquenessError.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IXCapException.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IURIListHolder.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IXCAPRequest.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IIdentity.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IXCAPError.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IPresenceAuthorizationRule.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IPresenceListsHolder.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IGsmLocationInfo.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IRegistry.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IExceptionHolder.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IMessage.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IContentPart.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IFileInfo.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IFileSelector.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IError.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/IMedia.java,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out %/ICryptoParam.java,$(LOCAL_SRC_FILES))

LOCAL_STATIC_JAVA_LIBRARIES := ims-common ims-parser ims-service xbilldns
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libs/org.xbill.dns_2.1.1.jar
LOCAL_PACKAGE_NAME := ims-service
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional debug eng
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := xbilldns:libs/org.xbill.dns_2.1.1.jar
include $(BUILD_MULTI_PREBUILT)
