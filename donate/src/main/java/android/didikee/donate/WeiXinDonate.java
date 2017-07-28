package android.didikee.donate;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by didikee on 2017/7/28.
 * 使用微信捐赠
 */

public class WeiXinDonate {
    private static final String TAG = WeiXinDonate.class.getSimpleName();
    // 微信包名
    private static final String TENCENT_PACKAGE_NAME = "com.tencent.mm";
    // 微信二维码扫描页面地址
    private static final String TENCENT_ACTIVITY_BIZSHORTCUT = "com.tencent.mm.action.BIZSHORTCUT";
    // Extra data
    private static final String TENCENT_EXTRA_ACTIVITY_BIZSHORTCUT = "LauncherUI.From.Scaner.Shortcut";

    /**
     * 启动微信二维码扫描页
     * ps： 需要你引导用户从文件中扫描二维码
     * @param activity activity
     */
    private static void gotoWeChatQrScan(@NonNull Activity activity) {
        Intent intent = new Intent(TENCENT_ACTIVITY_BIZSHORTCUT);
        intent.setPackage(TENCENT_PACKAGE_NAME);
        intent.putExtra(TENCENT_EXTRA_ACTIVITY_BIZSHORTCUT, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "你好像没有安装微信", Toast.LENGTH_SHORT).show();
        }
    }

    private static void sendPictureStoredBroadcast(Context context, String qrSavePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(qrSavePath));
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /**
     * 保存图片到本地，需要权限
     * @param qrBitmap 二维码图片
     */
    public static void saveDonateQrImage2SDCard(@NonNull String qrSavePath, @NonNull Bitmap qrBitmap) {
        File qrFile = new File(qrSavePath);
        File parentFile = qrFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            FileOutputStream fos = new FileOutputStream(qrFile);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 微信捐赠
     * @param activity activity
     * @param qrSavePath 个人收款二维码，可以通过微信生成
     */
    public static void donateViaWeiXin(Activity activity, String qrSavePath) {
        if (activity == null || TextUtils.isEmpty(qrSavePath)) {
            //参数错误
            Log.d(TAG, "参数为null");
            return;
        }
        sendPictureStoredBroadcast(activity, qrSavePath);
        gotoWeChatQrScan(activity);
    }

    /**
     * 判断支付宝客户端是否已安装，建议调用转账前检查
     * @param context Context
     * @return 支付宝客户端是否已安装
     */
    public static boolean hasInstalledWeiXinClient(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(TENCENT_PACKAGE_NAME, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取支付宝客户端版本名称，作用不大
     * @param context Context
     * @return 版本名称
     */
    public static String getWeiXinClientVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(TENCENT_PACKAGE_NAME, 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
