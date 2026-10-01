package com.sanskritisathi.app;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Client for the Supabase Edge Function B2 gateway.
 * B2 credentials never live in the APK.
 */
public final class B2MediaHelper {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private B2MediaHelper() {}

    public interface UploadCallback {
        void onProgress(int progress);
        void onSuccess(String fileName);
        void onError(String message);
    }

    public interface UrlCallback {
        void onSuccess(String url);
        void onError(String message);
    }

    public static void uploadVideo(Context context, Uri uri, String folder, UploadCallback cb) {
        if (context == null || uri == null) { error(cb, "Video select nahi hui."); return; }
        new Thread(() -> {
            HttpURLConnection c = null;
            try {
                String token = SupabaseAuthManager.getAccessToken(context);
                if (TextUtils.isEmpty(token)) throw new Exception("Login session nahi mili.");
                long size = fileSize(context, uri);
                String ext = extension(context, uri);
                String fileName = "reel_" + System.currentTimeMillis() + "_" + Math.abs(uri.toString().hashCode()) + "." + ext;
                URL u = new URL(SupabaseConfig.PROJECT_URL + "/functions/v1/b2-media?action=upload_media");
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST"); c.setDoOutput(true); c.setConnectTimeout(30000); c.setReadTimeout(300000);
                c.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY);
                c.setRequestProperty("Authorization", "Bearer " + token);
                c.setRequestProperty("Content-Type", mime(context, uri));
                c.setRequestProperty("x-media-file-name", fileName);
                c.setRequestProperty("x-media-folder", TextUtils.isEmpty(folder) ? "media" : folder);
                if (size >= 0) c.setFixedLengthStreamingMode(size); else c.setChunkedStreamingMode(64 * 1024);
                InputStream in = context.getContentResolver().openInputStream(uri);
                if (in == null) throw new Exception("Video read nahi ho saki.");
                OutputStream out = c.getOutputStream();
                byte[] buffer = new byte[64 * 1024]; int n; long total = 0;
                progress(cb, 5);
                while ((n = in.read(buffer)) != -1) { out.write(buffer,0,n); total += n; if (size > 0) progress(cb, 5 + (int)Math.min(90, total * 90L / size)); }
                out.flush(); out.close(); in.close();
                int code = c.getResponseCode(); String response = read(c, code);
                if (code < 200 || code >= 300) throw new Exception("B2 upload failed: HTTP " + code + " " + response);
                JSONObject json = new JSONObject(response);
                if (!json.optBoolean("success", false)) throw new Exception(json.optString("error", "B2 upload failed."));
                String saved = json.optString("fileName", "");
                if (TextUtils.isEmpty(saved)) throw new Exception("B2 fileName missing.");
                progress(cb, 100); final String result = saved; MAIN.post(() -> cb.onSuccess(result));
            } catch (Exception e) { error(cb, e.getMessage()); } finally { if (c != null) c.disconnect(); }
        }).start();
    }

    public static void uploadImage(Context context, Uri uri, String folder, UploadCallback cb) {
        if (context == null || uri == null) { error(cb, "Image select nahi hui."); return; }
        new Thread(() -> {
            HttpURLConnection c = null;
            try {
                String token = SupabaseAuthManager.getAccessToken(context);
                if (TextUtils.isEmpty(token)) throw new Exception("Login session nahi mili.");
                long size = fileSize(context, uri);
                String fileName = "post_" + System.currentTimeMillis() + "_" + Math.abs(uri.toString().hashCode()) + ".jpg";
                URL u = new URL(SupabaseConfig.PROJECT_URL + "/functions/v1/b2-media?action=upload_media");
                c = (HttpURLConnection) u.openConnection(); c.setRequestMethod("POST"); c.setDoOutput(true); c.setConnectTimeout(30000); c.setReadTimeout(180000);
                c.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY); c.setRequestProperty("Authorization", "Bearer " + token); c.setRequestProperty("Content-Type", "image/jpeg"); c.setRequestProperty("x-media-file-name", fileName); c.setRequestProperty("x-media-folder", TextUtils.isEmpty(folder)?"posts":folder);
                if(size>=0)c.setFixedLengthStreamingMode(size); else c.setChunkedStreamingMode(64*1024);
                InputStream in=context.getContentResolver().openInputStream(uri); if(in==null)throw new Exception("Image read nahi ho saki."); OutputStream out=c.getOutputStream(); byte[] buf=new byte[32*1024]; int n; long total=0; progress(cb,5); while((n=in.read(buf))!=-1){out.write(buf,0,n);total+=n;if(size>0)progress(cb,5+(int)Math.min(90,total*90L/size));} out.flush();out.close();in.close();
                int code=c.getResponseCode();String response=read(c,code);if(code<200||code>=300)throw new Exception("B2 image upload failed: HTTP "+code+" "+response);JSONObject json=new JSONObject(response);if(!json.optBoolean("success",false))throw new Exception(json.optString("error","B2 image upload failed."));String saved=json.optString("fileName","");if(TextUtils.isEmpty(saved))throw new Exception("B2 fileName missing.");progress(cb,100);final String result=saved;MAIN.post(()->cb.onSuccess(result));
            }catch(Exception e){error(cb,e.getMessage());}finally{if(c!=null)c.disconnect();}
        }).start();
    }

    public static void resolveUrl(Context context, String fileName, UrlCallback cb) {
        if (context == null || TextUtils.isEmpty(fileName)) { urlError(cb, "B2 file missing."); return; }
        new Thread(() -> {
            HttpURLConnection c = null;
            try {
                String token = SupabaseAuthManager.getAccessToken(context);
                if (TextUtils.isEmpty(token)) throw new Exception("Login session nahi mili.");
                URL u = new URL(SupabaseConfig.PROJECT_URL + "/functions/v1/b2-media?action=get_media");
                c = (HttpURLConnection) u.openConnection(); c.setRequestMethod("POST"); c.setDoOutput(true); c.setConnectTimeout(30000); c.setReadTimeout(60000);
                c.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY); c.setRequestProperty("Authorization", "Bearer " + token); c.setRequestProperty("Content-Type", "application/json");
                JSONObject body = new JSONObject(); body.put("fileName", fileName); body.put("validDurationInSeconds", 3600);
                try (OutputStream out = c.getOutputStream()) { out.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
                int code = c.getResponseCode(); String response = read(c, code);
                if (code < 200 || code >= 300) throw new Exception("B2 URL failed: HTTP " + code + " " + response);
                JSONObject json = new JSONObject(response); String url = json.optString("downloadUrl", "");
                if (!json.optBoolean("success", false) || TextUtils.isEmpty(url)) throw new Exception(json.optString("error", "B2 download URL missing."));
                final String result = url; MAIN.post(() -> cb.onSuccess(result));
            } catch (Exception e) { urlError(cb, e.getMessage()); } finally { if (c != null) c.disconnect(); }
        }).start();
    }

    private static void progress(UploadCallback cb, int p){ if(cb!=null) MAIN.post(() -> cb.onProgress(Math.max(0,Math.min(100,p)))); }
    private static void error(UploadCallback cb,String m){ if(cb!=null) MAIN.post(() -> cb.onError(TextUtils.isEmpty(m)?"B2 upload failed.":m)); }
    private static void urlError(UrlCallback cb,String m){ if(cb!=null) MAIN.post(() -> cb.onError(TextUtils.isEmpty(m)?"B2 URL failed.":m)); }
    private static String read(HttpURLConnection c,int code)throws Exception{ java.io.InputStream in=code>=400?c.getErrorStream():c.getInputStream(); if(in==null)return ""; StringBuilder b=new StringBuilder(); try(java.io.BufferedReader r=new java.io.BufferedReader(new java.io.InputStreamReader(in,StandardCharsets.UTF_8))){String l;while((l=r.readLine())!=null)b.append(l);} return b.toString(); }
    private static long fileSize(Context c,Uri u){ try(android.database.Cursor cur=c.getContentResolver().query(u,new String[]{android.provider.OpenableColumns.SIZE},null,null,null)){ if(cur!=null&&cur.moveToFirst()){int i=cur.getColumnIndex(android.provider.OpenableColumns.SIZE);if(i>=0&&!cur.isNull(i))return cur.getLong(i);}} }catch(Exception ignored){} return -1; }
    private static String mime(Context c,Uri u){String x=c.getContentResolver().getType(u);return TextUtils.isEmpty(x)?"video/mp4":x;}
    private static String extension(Context c,Uri u){String m=mime(c,u).toLowerCase();if(m.contains("webm"))return"webm";if(m.contains("quicktime"))return"mov";if(m.contains("3gpp"))return"3gp";return"mp4";}
}
