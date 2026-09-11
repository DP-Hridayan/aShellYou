package ashell.core.shizuku;

import ashell.core.shizuku.IShellProcess;

interface IShellUserService {
    void destroy() = 16777114;
    IShellProcess newProcess(in String[] cmd, in String[] env, in String dir) = 1;
    int getUid() = 2;
}
