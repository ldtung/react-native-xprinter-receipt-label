package com.analy.receiptlabel;

import android.graphics.Color;
import android.graphics.Paint;

import com.facebook.react.bridge.ReactApplicationContext;
import com.github.danielfelgar.drawreceiptlib.ReceiptBuilder;

import net.posprinter.IDeviceConnection;
import net.posprinter.IPOSListener;
import net.posprinter.POSConnect;
import net.posprinter.POSConst;
import net.posprinter.POSPrinter;

import java.util.ArrayList;
import java.util.List;

public class AnalyPosListener80mm implements IPOSListener {
    private IDeviceConnection curConnect;
    private ReactApplicationContext context;
    private List<PrinterLine> lines = new ArrayList<>();
    protected int receiptWidth;
    protected float defaultTextSize;

    public AnalyPosListener80mm(ReactApplicationContext context, IDeviceConnection curConnect, List<PrinterLine> linesToPrint) {
        this.curConnect = curConnect;
        this.context = context;
        this.lines.clear();
        this.lines.addAll(linesToPrint);

        this.receiptWidth = 520;
        this.defaultTextSize = 75f;
    }
    @Override
    public void onStatus(int code, String message) {
        switch (code) {
            case POSConnect.CONNECT_SUCCESS: {
                try {
                    POSPrinter printer = new POSPrinter(curConnect);
                    printer.initializePrinter();
                    ReceiptBuilder receipt = new ReceiptBuilder(1200);
                    receipt.setMargin(2, 2);
//                    for (PrinterLine line : lines) {
//                        if (line.isNewLine) {
//                            receipt.addLine();
//                            continue;
//                        }
//                        receipt.setTypeface(this.context, line.isBold ? "font/RobotoMono-Bold.ttf" : "font/RobotoMono-Regular.ttf");
//                        receipt.setTextSize(line.textSize != null ? line.textSize : defaultTextSize);
//                        receipt.setAlign(line.align != null ? line.align : Paint.Align.LEFT);
//                        receipt.setColor(line.textColor != null ? line.textColor : Color.BLACK);
//                        receipt.addText(line.text, line.isNewLine);
//                    }
                    receipt.setTextSize(90F);
                    receipt.addText("Bún mắm nêm bún nem nướng Tôi yêu tổ quốc tôi lắm.");
                    receipt.addText("Đây là nắng nem nướng nha trang à ứ ừ ư ự!");
                    printer.printBitmap(receipt.build(), POSConst.ALIGNMENT_CENTER, receiptWidth);
                    printer.feedLine();
                    printer.cutHalfAndFeed(1);
                    curConnect.close();
                } catch (Exception ex) {
                    // Error while printing
                } finally {
                    if (curConnect != null) {
                        try {
                            curConnect.close();
                        } catch (Exception ex) {
                        } finally {
                            // Do nothing.
                        }
                    }
                }
                break;
            }
            case POSConnect.CONNECT_FAIL: {
                break;
            }
        }
    }
}
