package de.lddt.zeichenroboterapp.listener;

/**
 * Created by Tim on 21.05.2016.
 */
public interface TransferListener {
    void onConnect();
    void onProgressUpdate(int progress, int packageCount);
    void onFinished();
    void error();
}
