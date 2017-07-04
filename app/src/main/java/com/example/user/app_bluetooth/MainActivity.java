package com.example.user.app_bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;

public class MainActivity extends Activity
{
    private boolean Check=false;
    private Button ConnectButton=null;
    private TextView ForView=null;
    private CheckBox ClientCheck=null;
    private CheckBox ServerCheck=null;
    class ConnectButtonListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            if(!Check)
            {
                Toast.makeText(getApplicationContext(),"请先选择客户端或服务端",Toast.LENGTH_SHORT).show();
                ForView.setText("请先选择本机是客户端还是服务端，注意和通信对方的机子不同即可");
            }
            else
            {
                BluetoothAdapter LocalApdapter=BluetoothAdapter.getDefaultAdapter();
                if(null!=LocalApdapter)
                {
                    System.out.println("本机有蓝牙设备");
                    if(!LocalApdapter.isEnabled())
                    {
                        Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(intent);
                    }
                    Set<BluetoothDevice> RomoteDevies=LocalApdapter.getBondedDevices();
                    if(RomoteDevies.size()>0)
                    {
                        for(Iterator iterator=RomoteDevies.iterator();iterator.hasNext();)
                        {
                            BluetoothDevice RemoteBluetooth=(BluetoothDevice)iterator.next();
                            System.out.println(RemoteBluetooth.getAddress());
                            BluetoothMsg.BlueToothAddress=RemoteBluetooth.getAddress();
                            BluetoothMsg.BluetoothRemoteDevice=RemoteBluetooth;
                            Toast.makeText(getApplicationContext(),RemoteBluetooth.getAddress(),Toast.LENGTH_SHORT).show();
                            Intent in=new Intent(MainActivity.this,BluetoothActivity.class);
                            startActivity(in);
                        }

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"不存在已配对的蓝牙，请配对",Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    Toast.makeText(getApplicationContext(),"没有蓝牙设备",Toast.LENGTH_SHORT).show();
                }
            }

        }

    }
    public void onCheckboxClicked(View view)
    {
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getId())
        {
            case R.id.checkBoxClient:
                if (checked)
                {
                   // Toast.makeText(getApplicationContext(),"已选择客户端",Toast.LENGTH_SHORT).show();
                    BluetoothMsg.serviceOrCilent=BluetoothMsg.ServerOrCilent.CILENT;
                    Check=true;
                    ForView.setText("已选择客户端，如果都勾选以后面勾选的为准");
                }
                else
                {
                    BluetoothMsg.serviceOrCilent=BluetoothMsg.ServerOrCilent.CILENT;
                    ForView.setText("取消选择客户端");
                    Check=false;
                }
                break;
            case R.id.checkBoxServer:
                if (checked)
                {
                    BluetoothMsg.serviceOrCilent=BluetoothMsg.ServerOrCilent.SERVICE;
                    Check=true;
                    ForView.setText("已选择服务端，如果都勾选以后面勾选的为准");
                }
                else
                {
                    BluetoothMsg.serviceOrCilent=BluetoothMsg.ServerOrCilent.CILENT;
                    ForView.setText("取消选择服务端");
                    Check=false;
                }
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectButton=(Button)findViewById(R.id.CONNECTBUTTON);
        ConnectButton.setOnClickListener(new ConnectButtonListener());
        ClientCheck=(CheckBox)findViewById(R.id.checkBoxClient);
        ServerCheck=(CheckBox)findViewById(R.id.checkBoxServer);
        ForView=(TextView)findViewById(R.id.CONNECTVIEW);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
