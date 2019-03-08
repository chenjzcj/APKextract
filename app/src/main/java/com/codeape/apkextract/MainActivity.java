package com.codeape.apkextract;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codeape.apkextract.entity.AppBean;
import com.codeape.apkextract.utils.ImmersionStyles;
import com.codeape.apkextract.view.CustomToolbar;
import com.codeape.apkextract.view.LoadDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView, listMenu;
    List<AppBean> mAppAllList = new ArrayList<>();
    List<AppBean> mAppPersonalList = new ArrayList<>();
    List<AppBean> mAppSystemList = new ArrayList<>();
    LoadDialog loadDialog;
    int position = 0;
    int state = 0;
    int screen = 0;
    String savePath = Environment.getExternalStorageDirectory() + File.separator + "/Android/data/com.codeape.apkextract/file/";
    String[] menuItem = {"全部", "个人", "系统"};
    CustomToolbar toolbar;
    RxPermissions permissions;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionStyles.setImmersionStyle(this, ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.white), true);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {

            //检查当前权限（若没有该权限，值为-1；若有该权限，值为0）
            int hasReadExternalStoragePermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasReadExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
            } else {
                //若没有授权，会弹出一个对话框（这个对话框是系统的，开发者不能自己定制），用户选择是否授权应用使用系统权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setRightOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listMenu.getVisibility() == View.VISIBLE) {
                    listMenu.setVisibility(View.GONE);
                } else {
                    listMenu.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, menuItem));
                    listMenu.setVisibility(View.VISIBLE);
                }
            }
        });

        listView = findViewById(R.id.list);
        loadDialog = new LoadDialog(this);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                position = i;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("请选择操作");
//                final String[] sex = {"提取APK", "发送APK", "APK信息"};
                final String[] sex = {"提取APK", "发送APK"};
                builder.setItems(sex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                state = 1;
                                new operationTask().execute();
                                break;
                            case 1:
                                state = 2;
                                new operationTask().execute();
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
                return false;
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                listMenu.setVisibility(View.GONE);
                return false;
            }
        });

        listMenu = findViewById(R.id.list_menu);
        listMenu.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, menuItem));
        listMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        screen = 0;
                        listView.setAdapter(new AppsAdapter(MainActivity.this, mAppAllList));
                        break;
                    case 1:
                        screen = 1;
                        listView.setAdapter(new AppsAdapter(MainActivity.this, mAppPersonalList));
                        break;
                    case 2:
                        screen = 2;
                        listView.setAdapter(new AppsAdapter(MainActivity.this, mAppSystemList));
                        break;
                }
                listMenu.setVisibility(View.GONE);
            }
        });
        state = 0;
        new operationTask().execute();

    }

    //用户选择是否同意授权后，会回调这个方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                //若用户不同意授权，直接暴力退出应用。
                // 当然，这里也可以有比较温柔的操作。
                finish();
            }
        }
    }

    public void getAllApk() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> list = packageManager.getInstalledPackages(0);
        for (PackageInfo p : list) {
            AppBean bean = new AppBean();
            bean.setAppIcon(p.applicationInfo.loadIcon(packageManager));
            bean.setAppName(packageManager.getApplicationLabel(p.applicationInfo).toString());
            bean.setAppPackageName(p.applicationInfo.packageName);
            bean.setApkPath(p.applicationInfo.sourceDir);
            File file = new File(p.applicationInfo.sourceDir);
            bean.setAppSize((double) file.length());
            bean.sourceDir = p.applicationInfo.sourceDir;
            int flags = p.applicationInfo.flags;
            //判断是否是属于系统的apk
            if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                bean.isSystem = true;
                mAppSystemList.add(bean);
            } else {
                bean.setSd(true);
                mAppPersonalList.add(bean);
            }
            mAppAllList.add(bean);

        }
        loadDialog.dismiss();
    }

    public void sendFile(File apkFile) {
        Uri data;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("*/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // "com.codeape.apkextract"即是在清单文件中配置的authorities
            data = FileProvider.getUriForFile(MainActivity.this, "com.codeape.apkextract", apkFile);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            data = Uri.fromFile(apkFile);
        }
        intent.putExtra(Intent.EXTRA_STREAM, data);
        startActivity(intent);
    }

    /**
     * 获得指定文件的byte数组
     */
    private byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    //参数一、文件的byte流
    //参数二、文件要保存的路径
    //参数三、文件保存的名字
    public static void saveFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;

        File file = null;
        try {
            //通过创建对应路径的下是否有相应的文件夹。
            File dir = new File(filePath);
            if (!dir.exists()) {// 判断文件目录是否存在
                dir.mkdirs();//如果文件存在则删除已存在的文件夹。
            }

            //如果文件存在则删除文件
            file = new File(filePath, fileName);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);//把需要保存的文件保存到SD卡中
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    class operationTask extends AsyncTask<String, Object, Long> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadDialog.show();
        }

        @Override
        protected Long doInBackground(String... strings) {
            switch (state) {
                case 0:
                    getAllApk();
                    break;
                default:
//                    saveFile(getBytes(mAppAllList.get(position).getApkPath()), savePath + mAppAllList.get(position).getAppPackageName() + ".apk", "");
                    switch (screen) {
                        case 0:
                            saveFile(getBytes(mAppAllList.get(position).getApkPath()), savePath + mAppAllList.get(position).getAppPackageName() + ".apk", "");
                            break;
                        case 1:
                            saveFile(getBytes(mAppPersonalList.get(position).getApkPath()), savePath + mAppPersonalList.get(position).getAppPackageName() + ".apk", "");
                            break;
                        case 2:
                            saveFile(getBytes(mAppSystemList.get(position).getApkPath()), savePath + mAppSystemList.get(position).getAppPackageName() + ".apk", "");
                            break;
                    }
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            loadDialog.dismiss();
            switch (state) {
                case 0:
                    listView.setAdapter(new AppsAdapter(MainActivity.this, mAppAllList));
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, "已提取到本地", Toast.LENGTH_SHORT).show();
                    state = 0;
                    break;
                case 2:
                    sendFile(new File(savePath + mAppAllList.get(position).getAppPackageName() + ".apk"));
                    state = 0;
                    break;
            }
        }
    }

    class AppsAdapter extends BaseAdapter {

        private Context mContext;
        List<AppBean> appBeans;

        public AppsAdapter(Context context, List<AppBean> appBeans) {
            mContext = context;
            this.appBeans = appBeans;
        }

        public AppsAdapter() {
        }

        @Override
        public int getCount() {
            if (null != appBeans) {
                return appBeans.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.item_apps, null);
                holder.app_image = convertView.findViewById(R.id.item_app_iv_bg);
                holder.app_title = convertView.findViewById(R.id.item_app_tv_title);
                holder.app_package = convertView.findViewById(R.id.item_app_tv_package);
                holder.app_size = convertView.findViewById(R.id.item_app_tv_size);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.app_image.setImageDrawable(appBeans.get(position).getAppIcon());
            holder.app_title.setText(appBeans.get(position).getAppName());
            holder.app_package.setText(appBeans.get(position).getAppPackageName());

            DecimalFormat df = new DecimalFormat("0.00");
            double size = appBeans.get(position).getAppSize() / 1024 / 1024;
            holder.app_size.setText(size > 0 ? df.format(size) + "MB" : df.format(size * 1024) + "KB");

            return convertView;
        }

        class ViewHolder {

            ImageView app_image;
            TextView app_title;
            TextView app_package;
            TextView app_size;

        }

    }
}
