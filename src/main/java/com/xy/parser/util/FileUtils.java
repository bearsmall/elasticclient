package com.xy.parser.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {

    public static void write(String from,String to) throws IOException {
        File file = new File(to);
        FileOutputStream outputStream = new FileOutputStream(file);
        FileChannel channel = outputStream.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(50240);
        String string = read(from);
        buffer.put(string.getBytes());
        buffer.flip();     //此处必须要调用buffer的flip方法
        channel.write(buffer);
        channel.close();
        outputStream.close();
    }

    public static String read(String from) throws IOException {
        FileInputStream fin = new FileInputStream(from);
        // 获取通道
        FileChannel fc = fin.getChannel();
        // 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(50240);
        // 读取数据到缓冲区
        fc.read(buffer);
        buffer.flip();
        StringBuffer s=new StringBuffer();
        while (buffer.remaining() > 0) {
            byte b = buffer.get();
            s.append((char)b);
        }
        fin.close();
        return s.toString();
    }
}
