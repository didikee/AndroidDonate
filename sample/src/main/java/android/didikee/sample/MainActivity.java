package android.didikee.sample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    public static final int REQUEST_CODE = 2233;

    public static final String Donate_USER_INPUT = "FKX06601UW0I4UBLQA8Q56";
    public static final String Donate_1 = "FKX01162H9X5Q0WTZZDVD0";
    public static final String Donate_5 = "FKX02513XN2AOJEYBO2NC9";
    public static final String Donate_10 = "FKX08442ZKTRLVH22COB54";
    public static final String Donate_18 = "FKX03752CDIPICFNASSL46";
    public static final String Donate_Merchant = "stx00187oxldjvyo3ofaw60";//商家收款
    private RadioGroup radioGroup;
    private Button btAlipayCustom;
    private Button btAlipayFree;
    private Button btAlipayMerchant;

    private int currentMoney = 0;
    private boolean isGooglePlay = true;
    private TextView noticeTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.bt_weixin).setOnClickListener(this);
        noticeTextView = ((TextView) findViewById(R.id.notice));
        btAlipayCustom = ((Button) findViewById(R.id.bt_alipay));
        btAlipayFree = ((Button) findViewById(R.id.bt_alipay_free));
        btAlipayMerchant = ((Button) findViewById(R.id.bt_alipay_merchant));
        radioGroup = ((RadioGroup) findViewById(R.id.radio_group));
        radioGroup.setOnCheckedChangeListener(this);
        radioGroup.getChildAt(0).performClick();

        btAlipayCustom.setOnClickListener(this);
        btAlipayFree.setOnClickListener(this);
        btAlipayMerchant.setOnClickListener(this);

        checkPlatfrom();
    }

    private void checkPlatfrom() {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (applicationInfo == null) return;
        String value = applicationInfo.metaData.getString("PAY_TYPE");
        isGooglePlay = "Google".equalsIgnoreCase(value);

        //layout notice
        noticeTextView.setText(getResources().getString(isGooglePlay ? R.string.msg_google_pay_limit : R.string.msg_pay_notice));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (isGooglePlay) {
            // 支付限制，改为图片展示
            doGoogle(id);
        } else {
            // 正常情况下
            doNormal(id);
        }

    }

    private String endString = "图片只做演示参考,以实际场景为准。";

    private void doGoogle(int id) {
        switch (id) {
            case R.id.bt_weixin:
                //微信捐赠
                showDonateTipDialog();
                break;
            case R.id.bt_alipay:
                // 自定义价格
                gotoTipShowActivity("支付宝捐赠--自定义价格", "在实际操作中用户选中相应的价格即可，无需手动输入，用户体验最好。" + endString, R.drawable.p1);
                break;
            case R.id.bt_alipay_free:
                // 用户手动输入金额
                gotoTipShowActivity("支付宝捐赠--用户填写", "在实际操作中用户必须手动输入金额。灵活度高，用户可以捐赠随意金额。" + endString, R.drawable.p1);
                break;
            case R.id.bt_alipay_merchant:
                // 商户收款
                gotoTipShowActivity("支付宝捐赠--商户收款", "在实际操作中用户必须手动输入金额。灵活度高，用户可以捐赠随意金额。(能使用其他支付方式)" + endString, R.drawable
                        .p2);
                break;
        }
    }

    private void gotoTipShowActivity(String title, String tip, int bgRes) {
        Intent tipIntent = new Intent(this, TipActivity.class);
        tipIntent.putExtra("title", title);
        tipIntent.putExtra("tip", tip);
        tipIntent.putExtra("res", bgRes);
        startActivity(tipIntent);
    }

    private void doNormal(int id) {
        switch (id) {
            case R.id.bt_weixin:
                //微信捐赠
                checkPermissionAndDonateWeixin();
                break;
            case R.id.bt_alipay:
                // 自定义价格
                if (currentMoney == 1) {
                    donateAlipay(Donate_1);
                } else if (currentMoney == 5) {
                    donateAlipay(Donate_5);
                } else if (currentMoney == 10) {
                    donateAlipay(Donate_10);
                } else if (currentMoney == 18) {
                    donateAlipay(Donate_18);
                }
                break;
            case R.id.bt_alipay_free:
                // 用户手动输入金额
                donateAlipay(Donate_USER_INPUT);
                break;
            case R.id.bt_alipay_merchant:
                // 商户收款
                donateAlipay(Donate_Merchant);
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
        String text = checkedRadioButton.getText().toString().trim();
        currentMoney = Integer.valueOf(text.replace("元", "").trim());
        btAlipayCustom.setText("支付宝捐赠(" + currentMoney + "元)");
    }

    /**
     * 支付宝支付
     * @param payCode 收款码后面的字符串；例如：收款二维码里面的字符串为 https://qr.alipay.com/stx00187oxldjvyo3ofaw60 ，则
     *                payCode = stx00187oxldjvyo3ofaw60
     *                注：不区分大小写
     */
    private void donateAlipay(String payCode) {
        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(this);
        if (hasInstalledAlipayClient) {
            AlipayDonate.startAlipayClient(this, payCode);
        }
    }

    private void checkPermissionAndDonateWeixin() {
        //检测微信是否安装
        if (!WeiXinDonate.hasInstalledWeiXinClient(this)) {
            Toast.makeText(this, "未安装微信客户端", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //已经有权限
            showDonateTipDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    private void showDonateTipDialog() {
        new AlertDialog.Builder(this)
                .setTitle("微信捐赠操作步骤")
                .setMessage("点击确定按钮后会跳转微信扫描二维码界面：\n\n" + "1. 点击右上角的菜单按钮\n\n" + "2. 点击'从相册选取二维码'\n\n" + "3. 选择第一张二维码图片即可\n\n")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isGooglePlay){
                            gotoTipShowActivity("微信捐赠", "在实际操作中用户必须手动输入金额。" + endString, R.drawable.p3);
                        }else {
                            donateWeixin();
                        }

                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 需要提前准备好 微信收款码 照片，可通过微信客户端生成
     */
    private void donateWeixin() {
        InputStream weixinQrIs = getResources().openRawResource(R.raw.didikee_weixin);
        String qrPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AndroidDonateSample" + File.separator +
                "didikee_weixin.png";
        WeiXinDonate.saveDonateQrImage2SDCard(qrPath, BitmapFactory.decodeStream(weixinQrIs));
        WeiXinDonate.donateViaWeiXin(this, qrPath);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            donateWeixin();
        } else {
            Toast.makeText(this, "权限被拒绝", Toast.LENGTH_LONG).show();
        }
    }

}
