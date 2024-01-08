package com.analy.receiptlabel;

import android.graphics.Bitmap;
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
                    try {
                        printer.printString("Đây là nắng nem nướng nha trang");
                        ReceiptBuilder receipt = new ReceiptBuilder(1200);
                        receipt.setMargin(2, 2);
                        receipt.setAlign(Paint.Align.LEFT);
                        receipt.setColor(Color.BLACK);
                        receipt.setTextSize(90F);
                        printer.printString("Tới đây 1");
                        for (PrinterLine line : lines) {
                            if (line.isNewLine) {
                                receipt.addLine();
                                continue;
                            }
                            printer.printString("Tới đây 2");
                            //receipt.setTypeface(this.context, line.isBold ? "fonts/RobotoMono-Bold.ttf" : "fonts/RobotoMono-Regular.ttf");
                            receipt.setTextSize(line.textSize != null ? line.textSize : defaultTextSize);
                            receipt.setAlign(line.align != null ? line.align : Paint.Align.LEFT);
                            receipt.setColor(line.textColor != null ? line.textColor : Color.BLACK);
                            receipt.addText(line.text, line.isNewLine);
                            printer.printString("Tới đây 3 " + "- " + line.text);
                        }
                        printer.printString("Tới đây 4");


                        ReceiptBuilder receipt2 = new ReceiptBuilder(1200);
                        receipt2.setMargin(2, 2);
                        receipt2.setAlign(Paint.Align.LEFT);
                        receipt2.setColor(Color.BLACK);
                        receipt2.setTextSize(90F);
                        receipt2.addText("Bún mắm nêm bún nem nướng Tôi yêu tổ quốc tôi lắm. Đây là nắng nem nướng nha trang à ứ ừ ư ự!\n");
                        receipt2.addText("Tôi yêu tiếng việt việt nam, tôi là người việt nam, kiêu hùng", true);
                        receipt2.addText("Tôi yêu tiếng việt việt nam, tôi là người việt nam, kiêu hùng", true);
                        receipt2.addText("Tôi yêu tiếng việt việt nam, tôi là người việt nam, kiêu hùng", true);
                        Bitmap imageToPrint = receipt2.build();
                        if (imageToPrint == null) {
                            printer.printString("Tới đây 5 NULL");
                        } else {
                            printer.printString("Tới đây 5 NOT NULL");
                        }
                        printer.feedLine(2);
                        printer.printBitmap(imageToPrint, POSConst.ALIGNMENT_CENTER, 484)
                                .feedLine()
                                .cutHalfAndFeed(1);
                    } catch (Exception ex) {
                        printer.printString("Tới đây 6" + ex.getMessage());
                    }
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
