package in.hedera.reku.swimclock.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by rakeshkalyankar on 22/09/18.
 * Required Queue to serialize GATT commands
 */

public class BleQueue {
    private Queue<Action> bleQueue = new LinkedList<Action>();
    private BluetoothGatt mBluetoothGatt;
    public BleQueue(BluetoothGatt bluetoothGatt) {
        this.mBluetoothGatt = bluetoothGatt;
    }

    public void writeDescriptor(BluetoothGattDescriptor descriptor) {
        addAction(ActionType.writeDescriptor, descriptor);
    }

    public void onDescriptorWrite(BluetoothGatt gatt,
                                  BluetoothGattDescriptor descriptor, int status) {
        bleQueue.remove();
        nextAction();
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        addAction(ActionType.readCharacteristic, characteristic);
    }

    ;

    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        bleQueue.remove();
        nextAction();
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        addAction(ActionType.writeCharacteristic, characteristic);
    }

    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        bleQueue.remove();
        nextAction();
    }

    public void requestMtu(int mtu) {
        addAction(ActionType.requestMtu, mtu);
    }

    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        bleQueue.remove();
        nextAction();
    }

    public void requestConnectionPriority(int priority) {
        addAction(ActionType.connectionPriority, priority);
    }

    private void addAction(ActionType actionType, Object object) {
        bleQueue.add(new Action(actionType, object));
        // if there is only 1 item in the queue, then process it. If more than
        // 1,
        // we handle asynchronously in the callback.
        if (bleQueue.size() == 1)
            nextAction();
    }

    private void nextAction() {
        if (bleQueue.isEmpty())
            return;
        Action action = bleQueue.element();
        if (ActionType.writeDescriptor.equals(action.getType())) {
            mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor) action
                    .getObject());
        } else if (ActionType.writeCharacteristic.equals(action.getType())) {
            do {

            }  while(! mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic) action.getObject()));
            return;

        } else if (ActionType.readCharacteristic.equals(action.getType())) {
            mBluetoothGatt
                    .readCharacteristic((BluetoothGattCharacteristic) action
                            .getObject());
        } else if (ActionType.connectionPriority.equals(action.getType())) {
            mBluetoothGatt.requestConnectionPriority((Integer) action.getObject());
            bleQueue.remove();
            nextAction();
        } else if (ActionType.requestMtu.equals(action.getType())) {
            mBluetoothGatt.requestMtu((int) action.getObject());
        } else {
            Log.e("BLEQueue", "Undefined Action found");
        }
    }

    enum ActionType {
        writeDescriptor, readCharacteristic, writeCharacteristic, connectionPriority, requestMtu
    }

    public class Action {
        private final ActionType actionType;
        private final Object object;

        public Action(ActionType actionType, Object object) {
            this.actionType = actionType;
            this.object = object;
        }

        public ActionType getType() {
            return this.actionType;
        }

        public Object getObject() {
            return this.object;
        }
    }
}
