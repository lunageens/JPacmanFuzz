import managers.FileReaderManager;

/**
 * A separate thread called ProcessTimeoutHandler is created to monitor the timeout. If the timeout is reached,
 * it interrupts the jpacman process and sets a flag (timeoutReached) accordingly.
 */
class ProcessTimeoutHandler extends Thread {

    /**
     * Process to be monitored.
     */
    private final Process process;

    /**
     * Max time in milliseconds that is allowed for one jpacman process to run.
     */
    private final long timeout = FileReaderManager.getInstance().getConfigReader().getIterationTimeout();

    /**
     * Shall be true if time out of process is reached.
     */
    private boolean timeoutReached;

    /**
     * Constructor of ProcessTimeoutHandler class.
     *
     * @param process
     *         Process to be monitored
     */
    public ProcessTimeoutHandler(Process process) {
        this.process = process;
        this.timeoutReached = false;
    }

    /**
     * Will automatically be called when calling start method of thread class.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(timeout);
            if (process.isAlive()) {
                process.destroy();
                timeoutReached = true;
            }
        } catch (InterruptedException e) {
            // Thread interrupted, do nothing
        }
    }

    /**
     * Monitors if timeout is reached.
     *
     * @return true if timeout is reached, false otherwise
     */
    public boolean isTimeoutReached() {
        return timeoutReached;
    }
}

