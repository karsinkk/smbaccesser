LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq (,$(filter user,$(TARGET_BUILD_VARIANT)))

LOCAL_CERTIFICATE := platform

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE_TAGS := eng


LOCAL_PACKAGE_NAME := MRVLInstaller

LOCAL_STATIC_JAVA_LIBRARIES:= \
    libarity android-support-v4 guava \
    libarity libjcifs-1.3.17 guava

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    libandroid-support-v4:libs/android-support-v4.jar \
    libjcifs-1.3.17:libs/jcifs-1.3.17.jar

include $(BUILD_MULTI_PREBUILT)
endif
