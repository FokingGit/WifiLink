package rxjava.foking.wifilinkdomo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText mEtSsid;
    private EditText mEtPassword;
    private Button mBtLink;
    private TextView mIdLinkresult;
    private WifiManager mWifiManager;
    private boolean isAplinking;
    private int securityType;
    private String domesticWiFiSIID;
    private String domesticWiFiPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("onCreate");
        setContentView(R.layout.activity_main);
        Logger.i("onCreate");
        //注册监听wifi扫描
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiBroadCastReceiver, filter);
        isAplinking = false;
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        initView();
        requestPermiss();
    }


    private void requestPermiss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            new RxPermissions(this)
                    .request(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(@NonNull Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                Logger.i("有权限");
                            } else {
                                Logger.i("没有权限");
                            }
                        }
                    });
        } else {
            Logger.i("有权限");
        }

    }

    private int tryGetSSIDTypeCount; //重新扫描的次数
    private BroadcastReceiver mWifiBroadCastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isAplinking) {
                List<ScanResult> list = mWifiManager.getScanResults();
                for (ScanResult scResult : list) {
                    if (!TextUtils.isEmpty(scResult.SSID) && scResult.SSID.equals(domesticWiFiSIID)) {
                        String capabilities = scResult.capabilities;
                        Logger.i("capabilities=" + capabilities);
                        if (!TextUtils.isEmpty(capabilities)) {
                            if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                                Logger.i("wpa");
                                securityType = 4;
                            } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                                Logger.i("wep");
                                securityType = 1;
                            } else {
                                Logger.i("no");
                                securityType = 0;
                            }
                        }
                    }
                }
                //当密码不为空，但是获取到加密类型是NONE,那就肯定有问题
                if (!TextUtils.isEmpty(domesticWiFiPassword) && (securityType == 0)) {
                    if (tryGetSSIDTypeCount < 5) {
                        //继续扫描,扫描次数加一
                        tryGetSSIDTypeCount++;
                        mWifiManager.startScan();
                        Logger.i("扫描加次数：" + tryGetSSIDTypeCount);
                    }
                } else {
                    configWPAWifi(MainActivity.this, domesticWiFiSIID, domesticWiFiPassword);
                }
            }

        }
    };

    private void initView() {
        mEtSsid = (EditText) findViewById(R.id.et_ssid);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mBtLink = (Button) findViewById(R.id.bt_link);
        mIdLinkresult = (TextView) findViewById(R.id.linkresult);

        mBtLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_link:
                submit();
                mWifiManager.startScan();
                mIdLinkresult.setText(getString(R.string.link_result) + domesticWiFiSIID);
                break;
        }
    }

    private void submit() {
        isAplinking = false;
        // validate
        domesticWiFiSIID = mEtSsid.getText().toString().trim();
        if (TextUtils.isEmpty(domesticWiFiSIID)) {
            Toast.makeText(this, "SSID", Toast.LENGTH_SHORT).show();
            return;
        }
        domesticWiFiPassword = mEtPassword.getText().toString().trim();
        // TODO validate success, do something


    }

    private void configWPAWifi(Context context, String ssid, String password) {
        isAplinking = true;
        try {
            WifiConfiguration configuration = new WifiConfiguration();
            configuration.allowedAuthAlgorithms.clear();
            configuration.allowedGroupCiphers.clear();
            configuration.allowedKeyManagement.clear();
            configuration.allowedPairwiseCiphers.clear();
            configuration.allowedProtocols.clear();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                configuration.SSID = ssid;
            } else {
                configuration.SSID = String.format("\"%s\"", ssid);
            }
            if (securityType == 1) {
                configuration.hiddenSSID = true;
                configuration.wepKeys[0] = "\"" + password + "\"";
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);

                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                configuration.wepTxKeyIndex = 0;
            } else if (securityType == 4) {
                configuration.preSharedKey = String.format("\"%s\"", password);
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2

                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);

                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            } else if (securityType == 0) {
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }


            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int networkId = addNetwork(configuration, wifiManager);
            wifiManager.disconnect();
            wifiManager.enableNetwork(networkId, true);
            boolean reconnect = wifiManager.reconnect();
            ensureWifiConfig();
            Logger.e("是否连接：" + reconnect);
        } catch (Exception e) {
        }
    }

    private int addNetwork(WifiConfiguration configuration, WifiManager wifiManager) {
        List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configurations) {
            if (config.SSID != null && config.SSID.equals(configuration.SSID)) {
                return config.networkId;
            }
        }
        return wifiManager.addNetwork(configuration);
    }

    /**
     * 确定手机已经连接身上设备发出的wifi
     */
    private void ensureWifiConfig() {
        Observable
                .interval(3, 3, TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .skipWhile(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        boolean networkConnected = isNetworkOnline();
                        if (networkConnected) {
                            if (isDesWifi()) {
                                return false;
                            } else {
                                configWPAWifi(MainActivity.this, domesticWiFiSIID, domesticWiFiPassword);
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                })
                .firstOrError()
                .flatMapCompletable(Functions.justFunction(Completable.complete()))
                .timeout(60, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        Logger.i("切换wifi：" + domesticWiFiSIID + "成功");
                        mIdLinkresult.setText(R.string.switch_wifi_success);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.i("切换wifi：" + domesticWiFiSIID + "失败");
                        mIdLinkresult.setText(R.string.switch_wifi_fail);
                    }
                });
    }

    public boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 3 www.baidu.com");
            int exitValue = ipProcess.waitFor();
            Logger.i("Process:" + exitValue);
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isDesWifi() {
        if (mWifiManager != null) {
            WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                String ssid = connectionInfo.getSSID();
                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
                if (TextUtils.equals(ssid, domesticWiFiSIID)) {
                    return true;
                }
            }
        }

        return false;
    }
}
