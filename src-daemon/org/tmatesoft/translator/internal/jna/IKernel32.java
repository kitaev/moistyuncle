package org.tmatesoft.translator.internal.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface IKernel32 extends StdCallLibrary {
	
	IKernel32 INSTANCE = (IKernel32) Native.loadLibrary("Kernel32", IKernel32.class, W32APIOptions.UNICODE_OPTIONS);
	
	boolean CreateProcess(String apname,
            char[] comline,
            Pointer p,
            Pointer p2,
            boolean inheritHandles,
            int createFlags,
            String environment,
            String directory,
            Pointer startinf,
            Pointer processInfo);

	public class StartupInfo extends Structure {
        public int cb;
        public String lpReserved;
        public String lpDesktop;
        public String lpTitle;
        public int dwX;
        public int dwY;
        public int dwXSize;
        public int dwYSize;
        public int dwXCountChars;
        public int dwYCountChars;
        public int dwFillAttribute;
        public int dwFlags;
        public short wShowWindow;
        public short cbReserved2;
        public Pointer lpReserved2;
        public Pointer hStdInput;
        public Pointer hStdOutput;
        public Pointer hStdError;
	}

	public class ProcessInformation extends Structure {
        public Pointer hProcess;
        public Pointer hThread;
        public int dwProcessId;
        public int dwThreadId;
	}
}
