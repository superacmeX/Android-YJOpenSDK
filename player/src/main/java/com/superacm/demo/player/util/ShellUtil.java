package com.superacm.demo.player.util;

import java.io.*;

public class ShellUtil {

    public static String runCmd(String[] args) throws Exception {
        try{
            Process process = Runtime.getRuntime().exec(args);

            InputStream iStream = process.getInputStream();

            // This is how we check whether it works
            return tryWriteProcessOutput(iStream);
        }catch(Exception e){
            throw new Exception(e);
        }
    }

    public static String ls_fds() throws Exception {
        int id = android.os.Process.myPid();

        //String[] cmd = new String[] {"lsof", "-p", String.valueOf(id)};
        String[] cmd = new String[] {"ls", "-l", String.format("/proc/%d/fd", id)};
        String output = ShellUtil.runCmd(cmd);
        return output;
    }

    private static String tryWriteProcessOutput(InputStream iStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));

        String output = "";
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                output += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }

        return output;
    }
}
