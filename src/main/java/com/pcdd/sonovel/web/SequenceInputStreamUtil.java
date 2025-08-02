package com.pcdd.sonovel.web;

import java.io.*;
import java.util.Vector;

public class SequenceInputStreamUtil {
    static InputStream originalIn;
    static PipedOutputStream stringPipeOut;
    static SequenceInputStream sequenceInput;

    static void repaceInputStream(){
        try {
            originalIn= System.in;
            // 1. 创建程序输入管道
            stringPipeOut = new PipedOutputStream();
            PipedInputStream stringPipeIn = null;
            stringPipeIn = new PipedInputStream(stringPipeOut, 1024);
            // 2. 创建输入流集合
            Vector<InputStream> inputStreams = new Vector<>();
            inputStreams.add(stringPipeIn); // 程序输入流
            inputStreams.add(originalIn);  // 控制台输入流

            // 3. 创建序列输入流
            sequenceInput = new SequenceInputStream(inputStreams.elements());

            // 4. 重定向系统输入
            System.setIn(sequenceInput);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    static void inputMessage(String message){
        if(stringPipeOut != null){
            try {
                stringPipeOut.write((message + '\n').getBytes());
                stringPipeOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
