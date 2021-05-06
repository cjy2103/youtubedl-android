package com.yausername.atomicparsley;

import android.content.Context;

import androidx.annotation.NonNull;

import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_common.SharedPrefsHelper;
import com.yausername.youtubedl_common.utils.ZipUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class AtomicParsley {

    private static final AtomicParsley INSTANCE = new AtomicParsley();
    protected static final String baseName = "youtubedl-android";
    private static final String packagesRoot = "packages";
    private static final String atomicparsleyDirName = "atomicparsley";
    private static final String atomicparsleyLibName = "libatomicparsley.zip.so";
    private static final String atomicparsleyLibVersion = "atomicparsleyLibVersion";

    private boolean initialized = false;
    private File binDir;

    private AtomicParsley() {
    }

    public static AtomicParsley getInstance() {
        return INSTANCE;
    }

    synchronized public void init(Context appContext) throws YoutubeDLException {
        if (initialized) return;

        File baseDir = new File(appContext.getNoBackupFilesDir(), baseName);
        if (!baseDir.exists()) baseDir.mkdir();

        binDir = new File(appContext.getApplicationInfo().nativeLibraryDir);

        File packagesDir = new File(baseDir, packagesRoot);
        File atomicparsleyDir = new File(packagesDir, atomicparsleyDirName);
        initAtomicParsley(appContext, atomicparsleyDir);

        initialized = true;
    }

    private void initAtomicParsley(Context appContext, File atomicparsleyDir) throws YoutubeDLException {
        File atomicparsleyLib = new File(binDir, atomicparsleyLibName);
        // using size of lib as version
        String atomicparsleySize = String.valueOf(atomicparsleyLib.length());
        if (!atomicparsleyDir.exists() || shouldUpdateAtomicParsley(appContext, atomicparsleySize)) {
            FileUtils.deleteQuietly(atomicparsleyDir);
            atomicparsleyDir.mkdirs();
            try {
                ZipUtils.unzip(atomicparsleyLib, atomicparsleyDir);
            } catch (Exception e) {
                FileUtils.deleteQuietly(atomicparsleyDir);
                throw new YoutubeDLException("failed to initialize", e);
            }
            updateAtomicParsley(appContext, atomicparsleySize);
        }
    }

    private boolean shouldUpdateAtomicParsley(@NonNull Context appContext, @NonNull String version) {
        return !version.equals(SharedPrefsHelper.get(appContext, atomicparsleyLibVersion));
    }

    private void updateAtomicParsley(@NonNull Context appContext, @NonNull String version) {
        SharedPrefsHelper.update(appContext, atomicparsleyLibVersion, version);
    }
}
