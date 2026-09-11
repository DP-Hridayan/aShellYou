package ashell.core.shizuku;

interface IShellProcess {
    ParcelFileDescriptor getInputStream() = 1;
    ParcelFileDescriptor getErrorStream() = 2;
    ParcelFileDescriptor getOutputStream() = 3;
    int waitFor() = 4;
    int exitValue() = 5;
    boolean alive() = 6;
    void destroy() = 7;
}
