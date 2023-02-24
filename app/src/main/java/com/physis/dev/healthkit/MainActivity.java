package com.physis.dev.healthkit;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.physicomtech.kit.physislibrary.PHYSIsBLEActivity;
import com.physis.dev.healthkit.customize.OnSingleClickListener;
import com.physis.dev.healthkit.customize.SerialNumberView;
import com.physis.dev.healthkit.dialog.LoadingDialog;
import com.physis.dev.healthkit.helper.PHYSIsPreferences;

import java.util.Arrays;

public class MainActivity extends PHYSIsBLEActivity {

    private static final String TAG = "SetupActivity";

    SerialNumberView snvSetup;
    Button btnConnect;
    TextView tvTempValue, tvHeartbeatValue, tvWeightValue;

    // 측정 데이터 메시지 프로토콜 STX/ETX
    private static final String BLE_MSG_STX = "$";
    private static final String BLE_MSG_ETX = "#";

    private PHYSIsPreferences preferences;

    private String serialNumber = null;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    protected void onBLEConnectedStatus(int result) {
        super.onBLEConnectedStatus(result);
        LoadingDialog.dismiss();
        setConnectedResult(result);
    }


    @Override
    protected void onBLEReceiveMsg(String msg) {
//        super.onBLEReceiveMsg(msg);
        if(msg.startsWith(BLE_MSG_STX) && msg.endsWith(BLE_MSG_ETX)){
            showMeasureHealthData(msg);
        }
    }

    /*
            Event
     */
    final SerialNumberView.OnSetSerialNumberListener onSetSerialNumberListener = new SerialNumberView.OnSetSerialNumberListener() {
        @Override
        public void onSetSerialNumber(String serialNum) {
            preferences.setPhysisSerialNumber(serialNumber = serialNum);
            Log.e(TAG, "# Set Serial Number : " + serialNumber);
        }
    };


    final OnSingleClickListener onClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (v.getId() == R.id.btn_connect) {
                if (serialNumber == null) {
                    Toast.makeText(getApplicationContext(), "PHYSIs Kit의 시리얼 넘버를 설정하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isConnected) {
                    disconnectDevice();
                } else {
                    LoadingDialog.show(MainActivity.this, "Connecting..");
                    connectDevice(serialNumber);
                }
            }
        }
    };

    /*
            Helper Method
     */
    @SuppressLint("SetTextI18n")
    private void setConnectedResult(int state){
        // set button
        if(isConnected = state == CONNECTED){
            btnConnect.setText("Disconnect");
        }else{
            btnConnect.setText("Connect");
        }
        // show toast
        String toastMsg;
        if(state == CONNECTED){
            toastMsg = "Physi Kit와 연결되었습니다.";
        }else if(state == DISCONNECTED){
            toastMsg = "Physi Kit 연결이 실패/종료되었습니다.";
        }else{
            toastMsg = "연결할 Physi Kit가 존재하지 않습니다.";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
    }

    private void showMeasureHealthData(String data){
        // 메시지 프로토콜 파싱 / 측정 데이터(심박, 체온, 체중)값 추출
        data = data.substring(1, data.length() - 1);
        String[] datas = data.split(",");
        if(datas.length == 3){
            // 측정 데이터 출력
            tvHeartbeatValue.setText(datas[0]);
            tvTempValue.setText(datas[1]);
            tvWeightValue.setText(datas[2]);
        }
    }

    private void init() {
        preferences = new PHYSIsPreferences(getApplicationContext());
        serialNumber = preferences.getPhysisSerialNumber();

        snvSetup = findViewById(R.id.snv_setup);
        snvSetup.setSerialNumber(serialNumber);
        snvSetup.showEditView(serialNumber == null);
        snvSetup.setOnSetSerialNumberListener(onSetSerialNumberListener);

        btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(onClickListener);

        tvTempValue = findViewById(R.id.tv_temp_value);
        tvHeartbeatValue = findViewById(R.id.tv_heartbeat_value);
        tvWeightValue = findViewById(R.id.tv_weight_value);
    }
}