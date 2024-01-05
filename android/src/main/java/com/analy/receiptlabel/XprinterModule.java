package com.analy.receiptlabel;

import static android.content.Context.BIND_AUTO_CREATE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.analy.receiptlabel.utils.PrinterCommands;
import com.analy.receiptlabel.utils.StringUtils;
import com.facebook.react.module.annotations.ReactModule;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.BitmapCallback;

import net.posprinter.IDeviceConnection;
import net.posprinter.IPOSListener;
import net.posprinter.POSConnect;
import net.posprinter.POSPrinter;
import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos80;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ReactModule(name = XprinterModule.NAME)
public class XprinterModule extends ReactContextBaseJavaModule {
  public static String DISCONNECT = "com.posconsend.net.disconnetct";
  public static final String NAME = "RNXprinter";
  private ReactApplicationContext context;

  private byte[] mBuffer = new byte[0];

  public static IMyBinder binder;
  public static boolean ISCONNECT;

  // Bluetooth
  BluetoothAdapter bluetoothAdapter;
  private Set<BluetoothDevice> mPairedDevices;

  // bindService connection
  ServiceConnection conn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      // Bind successfully
      binder = (IMyBinder) iBinder;
      Log.e("binder", "connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      Log.e("disbinder", "disconnected");
    }
  };

  public XprinterModule(ReactApplicationContext reactContext) {
    super(reactContext);

    this.context = reactContext;

    Intent intent = new Intent(this.context, PosprinterService.class);
    intent.putExtra("isconnect", true); // add
    this.context.bindService(intent, conn, BIND_AUTO_CREATE);
    Log.v(NAME, "RNXNetprinter alloc");
  }

  @Override
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void printTcp80mm(String ipAddress, int port, String payload, final Promise promise) {
    if (StringUtils.isBlank(ipAddress) || port <= 0) {
      promise.reject("-1", "Should provide valid ip address");
      return;
    }
    if (StringUtils.isBlank(payload)) {
      promise.reject("-1", "Should provide valid pageLoad to print");
      return;
    }
    int characterSize = 16;

    IPOSListener connectListener = new IPOSListener() {
      @Override
      public void onStatus(int code, String s) {
        switch (code) {
          case POSConnect.CONNECT_SUCCESS: {
            break;
          }
          case POSConnect.CONNECT_FAIL: {
            break;
          }
        }
      }
    };

    // Try to print with new libs
    POSConnect.init(this.context);
    IDeviceConnection curConnect = null;
    curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET);
    curConnect.connect(ipAddress, connectListener);
    POSPrinter printer = new POSPrinter(curConnect);
    printer.initializePrinter();
    printer.printString(payload)
            .feedLine()
            .cutHalfAndFeed(1);
    curConnect.close();
  }
}
