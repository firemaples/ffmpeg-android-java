package com.github.hiteshsondhi88.libffmpeg;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.File;

class FFmpegLoadLibraryAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private final String cpuArchNameFromAssets;
    private final FFmpegLoadBinaryResponseHandler ffmpegLoadBinaryResponseHandler;
    private final Context context;

    FFmpegLoadLibraryAsyncTask(Context context, String cpuArchNameFromAssets, FFmpegLoadBinaryResponseHandler ffmpegLoadBinaryResponseHandler) {
        this.context = context;
        this.cpuArchNameFromAssets = cpuArchNameFromAssets;
        this.ffmpegLoadBinaryResponseHandler = ffmpegLoadBinaryResponseHandler;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        File ffmpegFile = new File(FileUtils.getFFmpeg(context));
        String assetsFFmpeg = cpuArchNameFromAssets + File.separator + FileUtils.ffmpegFileName;
        if (ffmpegFile.exists()) {
            String installedSHA1 = FileUtils.SHA1(FileUtils.getFFmpeg(context));
            Log.i("Installed FFmpeg file SHA1: " + installedSHA1);
            if ((isDeviceFFmpegVersionOld(installedSHA1) || isFFmpegSHA1Changed(installedSHA1, assetsFFmpeg)) && !ffmpegFile.delete()) {
                Log.w("Delete the old FFmpeg file failed");
                return false;
            }
        }
        if (!ffmpegFile.exists()) {
            Log.d("Start to copy FFmpeg");
            boolean isFileCopied = FileUtils.copyBinaryFromAssetsToData(context,
                    assetsFFmpeg,
                    FileUtils.ffmpegFileName);
            Log.d("FFmpeg copied: " + isFileCopied);
            // make file executable
            if (isFileCopied) {
                if (!ffmpegFile.canExecute()) {
                    Log.d("FFmpeg is not executable, trying to make it executable ...");
                    if (ffmpegFile.setExecutable(true)) {
                        return true;
                    }
                } else {
                    Log.d("FFmpeg is executable");
                    return true;
                }
            }
        }
        return ffmpegFile.exists() && ffmpegFile.canExecute();
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (ffmpegLoadBinaryResponseHandler != null) {
            if (isSuccess) {
                ffmpegLoadBinaryResponseHandler.onSuccess();
            } else {
                ffmpegLoadBinaryResponseHandler.onFailure();
            }
            ffmpegLoadBinaryResponseHandler.onFinish();
        }
    }

    private boolean isDeviceFFmpegVersionOld(String installedSHA1) {
        return CpuArch.fromString(installedSHA1).equals(CpuArch.NONE);
    }

    private boolean isFFmpegSHA1Changed(String installedSHA1, String assetsFFmpegPath) {
        return !TextUtils.equals(installedSHA1, FileUtils.assetsFileSHA1(context, assetsFFmpegPath));
    }
}
