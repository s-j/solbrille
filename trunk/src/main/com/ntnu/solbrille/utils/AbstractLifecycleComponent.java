package com.ntnu.solbrille.utils;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public abstract class AbstractLifecycleComponent implements LifecycleComponent {

    private Exception failCause;
    private boolean isRunning;


    public void restart() {
        stop();
        start();
    }

    protected void setIsRunning(boolean running) {
        isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isFailed() {
        return failCause == null;
    }

    protected void setFailCause(Exception e) {
        failCause = e;
    }

    public Exception getFailCause() {
        return failCause;
    }
}
