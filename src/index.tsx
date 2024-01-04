import { NativeModules } from 'react-native';

type BluetoothPrinter = {
  deviceName: string;
  macAddress: string;
};

type NativeModuleType = typeof NativeModules & {
  RNXprinter: {
    printTcp80mm(
      payload: string,
      ip: string,
      port: number,
    ): Promise<void>;
    printBluetooth(
      payload: string,
      macAddress: string,
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
  payload: '',
  macAddress: '',
  ip: '192.168.192.168',
  port: 9100,
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
    payload,
    ip,
    port,
  } = getConfig(args);

  await RNXprinter.printTcp80mm(
    payload,
    ip,
    port,
    );
  };

const printBluetooth = (
  args: Partial<PrintBluetoothInterface> & Pick<PrinterInterface, 'payload'>
): Promise<void> => {
  const {
    payload,
    macAddress,
  } = getConfig(args);

  return RNXprinter.printBluetooth(
    payload,
    macAddress,
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

