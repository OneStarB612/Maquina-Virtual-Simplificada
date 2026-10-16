
package com.mycompany.simple.vm;

@FunctionalInterface
public interface VMListener {

    void onVMEvent(VMEvent event);
}

