package com.example.liuyang.restartapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class MainActivity extends Activity {

    public static final String ACTION_REBOOT =  "android.intent.action.REBOOT";
    public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    private SeekBar seekBar;
    private TextView textView;

    private static AudioManager am;
    private static float audioMaxVolumn;
    private float volumnRatio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textView = (TextView) findViewById(R.id.result);
        //seekbar设置监听
        setSeekBarListener();
    }

    private void setSeekBarListener() {
        // 获取音频服务然后强转成一个音频管理器,后面方便用来控制音量大小用
        am = (AudioManager)this.getSystemService(this.AUDIO_SERVICE);
        // 获取最大音量值
        audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekBar.setMax((int)audioMaxVolumn);
        seekBar.setProgress((int)audioCurrentVolumn);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("当前进度："+progress+"%");
                volumnRatio = progress/audioMaxVolumn;
                Log.d("debug",String.valueOf(seekBar.getId()));
            }
            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this,"按住seekbar",Toast.LENGTH_SHORT).show();
            }
            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar ) {
                Toast.makeText(MainActivity.this,"放开seekbar",Toast.LENGTH_SHORT).show();
                myToast("设置音量" + volumnRatio);
            }
        });
    }

    public void restart(View view){

//        try {
//            //获得ServiceManager类
//            Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
//
//            //获得ServiceManager的getService方法
//            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
//
//            //调用getService获取RemoteService
//            Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);
//
//            //获得IPowerManager.Stub类
//            Class<?> cStub = Class .forName("android.os.IPowerManager$Stub");
//            //获得asInterface方法
//            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
//            //调用asInterface方法获取IPowerManager对象
//            Object oIPowerManager = asInterface.invoke(null, oRemoteService);
//            //获得shutdown()方法
//            Method shutdown = oIPowerManager.getClass().getMethod("shutdown",boolean.class,boolean.class);
//            //调用shutdown()方法
//            shutdown.invoke(oIPowerManager,false,true);
//
//        } catch (Exception e) {
//            Log.e("",  e.toString());
//        }


//        Intent intent2 = new Intent(Intent.ACTION_REBOOT);
//        intent2.putExtra("nowait", 1);
//        intent2.putExtra("interval", 1);
//        intent2.putExtra("window", 0);
//        sendBroadcast(intent2);

        try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(
                        process.getOutputStream());
                out.writeBytes("reboot \n");
                out.writeBytes("exit\n");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        myToast("开始重启");
    }


    public void distanceINIT(View view){
        gpioset(149 ,"out");
        myToast("初始化成功");
    }

    public void distanceStart(View view){
        gpioread(149);
        myToast("结果= "  +  gpioread(149));
    }

    public void gpioset(int gpio_num ,String gpio_mode) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("echo gpio_mode >/sys/class/gpio/gpio" + gpio_num + "/direction\n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            myToast("结果= "  +  e.toString());
        }
    }


    public void gpiowrite(int gpio_num ,int gpio_val) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("echo gpio_val >/sys/class/gpio/gpio" + gpio_num + "/value\n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String gpioread(int gpio_num) {
        String value = retRootCommand("cat /sys/class/gpio/gpio" + gpio_num + "/value");
        return value;
    }

    private  String retRootCommand(final String strCmd) {
        try {
            final Process localProcess = Runtime.getRuntime().exec("su");
            final OutputStream localOutputStream = localProcess.getOutputStream();
            final DataOutputStream localDataOutputStream = new DataOutputStream(localOutputStream);

            localDataOutputStream.writeBytes(strCmd + "\n");
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();

            final BufferedReader bufferReader = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));

            StringBuffer strResult = new StringBuffer();

            String line = null;
            while ((line = bufferReader.readLine()) != null) {
                strResult.append(line);
            }

            localProcess.waitFor();
            localOutputStream.close();
            return strResult.toString();
        } catch (final Exception e) {
            e.printStackTrace();
            myToast("结果= "  +  e.toString());
            return "";
        }
    }


    private void myToast(String content) {
        Toast.makeText(getApplicationContext(),content ,Toast.LENGTH_SHORT).show();
    }
}
