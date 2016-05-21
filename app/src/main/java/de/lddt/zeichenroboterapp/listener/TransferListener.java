package de.lddt.zeichenroboterapp.listener;

/**
 * Listener interface for Actions while transferring vectors to the nxt brick.
 */
public interface TransferListener {
    //when trying to connect to the brick
    void onConnect();

    //when updating the amount of transferred packages
    void onProgressUpdate(int progress, int packageCount);

    //successfully transferred the date to the brick
    void onFinished();

    //an error occurred
    void error();
}
