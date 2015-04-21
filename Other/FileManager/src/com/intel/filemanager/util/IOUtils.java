package com.intel.filemanager.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
    /**
     * General IO stream (include input stream and output cream).
     */
    public class IOUtils
    {
        /**
         * The default buffer size we set to read.
         */
    	private static final int DEFAULT_BUFFER_SIZE_TO_READ = 1024 * 4;
    	/**
    	 * buffer for transfer in bytes  data between fileinstream and fileoutstream.
    	 */
    	private static byte[] buffer = new byte[DEFAULT_BUFFER_SIZE_TO_READ];
    	  /**
    	 * The below is the basic introduce about IO stream
         * @param The default bytes we read from  InputStream  and to write the OutputStream.
         * @param input  the InputStream to read from the file
         * @param output  the OutputStream to write to the folder
         * @return the number of bytes copied from the folder(file) 
         * @throws NullPointerException  In some conditions may appear nullpointer
         * @throws IOException In some condition may appear IOException need to throws
         * @throws ArithmeticException  In this condition need to throws Arithmetic problems.
         */
        public static int copyFileStream(InputStream inputstream, OutputStream outputstream) throws IOException {
            long fileCount = copyLargeFileStream(inputstream, outputstream);
            if (fileCount > Integer.MAX_VALUE) {
                return -1;
            }
            return (int) fileCount;
        }

        /**
         * Copy bytes from a large file (size over 2GB) read from InputStream and write to an
         * OutputStream.
         * @param input  the InputStream to read from the file
         * @param output  the OutputStream to write to the folder
         * @return the number of one time bytes copied from the InputStream
         * @throws NullPointerException  In some conditions may appear nullpointer exception
         * @throws IOException In some condition may appear IOException need to throws
         */
        public static long copyLargeFileStream(InputStream inputstream, OutputStream outputstream)
                throws IOException {
            long filecount = 0;
            int num = 0;
            while (-1 != (num = inputstream.read(buffer, 0, DEFAULT_BUFFER_SIZE_TO_READ))) {
            	outputstream.write(buffer, 0, num);
                filecount += num;
            }
            return filecount;
        }
        
        /**
         * Unconditionally close an OutputStream.
         * Equivalent to unconditionally,any exceptions will be ignored.
         * This may typically used in the finally blocks when necessary.
         * @param OutputStream  the OutputStream to close, may be null or already have been closed
         */
        public static void closeQuietlyOutput(OutputStream outputstream) {
            try {
                if (outputstream != null) {
                	outputstream.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
        
        /**
         * Unconditionally close an InputStream .
         * Equivalent to unconditionally, any exceptions will be ignored.
         * This may typically used in the finally blocks when necessary.
         * @param inputstream  the InputStream to close, may be null or already have been closed
         */
        public static void closeQuietlyInput(InputStream inputstream) {
            try {
                if (inputstream != null) {
                	inputstream.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
