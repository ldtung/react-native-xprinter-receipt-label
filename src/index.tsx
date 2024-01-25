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
    ): Promise<void>;
    printTcp58mm(
      ip: string,
      port: number,
      payload: string,
    ): Promise<void>;
    printLabelTcp(
      ip: string,
      port: number,
      payload: string,
      labelWidth: number,
      labelHeight: number,
      labelGap: number,
    ): Promise<void>;
    printBluetooth80mm(
      macAddress: string,
      payload: string,
    ): Promise<void>;
    printBluetooth58mm(
      macAddress: string,
      payload: string,
    ): Promise<void>;
    printLabelBluetooth(
      macAddress: string,
      payload: string,
      labelWidth: number,
      labelHeight: number,
      labelGap: number,
    ): Promise<void>;
    printUsb80mm(
      payload: string,
    ): Promise<void>;
    printUsb58mm(
      payload: string,
    ): Promise<void>;
    printLabelUsb(
      payload: string,
      labelWidth: number,
      labelHeight: number,
      labelGap: number,
    ): Promise<void>;
    getBluetoothDeviceList(): Promise<BluetoothPrinter[]>;
    getUsbDeviceList(): Promise<string[]>;
  };
};

const { RNXprinter }: NativeModuleType =
  NativeModules as NativeModuleType;

interface PrinterInterface {
  payload: string;
  labelWidth: number,
  labelHeight: number,
  labelGap: number,
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
  labelWidth: 50,
  labelHeight: 30,
  labelGap: 2
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
  } = getConfig(args);

  await RNXprinter.printTcp80mm(
    ip,
    port,
    payload,
    );
  };

const printTcp58mm = async (
  args: Partial<PrintTcpInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    ip,
    port,
    payload,
  } = getConfig(args);

  await RNXprinter.printTcp58mm(
    ip,
    port,
    payload,
    );
  };
const printLabelTcp = async (
  args: Partial<PrintTcpInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    ip,
    port,
    payload,
    labelWidth,
    labelHeight,
    labelGap,
  } = getConfig(args);

  await RNXprinter.printLabelTcp(
    ip,
    port,
    payload,
    labelWidth,
    labelHeight,
    labelGap,
    );
  };

const printBluetooth80mm = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    macAddress,
    payload,
  } = getConfig(args);

  return RNXprinter.printBluetooth80mm(
    macAddress,
    payload,
  );
};

const printBluetooth58mm = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    macAddress,
    payload,
  } = getConfig(args);

  return RNXprinter.printBluetooth58mm(
    macAddress,
    payload,
  );
};

const printLabelBluetooth = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    macAddress,
    payload,
    labelWidth,
    labelHeight,
    labelGap,
  } = getConfig(args);

  return RNXprinter.printLabelBluetooth(
    macAddress,
    payload,
    labelWidth,
    labelHeight,
    labelGap,
  );
};

const printUsb80mm = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    payload,
  } = getConfig(args);

  return RNXprinter.printUsb80mm(
    payload,
  );
};

const printUsb58mm = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    payload,
  } = getConfig(args);

  return RNXprinter.printUsb58mm(
    payload,
  );
};

const printLabelUsb = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    payload,
    labelWidth,
    labelHeight,
    labelGap,
  } = getConfig(args);

  return RNXprinter.printLabelUsb(
    payload,
    labelWidth,
    labelHeight,
    labelGap,
  );
};

const getBluetoothDeviceList = (): Promise<BluetoothPrinter[]> => {
  return RNXprinter.getBluetoothDeviceList();
};

const getUsbDeviceList = (): Promise<string[]> => {
  return RNXprinter.getUsbDeviceList();
};

export default {
  printTcp80mm,
  printTcp58mm,
  printBluetooth80mm,
  printBluetooth58mm,
  printUsb80mm,
  printUsb58mm,
  printLabelTcp,
  printLabelBluetooth,
  printLabelUsb,
  defaultConfig,
  getBluetoothDeviceList,
  getUsbDeviceList,
};

