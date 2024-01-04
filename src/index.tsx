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
    printBluetooth(
      macAddress: string,
      payload: string,
    ): Promise<void>;
    getBluetoothDeviceList(): Promise<BluetoothPrinter[]>;
  };
};

const { RNXprinter }: NativeModuleType =
  NativeModules as NativeModuleType;

interface PrinterInterface {
  payload: string;
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

const printBluetooth = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    macAddress,
    payload,
  } = getConfig(args);

  return RNXprinter.printBluetooth(
    macAddress,
    payload,
  );
};

const getBluetoothDeviceList = (): Promise<BluetoothPrinter[]> => {
  return RNXprinter.getBluetoothDeviceList();
};

export default {
  printTcp80mm,
  printBluetooth,
  defaultConfig,
  getBluetoothDeviceList,
};

