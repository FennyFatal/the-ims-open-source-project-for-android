package com.tmobile.test.ortptest;

import android.app.ListActivity;
import android.content.Context;

import java.io.*;
import java.lang.Process;
import java.lang.Runtime;
import java.net.InetAddress;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class OrtpTestActivity extends ListActivity {

    private static final String TAG = "OrtpTestActivity";

    private static final String LOCAL_HOST = "localhost";
//    private static final String LOCAL_HOST = "127.0.0.1";
//    private static final String LOCAL_HOST_EMU = "10.0.2.2";
    private static final String PORT = "8000";
    private static final String STREAMS_NUM = "5";
    private static final String FILE_NAME = "ring_digital_01.ogg";

    private static final String [][] LAUCNH_PROCESSES = {
        new String [] {"/system/bin/mrtpsend", FILE_NAME, "", PORT, STREAMS_NUM
                /*, "[--packet-size size]", "[--ts-inc value]"*/},
        new String [] {"/system/bin/mrtprecv", FILE_NAME, PORT, STREAMS_NUM},

        new String [] {"/system/bin/mrtpsend",  FILE_NAME, "", PORT, STREAMS_NUM
                /*, "[--packet-size size]", "[--ts-inc value]"*/},
        new String [] {"/system/bin/rtpmemtest", FILE_NAME, PORT, STREAMS_NUM},

        new String [] {"/system/bin/rtpsend", FILE_NAME, "", PORT
                /*"[ --with-clockslide <value> ] [ --with-jitter <milliseconds>]"*/},
        new String [] {"/system/bin/rtprecv", FILE_NAME + ".data", PORT
                /* "[--format format] [--soundcard] [--noadapt] [--with-jitter <milliseconds>]"*/},

        new String [] {"/system/bin/rtpsend_stupid", FILE_NAME, "", PORT
                /* "filename dest_ip4addr dest_port [ --with-clockslide <value> ] [ --with-ptime <milliseconds>]"*/},
        new String [] {"/system/bin/rtprecv",  FILE_NAME + ".dbb", PORT
                /* "[--format format] [--soundcard] [--noadapt] [--with-jitter <milliseconds>]"*/},

        new String [] {"/system/bin/test_timer"},
        null,

        new String [] {"/system/bin/tevrtpsend", FILE_NAME, "", PORT},
        new String [] {"/system/bin/tevrtprecv", FILE_NAME + ".tev", PORT},
    };

    private String[] mTestNames;
    private Process[] mTestProcesses;
    private File mFileDirectory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTestNames = getResources().getStringArray(R.array.items);
        mTestProcesses = new Process[LAUCNH_PROCESSES.length];
        for (int i = 0; i < LAUCNH_PROCESSES.length; i++) {
            mTestProcesses[i] = null;
        }
        BaseAdapter adapter = new TestAdapter(this);
        getListView().setAdapter(adapter);

        mFileDirectory = getFilesDir();

        try {
            copyAssetFile(FILE_NAME, FILE_NAME);
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy file " + FILE_NAME, e);
        }
    }

    private void copyAssetFile(String inFile, String outFile) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(getAssets().open(inFile));
        BufferedOutputStream bos = new BufferedOutputStream(openFileOutput(outFile, MODE_WORLD_WRITEABLE));

        byte buf[] = new byte[8192];
        int len;
        while ((len = bis.read(buf)) > -1) {
            bos.write(buf, 0, len);
        }
        bos.close();
        bis.close();
    }

    private final class TestAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public TestAdapter(Context context) {
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return mTestNames.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.test_row, null);
            } else {
                view = convertView;
            }
            TextView text = (TextView)view.findViewById(R.id.testName);
            text.setText(mTestNames[position]);
            Button start = (Button)view.findViewById(R.id.startTest);
            start.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        if (LAUCNH_PROCESSES[2* position + 1] != null) {
                            mTestProcesses[2 * position + 1] =
                                    Runtime.getRuntime().exec(LAUCNH_PROCESSES[2* position + 1], null, mFileDirectory);
                        }

                        String address = InetAddress.getByName(LOCAL_HOST).getHostAddress();
                        if (LAUCNH_PROCESSES[2* position].length > 2) {
                            LAUCNH_PROCESSES[2* position][2] = address;
                        }
                        mTestProcesses[2 * position] =
                                Runtime.getRuntime().exec(LAUCNH_PROCESSES[2* position], null, mFileDirectory);
                    } catch (IOException e) {
                        Log.e("OrtpTestActivity", "Failed to launch process", e);
                    }
                }
            });
            Button stop = (Button)view.findViewById(R.id.stopTest);
            stop.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (mTestProcesses[2 * position] != null) {
                        mTestProcesses[2 * position].destroy();
                        mTestProcesses[2 * position] = null;
                    }
                    if (mTestProcesses[2 * position + 1] != null) {
                        mTestProcesses[2 * position + 1].destroy();
                        mTestProcesses[2 * position + 1] = null;
                    }
                }
            });

            return view;
        }

    }

}