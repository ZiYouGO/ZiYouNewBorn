package com.mingle.ZiYou.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jd on 2016/6/16.
 */
    public class DownloadVoiceThread extends Thread {
        private String urlStr;             //下载链接
        private String fileName;           //文件名
        private File f_voice;
    private Context context;

        public DownloadVoiceThread(String urlStr,  String fileName,
                                   Context context) {
            this.urlStr = urlStr;
            this.fileName = fileName;
            this.context=context;
        }

        public void run() {
            FileOutputStream  output = null;
            URL url = null;

            try {
                url = new URL(urlStr);
                output=context.openFileOutput(fileName,Context.MODE_WORLD_READABLE);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                InputStream input = conn.getInputStream();
                int len=conn.getContentLength();
                    // 读取大文件
                    byte[] voice_bytes = new byte[1024];
                    int len1 = -1;
                    while ((len1 = input.read(voice_bytes)) != -1) {
                        output.write(voice_bytes, 0, len1);
                        output.flush();
                    }
                    System.out.println("success");
                    output.close();

            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
                //return;
            } finally {

            }
        }

}
