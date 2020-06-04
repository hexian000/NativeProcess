package me.hexian000.nativeprocess.api;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class DaemonService extends Service implements Runnable {
    private Process shell;
    private Scanner in;
    private Binder mBinder;
    private ProcSample sample;

    @Override
    public boolean onUnbind(Intent intent) {
        in.close();
        shell.destroy();
        return false;
    }

    @Override
    public void run() {
        try {
            ProcSample sample = new ProcSample();
            while (in.hasNext()) {
                String line = in.nextLine();
                if (line.startsWith("END")) {
                    this.sample = sample;
                    sample = new ProcSample();
                    continue;
                }
                sample.add(line);
            }
        } catch (IllegalStateException ignored) {
        }
    }

    class Binder extends android.os.Binder {
        public ProcSample getSample() {
            return DaemonService.this.sample;
        }
    }

    public DaemonService() {
        mBinder = new Binder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        try {
            shell = Runtime.getRuntime().exec("su");
            in = new Scanner(shell.getInputStream());
            PrintStream out = new PrintStream(shell.getOutputStream());
            out.println(getApplicationInfo().nativeLibraryDir + "/libtasks.so");
            out.flush();
            out.close();
        } catch (IOException ignored) {
        }
        new Thread(this).start();
        return mBinder;
    }


}
