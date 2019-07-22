package com.github.hiteshsondhi88.sampleffmpeg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.util.Arrays;

public class Home extends Activity implements View.OnClickListener {

    private static final String TAG = Home.class.getSimpleName();
    private static final int REQUEST_MEDIA_FILE = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    @Inject
    FFmpeg ffmpeg;

    @InjectView(R.id.select_file)
    Button selectFile;

    @InjectView(R.id.command)
    EditText commandEditText;

    @InjectView(R.id.command_output)
    LinearLayout outputLayout;

    @InjectView(R.id.run_command)
    Button runButton;

    private ProgressDialog progressDialog;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);
        ObjectGraph.create(new DaggerDependencyModule(this)).inject(this);

        com.github.hiteshsondhi88.libffmpeg.Log.setLogInterceptor(new com.github.hiteshsondhi88.libffmpeg.Log.LogInterceptor() {
            @Override
            public void log(final String level, final String tag, final String log, final Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String logString = level.toUpperCase() + "/" + tag + "/" + log;
                        if (throwable != null) {
                            logString += "\n" + Log.getStackTraceString(throwable);
                        }
                        addTextViewToLayout(logString);
                    }
                });
            }

            @Override
            public void log(final String level, final String tag, final String log) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String logString = level.toUpperCase() + "/" + tag + "/" + log;
                        addTextViewToLayout(logString);
                    }
                });
            }
        });

        loadFFMpegBinary();
        initUI();
    }

    private void initUI() {
        runButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);

        selectFile.setOnClickListener(this);
    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    addTextViewToLayout("FAILED with output : " + s);
                    Log.e(TAG, s);
                }

                @Override
                public void onSuccess(String s) {
                    addTextViewToLayout("SUCCESS with output : " + s);
                    Log.d(TAG, s);
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + Arrays.toString(command));
                    addTextViewToLayout("progress : " + s);
                    progressDialog.setMessage("Processing\n" + s);
                    Log.d(TAG, s);
                }

                @Override
                public void onStart() {
                    startTime = System.currentTimeMillis();
                    outputLayout.removeAllViews();

                    Log.d(TAG, "Started command : ffmpeg " + Arrays.toString(command));
                    addTextViewToLayout("Started command : ffmpeg " + Arrays.toString(command));
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    long totalTime = System.currentTimeMillis() - startTime;
                    Log.d(TAG, "Finished command(" + totalTime + "ms) : ffmpeg " + Arrays.toString(command));
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void addTextViewToLayout(String text) {
        TextView textView = new TextView(Home.this);
        textView.setText(text);
        outputLayout.addView(textView);
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(Home.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.device_not_supported))
                .setMessage(getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Home.this.finish();
                    }
                })
                .create()
                .show();

    }

    @Override
    public void onClick(View v) {
        if (!checkPermission()) {
            return;
        }

        switch (v.getId()) {
            case R.id.run_command:
                String cmd = commandEditText.getText().toString();
                String[] command = cmd.split(" ");
                if (command.length != 0) {
                    execFFmpegBinary(command);
                } else {
                    Toast.makeText(Home.this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.select_file:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, REQUEST_MEDIA_FILE);
                break;
        }
    }

    private boolean checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setMessage("Need R/W storage permission to work")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", getPackageName(), null)));
                            }
                        })
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_MEDIA_FILE:
                    Uri uri = data.getData();
                    if (uri != null) {
                        compressVideo(uri);
                    }
                    break;
            }
        }
    }

    private void compressVideo(Uri uri) {
        Cursor c = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        c.moveToFirst();
        String uriString = c.getString(0);
        c.close();

        String[] command = {
                "-i", uriString,
                "-y",
                "-c:v", "libx264",
//        "-r", "15",
                "-preset", "ultrafast",
                "-movflags", "+faststart", //move meta to the first block    https://stackoverflow.com/questions/21686191/can-ffmpeg-place-mp4-metainfo-at-the-beginning-of-the-file
//                "-vf", "scale=w=min(iw\\,200):h=-2", // https://stackoverflow.com/questions/8218363/maintaining-ffmpeg-aspect-ratio   https://lists.ffmpeg.org/pipermail/ffmpeg-user/2015-July/027742.html
//        "-vf", "scale=360:360:force_original_aspect_ratio=decrease",  //https://stackoverflow.com/questions/8133242/ffmpeg-resize-down-larger-video-to-fit-desired-size-and-add-padding
                "-vf", "scale='if(lte(min(iw\\,ih\\),360),-2,if(lt(iw\\,ih\\), 360, -2))':'if(lte(min(iw\\,ih\\),360),-2,if(lt(ih\\,iw\\), 360, -2))'",
//        "-x264opts", "crf=18:ref=4:bframes=5",
                "/storage/emulated/0/test.mp4"
        };

        execFFmpegBinary(command);
    }
}
