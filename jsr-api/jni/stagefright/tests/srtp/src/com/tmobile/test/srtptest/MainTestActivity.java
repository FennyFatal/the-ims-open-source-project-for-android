package com.tmobile.test.srtptest;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainTestActivity extends ListActivity {

    static {
        System.loadLibrary("SrtpTest");
    }

    private String[] mTestNames;
    private List<String> mTestResults;

    private void updateUI(final boolean result, final BaseAdapter adapter) {
        MainTestActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (result) {
                    mTestResults.add("Test Passed");
                } else {
                    mTestResults.add("Test Failed");
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTestNames = getResources().getStringArray(R.array.items);
        mTestResults = new ArrayList<String>();
        final BaseAdapter adapter = new TestAdapter(this);
        getListView().setAdapter(adapter);
        findViewById(R.id.startTest).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new Thread(new Runnable() {
                    public void run() {
                        updateUI(runEnvTest(), adapter);
                        updateUI(runAesTest("000102030405060708090a0b0c0d0e0f",
                                "00112233445566778899aabbccddeeff", "69c4e0d86a7b0430d8cdb78070b4c55a"), adapter);
                        updateUI(runCipherTest(), adapter);
                        updateUI(runDatatypesTest(), adapter);
                        updateUI(runSha1Test(), adapter);
                        updateUI(runStatTest(), adapter);
                        updateUI(runKernelTest(), adapter);
                        updateUI(runRandTest(256), adapter);
                        updateUI(runRdbxTest(), adapter);
                        updateUI(runSrtpTest(), adapter);
                        updateUI(runRocTest(2000), adapter);
                        updateUI(runReplayTest(1000), adapter);
                        updateUI(runDtlsTest(), adapter);
                        updateUI(runRtpwTest(), adapter);
                    }
                }).start();
                mTestResults.clear();
                adapter.notifyDataSetChanged();

            }
        });
    }

    private final class TestAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public TestAdapter(Context context) {
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return mTestResults.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(android.R.layout.simple_list_item_2, null);
            } else {
                view = convertView;
            }
            TextView text1 = (TextView)view.findViewById(android.R.id.text1);
            text1.setText(mTestNames[position]);
            TextView text2 = (TextView)view.findViewById(android.R.id.text2);
            text2.setText(mTestResults.get(position));
            return view;
        }

    }

    private native boolean runEnvTest();
    private native boolean runAesTest(String key, String phrase, String cipher);
    private native boolean runCipherTest();
    private native boolean runDatatypesTest();
    private native boolean runSha1Test();
    private native boolean runStatTest();
    private native boolean runKernelTest();
    private native boolean runRandTest(int bytes);

    private native boolean runRdbxTest();
    private native boolean runSrtpTest();
    private native boolean runRocTest(int num_trials);
    private native boolean runReplayTest(int num_trials);
    private native boolean runDtlsTest();
    private native boolean runRtpwTest();

}