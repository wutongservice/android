LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := tests

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := ut_SyncMLClient

LOCAL_INSTRUMENTATION_FOR := SyncMLClient

LOCAL_JAVA_LIBRARIES := android.test.runner oms

include $(BUILD_PACKAGE)