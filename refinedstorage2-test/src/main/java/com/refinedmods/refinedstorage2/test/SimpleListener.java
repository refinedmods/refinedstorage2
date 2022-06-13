package com.refinedmods.refinedstorage2.test;

public class SimpleListener implements Runnable {
    private int changes;

    public int getChanges() {
        return changes;
    }

    public boolean isChanged() {
        return changes > 0;
    }

    public void reset() {
        changes = 0;
    }

    @Override
    public void run() {
        changes++;
    }
}
