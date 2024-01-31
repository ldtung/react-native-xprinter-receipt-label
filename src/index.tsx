import { NativeModules } from 'react-native';

type BluetoothPrinter = {
  deviceName: string;
  macAddress: string;
};

type NativeModuleType = typeof NativeModules & {
  RNXprinter: {
    printTcp80mm(
      ip: string,
      port: number,
      payload: string,
      closeAfterPrinted: boolean,
    ): Promise<void>;
    printTcp58mm(
      ip: string,
      port: number,
      payload: string,
      closeAfterPrinted: boolean,
    ): Promise<void>;
    printBluetooth80mm(
      macAddress: string,
      payload: string,
      closeAfterPrinted: boolean,
    ): Promise<void>;
    printBluetooth58mm(
      macAddress: string,
      payload: string,
      closeAfterPrinted: boolean,
    ): Promise<void>;
    printUsb80mm(
      payload: string,
      usbDeviceName: string,
      closeAfterPrinted: boolean,
    ): Promise<void>;
    printUsb58mm(
      payload: string,
      usbDeviceName: string,
      closeAfterPrinted: boolean,
    ): Promise<void>;
    getBluetoothDeviceList(): Promise<BluetoothPrinter[]>;
    getUsbDeviceList(): Promise<string[]>;
    closeTcpConnection(): Promise<boolean>;
    closeBluetoohConnection(): Promise<boolean>;
    closeUsbConnection(): Promise<boolean>;
  };
};

const { RNXprinter }: NativeModuleType =
  NativeModules as NativeModuleType;

interface PrinterInterface {
  payload: string;
  usbDeviceName: string;
  labelWidth: number,
  labelHeight: number,
  labelGap: number,
  labelSpaceLeft: number,
  labelSpaceTop: number,
  closeAfterPrinted: boolean,
}

interface PrintTcpInterface extends PrinterInterface {
  ip: string;
  port: number;
}

interface PrintBluetoothInterface extends PrinterInterface {
  macAddress: string;
}

let defaultConfig: PrintTcpInterface & PrintBluetoothInterface = {
  macAddress: '',
  ip: '192.168.192.168',
  port: 9100,
  payload: '',
  usbDeviceName: '',
  labelWidth: 50,
  labelHeight: 30,
  labelGap: 2,
  labelSpaceLeft: 0,
  labelSpaceTop: 6,
  closeAfterPrinted: true,
};

const getConfig = (
  args: Partial<typeof defaultConfig>
): typeof defaultConfig => {
  return Object.assign({}, defaultConfig, args);
};

const printTcp80mm = async (
  args: Partial<PrintTcpInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    ip,
    port,
    payload,
    closeAfterPrinted,
  } = getConfig(args);

  await RNXprinter.printTcp80mm(
    ip,
    port,
    payload,
    closeAfterPrinted,
    );
  };

const printTcp58mm = async (
  args: Partial<PrintTcpInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    ip,
    port,
    payload,
    closeAfterPrinted,
  } = getConfig(args);

  await RNXprinter.printTcp58mm(
    ip,
    port,
    payload,
    closeAfterPrinted,
    );
  };

const printBluetooth80mm = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    macAddress,
    payload,
    closeAfterPrinted,
  } = getConfig(args);

  return RNXprinter.printBluetooth80mm(
    macAddress,
    payload,
    closeAfterPrinted,
  );
};

const printBluetooth58mm = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    macAddress,
    payload,
    closeAfterPrinted,
  } = getConfig(args);

  return RNXprinter.printBluetooth58mm(
    macAddress,
    payload,
    closeAfterPrinted,
  );
};

const printUsb80mm = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    payload,
    usbDeviceName,
    closeAfterPrinted,
  } = getConfig(args);

  return RNXprinter.printUsb80mm(
    payload,
    usbDeviceName,
    closeAfterPrinted,
  );
};

const printUsb58mm = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    payload,
    usbDeviceName,
    closeAfterPrinted,
  } = getConfig(args);

  return RNXprinter.printUsb58mm(
    payload,
    usbDeviceName,
    closeAfterPrinted,
  );
};

const getBluetoothDeviceList = (): Promise<BluetoothPrinter[]> => {
  return RNXprinter.getBluetoothDeviceList();
};

const getUsbDeviceList = (): Promise<string[]> => {
  return RNXprinter.getUsbDeviceList();
};

const closeTcpConnection = (): Promise<boolean> => {
  return RNXprinter.closeTcpConnection();
};

const closeBluetoohConnection = (): Promise<boolean> => {
  return RNXprinter.closeBluetoohConnection();
};

const closeUsbConnection = (): Promise<boolean> => {
  return RNXprinter.closeUsbConnection();
};

export default {
  printTcp80mm,
  printTcp58mm,
  printBluetooth80mm,
  printBluetooth58mm,
  printUsb80mm,
  printUsb58mm,
  defaultConfig,
  getBluetoothDeviceList,
  getUsbDeviceList,
  closeTcpConnection,
  closeBluetoohConnection,
  closeUsbConnection,
};

