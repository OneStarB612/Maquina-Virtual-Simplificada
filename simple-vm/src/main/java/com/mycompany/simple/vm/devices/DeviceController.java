package com.mycompany.simple.vm.devices;

/**
 * Common interface representing a simulated peripheral controller.
 */
public interface DeviceController {
    /**
     * Returns the name of the simulated device.
     * @return String representation of the device name.
     */
    String getDeviceName();

    /**
     * Resets the device controller to its initial state.
     */
    void reset();
}
