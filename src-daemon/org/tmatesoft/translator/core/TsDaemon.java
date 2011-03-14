package org.tmatesoft.translator.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.tmatesoft.translator.internal.jna.IKernel32;

import com.sun.jna.LastErrorException;

public class TsDaemon {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File log = findLogFile();
		for (int i = 0; i < args.length; i++) {
			log(log, "args[" + i + "] = " + args[i]);
			if ("/daemon".equals(args[i])) {
				synchronized (Thread.currentThread()) {
					try {
						log(log, "waiting...");
						Thread.currentThread().wait();
					} catch (InterruptedException e) {
//						e.printStackTrace();
					}
					log(log, "wait interrupted");
				}
				System.exit(0);
			}
		} 
		try {
			log(log, "about to daemonize");
			daemonize();
		} catch (IOException e) {
		}
		System.exit(0);
	}
	
	private static File findLogFile() {
		File dir = new File("").getAbsoluteFile();
		for (int i = 0; i < 100; i++) {
			File f = new File(dir, "log." + i + ".txt");
			if (!f.exists()) {
				return f;
			}
		}
		return null;
	}
	
	private static void log(File log, String message) {
		try {
			FileWriter writer = new FileWriter(log, true);
			writer.append(message);
			writer.append("\n");
			writer.close();
		} catch (IOException e) {}
		System.out.println(message);
	}

	private static void daemonize() throws IOException {
		String path = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java.exe";
		path = "\"" + path + "\"";
		String classPath = System.getProperty("java.class.path");
		String commandLine = path +  " -cp " + classPath + " " +TsDaemon.class.getCanonicalName().replace('/', '.') + " /daemon\0";
		String directory = new File("").getAbsolutePath();
		
		IKernel32.StartupInfo si = new IKernel32.StartupInfo();
		si.clear();
		si.cb = si.size();
		si.lpTitle = "TITLE";
		IKernel32.ProcessInformation pi = new IKernel32.ProcessInformation();
		pi.clear();
		si.write();
		pi.write();
		
		System.out.println("cmd line: " + commandLine);
		boolean result = false;
		try {
			result = IKernel32.INSTANCE.CreateProcess(
					null, 
					commandLine.toCharArray(), 
					null, 
					null, 
					false, 
//					0x08000200, 
					0x10,
					null, 
					directory, 
					si.getPointer(), 
					pi.getPointer());
			
		} catch (LastErrorException e) {
			e.printStackTrace();
		}
		
		pi.read();
		if (result) {
			System.out.println("PID: " + pi.dwProcessId);
		} else {
			System.out.println("ERROR");
		}
	}

}
