package com.analy.receiptlabel;

import static android.content.Context.BIND_AUTO_CREATE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.IBinder;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.analy.receiptlabel.utils.StringUtils;
import com.facebook.react.module.annotations.ReactModule;
import com.github.danielfelgar.drawreceiptlib.ReceiptBuilder;

import net.posprinter.IDeviceConnection;
import net.posprinter.IPOSListener;
import net.posprinter.POSConnect;
import net.posprinter.POSConst;
import net.posprinter.POSPrinter;
import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.service.PosprinterService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static IDeviceConnection curEthernetConnect = null;

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

        // Try to print with new libs
        POSConnect.init(this.context);

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

        List<PrinterLine> lines = parsePayload(payload);
        ReactApplicationContext me = this.context;
        boolean needToReconnect = false;
        final List<String> toReconnectDebug = new ArrayList<>();
        toReconnectDebug.add("Reconnect now : false");
        if (XprinterModule.curEthernetConnect == null || !XprinterModule.curEthernetConnect.isConnect()) {
            if (XprinterModule.curEthernetConnect != null) {
                XprinterModule.curEthernetConnect.close();
                toReconnectDebug.add("--> Reconnect closed: true");
            } else {
                toReconnectDebug.add("--> Reconnect: true");
            }
            XprinterModule.curEthernetConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET);
            needToReconnect = true;
        }
        //curEthernetConnect.connect(ipAddress, new AnalyPosListener80mm(context, curEthernetConnect, lines));
        if (needToReconnect) {
            XprinterModule.curEthernetConnect.connect(ipAddress, new IPOSListener() {
                @Override
                public void onStatus(int i, String s) {
                    switch (i) {
                        case POSConnect.CONNECT_SUCCESS: {
                            doPrintingService(toReconnectDebug, me, lines);
                            break;
                        }
                        case POSConnect.CONNECT_FAIL: {
                            break;
                        }
                    }

                }
            });
        } else {
            // Trigger print now.
            doPrintingService(toReconnectDebug, me, lines);
        }
    }

    private static void doPrintingService(List<String> toReconnectDebug, ReactApplicationContext me, List<PrinterLine> lines) {
        try {
            POSPrinter printer = new POSPrinter(XprinterModule.curEthernetConnect);
            printer.initializePrinter();
            try {
                int marginDefault = 0;
                int receiptBuilderWidth = 1200;
                int receiptWidthFor80mm = 574;
                String fontRegular = "fonts/RobotoMono-Regular.ttf";
                String fontBold = "fonts/RobotoMono-Bold.ttf";
                float defaultTextSize = 70F;
                ReceiptBuilder receipt = new ReceiptBuilder(receiptBuilderWidth);
                receipt.setMargin(marginDefault, marginDefault);
                receipt.setAlign(Paint.Align.LEFT);
                receipt.setColor(Color.BLACK);
                receipt.setTextSize(defaultTextSize);
                receipt.setTypeface(me, fontRegular);
                for (PrinterLine line : lines) {
                    if (line.isNewLine) {
                        receipt.addLine();
                        continue;
                    } else if (line.isParagraph) {
                        receipt.addParagraph();
                        continue;
                    }

                    receipt.setTypeface(me, line.isBold ? fontBold : fontRegular);
                    receipt.setMargin(marginDefault, marginDefault);
                    receipt.setTextSize(line.textSize != null ? line.textSize : defaultTextSize);
                    receipt.setAlign(line.align != null ? line.align : Paint.Align.LEFT);
                    receipt.setColor(line.textColor != null ? line.textColor : Color.BLACK);
                    receipt.addText(line.text, !line.isSameLine);
                    if (!line.isSameLine && line.textSize != null && line.textSize > defaultTextSize) {
                        receipt.addBlankSpace(15);
                    }
                }
                Bitmap imageToPrint = receipt.build();
                printer.feedLine(marginDefault);
                printer.printBitmap(imageToPrint, POSConst.ALIGNMENT_CENTER, receiptWidthFor80mm);
            } catch (Exception ex) {
                printer.printString("Have error in printing " + ex.getMessage());
            }
            printer.feedLine();
            printer.cutHalfAndFeed(1);
        } catch (Exception ex) {
            // Error while printing
        }
    }

    private List<PrinterLine> parsePayload(String payload) {
        List<PrinterLine> lines = new ArrayList<>();
        String[] payloadItems = payload.split("@@NL@@");
        for (String payloadLine : payloadItems) {
            List<String> inlineStrings = splitString(payloadLine);
            for (String inlineText : inlineStrings) {
                if (StringUtils.isNotBlank(inlineText)) {
                    lines.add(buildPrinterLine(splitPrefixText(inlineText)));
                }
            }
        }
        return lines;
    }

    private static PrinterLine buildPrinterLine(String[] lineToPrintAndFormat) {
        PrinterLine line = new PrinterLine();
        line.text = lineToPrintAndFormat[1];
        if (StringUtils.isNotBlank(lineToPrintAndFormat[0])) {
            String lineFormat = lineToPrintAndFormat[0];
            lineFormat = lineFormat.replace("[", "");
            lineFormat = lineFormat.replace("]", "");
            String[] formats = lineFormat.split(",");
            for (String format : formats) {
                if ("B".equalsIgnoreCase(format)) {
                    line.isBold = true;
                } else if ("R".equalsIgnoreCase(format)) {
                    line.align = Paint.Align.RIGHT;
                } else if ("C".equalsIgnoreCase(format)) {
                    line.align = Paint.Align.CENTER;
                } else if ("XL".equalsIgnoreCase(format)) {
                    line.textSize = 90F;
                } else if ("L".equalsIgnoreCase(format)) {
                    line.textSize = 85F;
                } else if ("S".equalsIgnoreCase(format)) {
                    line.isSameLine = true;
                } else if ("P".equalsIgnoreCase(format)) {
                    line.isParagraph = true;
                }
            }
        }
        return line;
    }

    public static List<String> splitString(String input) {
        String[] letters = {"P", "S", "C", "B", "R", "XL", "L"};
        String regexPattern = generateRegexPattern(letters);
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(input);

        List<String> splitStrings = new ArrayList<>();
        int lastMatchEnd = 0;
        String currentPrefix = "";

        while (matcher.find()) {
            int start = matcher.start();
            if (start > lastMatchEnd) {
                splitStrings.add(currentPrefix + input.substring(lastMatchEnd, start).trim());
            }
            currentPrefix = matcher.group();
            lastMatchEnd = matcher.end();
        }

        if (lastMatchEnd < input.length()) {
            splitStrings.add(currentPrefix + input.substring(lastMatchEnd).trim());
        }

        return splitStrings;
    }

    public static String[] splitPrefixText(String line) {
        // Initialize the prefix and text strings
        String prefix = "";
        String text = "";

        // Find the index of the first closing square bracket ']' to determine the prefix
        int closingBracketIndex = line.indexOf(']');
        if (closingBracketIndex != -1) {
            prefix = line.substring(0, closingBracketIndex + 1); // Include the closing bracket
            text = line.substring(closingBracketIndex + 1).trim();
        } else {
            // If the closing bracket is not found, consider the whole line as text
            text = line.trim();
        }

        return new String[]{prefix, text};
    }

    public static String generateRegexPattern(String[] letters) {
        List<String> patterns = new ArrayList<>();

        for (int i = 1; i <= letters.length; i++) {
            generateCombinations(letters, 0, i, "", patterns);
        }

        return String.join("|", patterns);
    }

    private static void generateCombinations(String[] letters, int index, int length, String current, List<String> patterns) {
        if (length == 0) {
            patterns.add("\\[" + current.replaceAll(",", ",") + "\\]");
            return;
        }
        if (index == letters.length) {
            return;
        }

        if (!current.isEmpty()) {
            generateCombinations(letters, index + 1, length - 1, current + "," + letters[index], patterns);
        } else {
            generateCombinations(letters, index + 1, length - 1, letters[index], patterns);
        }
        generateCombinations(letters, index + 1, length, current, patterns);
    }
}
