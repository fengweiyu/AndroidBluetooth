package com.example.user.app_bluetooth;

/**
 * Created by user on 8/16/2016.
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
public class BluetoothActivity extends Activity{
    public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
    Context mContext;
    private Button ButtonSend=null;
    private EditText editText=null;
    private TextView ForView=null;
    private BluetoothServerSocket mserverSocket = null;
    private ServerThread startServerThread = null;
    private clientThread clientConnectThread = null;
    private BluetoothSocket socket = null;
    private BluetoothDevice device = null;
    private readThread mreadThread = null;;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    class ButtonSendListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            String inputtext=editText.getText().toString();
            if (inputtext.length()>0) {
                sendMessageHandle(inputtext);
                HandleMessageToDisplay(inputtext);
                editText.setText("");
                editText.clearFocus();
                //close InputMethodManager
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }else
            {
                Toast.makeText(getApplicationContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show();
            }


        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetootch_activity);
        mContext = this;
        ButtonSend = (Button) findViewById(R.id.BUTTONSEND);
        editText = (EditText) findViewById(R.id.text808);
        ButtonSend.setOnClickListener(new ButtonSendListener());
        ForView = (TextView) findViewById(R.id.textView);
    }
    @Override
    protected  void onResume()
    {
       // BluetoothMsg.serviceOrCilent=BluetoothMsg.ServerOrCilent.CILENT;
        if(BluetoothMsg.isOpen)
        {
            Toast.makeText(getApplicationContext(),"连接已经打开",Toast.LENGTH_SHORT).show();
            //ForView.setText("连接已经打开");
            return;
        }
        if(BluetoothMsg.serviceOrCilent==BluetoothMsg.ServerOrCilent.CILENT)
        {
            String address = BluetoothMsg.BlueToothAddress;
            if(!address.equals("null"))
            {
                //device = mBluetoothAdapter.getRemoteDevice(address);
                device = BluetoothMsg.BluetoothRemoteDevice;
                clientConnectThread = new clientThread();
                clientConnectThread.start();
                BluetoothMsg.isOpen = true;
            }
            else
            {
               Toast.makeText(getApplicationContext(),"蓝牙地址为空",Toast.LENGTH_SHORT).show();
                //ForView.setText("蓝牙地址为空");
            }
        }
        else if(BluetoothMsg.serviceOrCilent==BluetoothMsg.ServerOrCilent.SERVICE)
        {
            startServerThread = new ServerThread();
            startServerThread.start();
            BluetoothMsg.isOpen = true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),"没有选择通信模式",Toast.LENGTH_SHORT).show();
            //ForView.setText("没有选择通信模式");
        }
        super.onResume();
    }
    private Handler LinkDetectedHandler = new Handler() {
        public void handleMessage(Message msg) {
            //Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
            if(msg.what==1)
            {
                //ForView.setText((String)msg.obj);
                HandleMessageToDisplay((String)msg.obj);
            }
            else
            {
                ForView.setText((String) msg.obj);
            }
        }
    };
    //开启客户端
    private class clientThread extends Thread {
        @Override
        public void run() {
            try {
                //创建一个Socket连接：只需要服务器在注册时的UUID号
                // socket = device.createRfcommSocketToServiceRecord(BluetoothProtocols.OBEX_OBJECT_PUSH_PROTOCOL_UUID);
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001000-0000-1000-8000-00805F9B34FB"));
                //连接
                String Linkaddress ="请稍候，正在连接服务器:"+BluetoothMsg.BlueToothAddress;
                Message msg1 = new Message();
                msg1.obj = Linkaddress;
                msg1.what = 1;
                LinkDetectedHandler.sendMessage(msg1);
                //ForView.setText(Linkaddress);线程中不能直接使用外部的界面显示看，主线程才可以
                //Toast.makeText(mContext,Linkaddress,Toast.LENGTH_SHORT).show();不能使用

                socket.connect();
                Message msg2 = new Message();
                msg2.obj = "已经连接上可发送信息";
                msg2.what = 1;
                LinkDetectedHandler.sendMessage(msg2);
                //启动接受数据
                mreadThread = new readThread();
                mreadThread.start();
            }
            catch (IOException e)
            {
                System.out.println("connect异常");
                Log.e("connect", "", e);
                //Toast.makeText(getApplicationContext(),"连接服务器异常请重试",Toast.LENGTH_SHORT).show();
               // ForView.setText("连接服务器异常请重试");
            }
        }
    };
    //开启服务器
    private class ServerThread extends Thread {
        @Override
        public void run() {
            try {
                    /* 创建一个蓝牙服务器
                     * 参数分别：服务器名称、UUID   */
                mserverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
                        UUID.fromString("00001000-0000-1000-8000-00805F9B34FB"));

                Log.d("server", "wait cilent connect...");
                Message msg1 = new Message();
                msg1.obj = "请稍候，正在等待客户端的连接...";
                msg1.what = 1;
                LinkDetectedHandler.sendMessage(msg1);

                    /* 接受客户端的连接请求 */
                socket = mserverSocket.accept();
                Log.d("server", "accept success !");
                Message msg2 = new Message();
                msg2.obj = "客户端已经连接上！可以发送信息";
                msg2.what = 1;
                LinkDetectedHandler.sendMessage(msg2);
                //ForView.setText("客户端已经连接上！可以发送信息");
                //启动接受数据
                mreadThread = new readThread();
                mreadThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    /* 停止服务器 */
    private void shutdownServer() {
        new Thread() {
            @Override
            public void run() {
                if(startServerThread != null)
                {
                    startServerThread.interrupt();
                    startServerThread = null;
                }
                if(mreadThread != null)
                {
                    mreadThread.interrupt();
                    mreadThread = null;
                }
                try {
                    if(socket != null)
                    {
                        socket.close();
                        socket = null;
                    }
                    if (mserverSocket != null)
                    {
                        mserverSocket.close();/* 关闭服务器 */
                        mserverSocket = null;
                    }
                } catch (IOException e) {
                    Log.e("server", "mserverSocket.close()", e);
                }
            };
        }.start();
    }
    /* 停止客户端连接 */
    private void shutdownClient() {
        new Thread() {
            @Override
            public void run() {
                if(clientConnectThread!=null)
                {
                    clientConnectThread.interrupt();
                    clientConnectThread= null;
                }
                if(mreadThread != null)
                {
                    mreadThread.interrupt();
                    mreadThread = null;
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    socket = null;
                }
            };
        }.start();
    }
    //发送数据
    private void sendMessageHandle(String msg)
    {
        if (socket == null)
        {
            Toast.makeText(getApplicationContext(), "没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            OutputStream os = socket.getOutputStream();
            os.write(msg.getBytes());
            //ForView.setText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //发送数据
    private void HandleMessageToDisplay(String msg)
    {
        char cTempChar;
        int ProtocolLen;
        byte TempCharBuf[]=new byte[4];
        cTempChar=msg.charAt(0);
        if('4'!=cTempChar)
        {
            ForView.setText(msg);
        }
        else
        {
           // msg.getChars(0, 2, TempCharBuf, 0);
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append("协议头:");
            strBuffer.append(msg.substring(0,4));
            strBuffer.append("\r\n");
            strBuffer.append("长度:");
            strBuffer.append(msg.substring(4,8));
            strBuffer.append("\r\n");
            ProtocolLen=Integer.parseInt(msg.substring(4,8),16);
            strBuffer.append("协议版本号:");
            strBuffer.append(msg.substring(8, 10));
            strBuffer.append("\r\n");
            strBuffer.append("设备编号:");
            strBuffer.append(msg.substring(10,50));
            strBuffer.append("\r\n");
            strBuffer.append("信息类型:");
            strBuffer.append(msg.substring(50,54));
            strBuffer.append("\r\n");
            strBuffer.append("协议数据:");
            ProtocolLen=LowToInt(ProtocolLen);
            ProtocolLen=((ProtocolLen & 0xFFFF) << 16) | ((ProtocolLen >>16) & 0xFFFF);
            ProtocolLen=ProtocolLen*2-62;
            ///strBuffer.append(new String(Integer.toHexString(ProtocolLen)));
            strBuffer.append(msg.substring(54,54+ProtocolLen));
            strBuffer.append("\r\n");
            strBuffer.append("校验和:");
            strBuffer.append(msg.substring(54+ProtocolLen,58+ProtocolLen));
            strBuffer.append("\r\n");
            strBuffer.append("协议尾:");
            strBuffer.append(msg.substring(58+ProtocolLen,62+ProtocolLen));
            strBuffer.append("\r\n");
            ForView.setText(strBuffer);
        }
    }
    public static int LowToInt(int a) {
        return (((a & 0xFF) << 24) | (((a >>8) & 0xFF) << 16) | (((a >> 16) & 0xFF) << 8) | ((a >> 24) & 0xFF));
    }
    //读取数据
    private class readThread extends Thread {
        @Override
        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;
            InputStream mmInStream = null;

            try {
                mmInStream = socket.getInputStream();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (true) {
                try {
                    // Read from the InputStream
                    if( (bytes = mmInStream.read(buffer)) > 0 )
                    {
                        byte[] buf_data = new byte[bytes];
                        for(int i=0; i<bytes; i++)
                        {
                            buf_data[i] = buffer[i];
                        }
                        String s = new String(buf_data);
                        Message msg = new Message();
                        msg.obj = s;
                        msg.what = 1;
                        LinkDetectedHandler.sendMessage(msg);
                        //ForView.setText(s);
                    }
                } catch (IOException e) {
                    try {
                        mmInStream.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
    @Override
    protected void onDestroy()
    {
        if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.CILENT)
        {
            shutdownClient();
        }
        else if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.SERVICE)
        {
            shutdownServer();
        }
        BluetoothMsg.isOpen = false;
        BluetoothMsg.serviceOrCilent = BluetoothMsg.ServerOrCilent.NONE;
        super.onDestroy();
    }//发送数据


}
