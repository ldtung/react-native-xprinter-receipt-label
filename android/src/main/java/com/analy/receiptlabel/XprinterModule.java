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
import com.tinyhack.vncharset.VNCharsetProvider;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.BitmapCallback;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos80;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

  @SuppressLint({"MissingPermission", "NewApi"})
  @ReactMethod
  public void getDeviceList(Promise promise) {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      promise.reject("-100", "Not bluetooth adapter");
    } else if (bluetoothAdapter.isEnabled()) {
      mPairedDevices = bluetoothAdapter.getBondedDevices();
      WritableArray pairedDeviceList = Arguments.createArray();
      for (BluetoothDevice device : mPairedDevices) {
        WritableMap deviceMap = Arguments.createMap();
        deviceMap.putString("name", device.getName());
        deviceMap.putString("address", device.getAddress());
        pairedDeviceList.pushMap(deviceMap);
      }
      promise.resolve(pairedDeviceList);
    } else {
      promise.reject("-103", "BluetoothAdapter not open...");
    }
  }

  @ReactMethod
  public void connectPrinter(String bleAddress, final Promise promise) {
    binder.connectBtPort(bleAddress, new UiExecute() {
      @Override
      public void onsucess() {

        ISCONNECT = true;
        promise.resolve(true);
      }

      @Override
      public void onfailed() {
        ISCONNECT = false;
        promise.reject("-105", "Device address not exist.");
      }
    });
  }

  @ReactMethod
  public void disconnectPrinter(final Promise promise) {
    if (ISCONNECT) {
      binder.disconnectCurrentPort(new UiExecute() {
        @Override
        public void onsucess() {
          promise.resolve(true);
        }

        @Override
        public void onfailed() {
          promise.reject("-105", "Device not connect.");
        }
      });
    } else {
      promise.reject("-105", "Device not connect.");
    }
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
    binder.connectNetPort(ipAddress, port, new UiExecute() {
      @Override
      public void onsucess() {
        // start to print
        final String tempText = payload;
        binder.writeDataByYouself(
                new UiExecute() {
                  @Override
                  public void onsucess() {
                    Log.v(NAME, "pushText onsucess");
                  }

                  @Override
                  public void onfailed() {
                    Log.v(NAME, "pushText onfailed");
                  }
                }, new ProcessData() {
                  @Override
                  public List<byte[]> processDataBeforeSend() {

                    List<byte[]> list = new ArrayList<byte[]>();
                    String str = tempText;
                    if (StringUtils.isBlank(str)) {
                      return null;
                    }
                    // initialize the printer
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    list.add(DataForSendToPrinterPos80.CancelChineseCharModel());
//                    List<Integer> codepages = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8 ,9, 10, 16, 17, 18, 19, 20,
//                            21,22, 23, 24, 25, 27, 28, 29, 30, 31, 32, 255, 33, 50, 51, 52, 53,
//                            54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70,
//                            71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
//                            81, 82, 83, 84, 85, 86, 87, 88, 89);
//                    Collections.reverse(codepages);
//                    for (Integer codepage  : codepages) {
                    list.add(DataForSendToPrinterPos80.selectCharacterCodePage(27));
                    try {
                      byte[] b = str.getBytes("utf-8");

                      String strText  = (new String(b, "utf-8"));
                      Charset charsetObj = new VNCharsetProvider().charsetForName("TCVN-3-1");
                      byte[] textBytesToPrint = strText.getBytes(charsetObj);
                      list.add(PrinterCommands.ESC_ALIGN_CENTER);
                      list.add(DataForSendToPrinterPos80.selectCharacterSize(characterSize));
                      list.add(textBytesToPrint);

                      list.add(DataForSendToPrinterPos80.selectCharacterCodePage(27));
                      charsetObj = new VNCharsetProvider().charsetForName("TCVN-3-2");
                      textBytesToPrint = strText.getBytes(charsetObj);
                      list.add(PrinterCommands.ESC_ALIGN_CENTER);
                      list.add(DataForSendToPrinterPos80.selectCharacterSize(characterSize));
                      list.add(textBytesToPrint);
                    } catch (Exception ex) {

                    }

                    list.add(DataForSendToPrinterPos80.selectCharacterCodePage(27));
                    byte[] textBytesToPrint = StringUtils.strTobytes(str, "windows-1258");

                    list.add(PrinterCommands.ESC_ALIGN_CENTER);
                    list.add(DataForSendToPrinterPos80.selectCharacterSize(characterSize));
                    list.add(textBytesToPrint);
//                    }

                    // should add the command of print and feed line,because print only when one
                    // line is complete, not one line, no print
                    list.add(DataForSendToPrinterPos80.printAndFeedLine());
                    // cut pager
                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1));
                    return list;
                  }
                });

        // close printer
        binder.disconnectCurrentPort(new UiExecute() {
          @Override
          public void onsucess() {
            promise.resolve(true);
          }

          @Override
          public void onfailed() {
            promise.reject("-105", "Device not connect.");
          }
        });
      }

      @Override
      public void onfailed() {
        promise.reject("-105", "Device not connect.");
      }
    });
  }

  @ReactMethod
  public void pushFlashImage(Integer index) {

    binder.writeDataByYouself(
        new UiExecute() {
          @Override
          public void onsucess() {
            Log.v(NAME, "pushFlashImage onsucess");
          }

          @Override
          public void onfailed() {
            Log.v(NAME, "pushFlashImage onfailed");
          }
        }, new ProcessData() {
          @Override
          public List<byte[]> processDataBeforeSend() {

            List<byte[]> list = new ArrayList<byte[]>();
            if (index == 0) {
            } else {
              // initialize the printer
              // list.add( DataForSendToPrinterPos58.initializePrinter());
              list.add(DataForSendToPrinterPos80.initializePrinter());
              // align center data
              list.add(PrinterCommands.ESC_ALIGN_CENTER);
              // print flash image
              list.add(DataForSendToPrinterPos80.printBmpInFLASH(index, 0));

              return list;
            }
            return null;
          }
        });
  }

  private Bitmap b1;// grey-scale bitmap
  private Bitmap b2;// compress bitmap

  @ReactMethod
  private void pushImage(String base64img, final int width) {
    byte[] bytes = Base64.decode(base64img, Base64.DEFAULT);
    Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    if (b != null) {
      b1 = convertGreyImg(b);
      Message message = new Message();
      message.what = 1;
      handler.handleMessage(message);

      // compress the bitmap
      Tiny.BitmapCompressOptions options = new Tiny.BitmapCompressOptions();
      Tiny.getInstance().source(b1).asBitmap().withOptions(options).compress(new BitmapCallback() {
        @Override
        public void callback(boolean isSuccess, Bitmap bitmap, Throwable t) {
          if (isSuccess) {
            // Toast.makeText(PosActivity.this,"bitmap:
            // "+bitmap.getByteCount(),Toast.LENGTH_LONG).show();
            b2 = bitmap;
            b2 = resizeImage(b2, width, false);
            Message message = new Message();
            message.what = 2;
            handler.handleMessage(message);
          }
        }
      });
    }
  }

  public static Bitmap resizeImage(Bitmap bitmap, int w, boolean ischecked) {
    Bitmap BitmapOrg = bitmap;
    Bitmap resizedBitmap = null;
    int width = BitmapOrg.getWidth();
    int height = BitmapOrg.getHeight();
    if (width <= w) {
      return bitmap;
    }
    if (!ischecked) {
      int newWidth = w;
      int newHeight = height * w / width;

      float scaleWidth = ((float) newWidth) / width;
      float scaleHeight = ((float) newHeight) / height;

      Matrix matrix = new Matrix();
      matrix.postScale(scaleWidth, scaleHeight);
      // if you want to rotate the Bitmap
      // matrix.postRotate(45);
      resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
          height, matrix, true);
    } else {
      resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, w, height);
    }

    return resizedBitmap;
  }

  private void printPicCode(final Bitmap printBmp) {
    binder.writeDataByYouself(new UiExecute() {
      @Override
      public void onsucess() {
        Log.v(NAME, "printPicCode onsucess");
      }

      @Override
      public void onfailed() {
        Log.v(NAME, "printPicCode onfailed");
      }
    }, new ProcessData() {
      @Override
      public List<byte[]> processDataBeforeSend() {
        List<byte[]> list = new ArrayList<byte[]>();
        list.add(DataForSendToPrinterPos80.initializePrinter());
        list.add(PrinterCommands.ESC_ALIGN_CENTER);
        list.add(DataForSendToPrinterPos80.printRasterBmp(
            0, printBmp, BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Left, 576));
        // list.add(DataForSendToPrinterPos80.printAndFeedForward(3));
        return list;
      }
    });
  }

  public Handler handler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (msg.what == 2) {
        printPicCode(b2);
      }
    }
  };

  public Bitmap convertGreyImg(Bitmap img) {
    int width = img.getWidth();
    int height = img.getHeight();

    int[] pixels = new int[width * height];

    img.getPixels(pixels, 0, width, 0, 0, width, height);

    // The arithmetic average of a grayscale image; a threshold
    double redSum = 0, greenSum = 0, blueSun = 0;
    double total = width * height;

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        int grey = pixels[width * i + j];

        int red = ((grey & 0x00FF0000) >> 16);
        int green = ((grey & 0x0000FF00) >> 8);
        int blue = (grey & 0x000000FF);

        redSum += red;
        greenSum += green;
        blueSun += blue;

      }
    }
    int m = (int) (redSum / total);

    // Conversion monochrome diagram
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        int grey = pixels[width * i + j];

        int alpha1 = 0xFF << 24;
        int red = ((grey & 0x00FF0000) >> 16);
        int green = ((grey & 0x0000FF00) >> 8);
        int blue = (grey & 0x000000FF);

        if (red >= m) {
          red = green = blue = 255;
        } else {
          red = green = blue = 0;
        }
        grey = alpha1 | (red << 16) | (green << 8) | blue;
        pixels[width * i + j] = grey;

      }
    }
    Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

    return mBitmap;
  }

  @ReactMethod
  public void pushCutPaper() {
    binder.writeDataByYouself(new UiExecute() {
      @Override
      public void onsucess() {
        Log.v(NAME, "pushCutPaper onsucess");
      }

      @Override
      public void onfailed() {
        Log.v(NAME, "pushCutPaper onfailed");
      }
    }, new ProcessData() {
      @Override
      public List<byte[]> processDataBeforeSend() {
        List<byte[]> list = new ArrayList<byte[]>();
        list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1));
        return list;
      }
    });
  }
}
