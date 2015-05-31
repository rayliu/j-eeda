package com.jfinal.weixin.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Formatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;  

import org.apache.commons.httpclient.NameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.jfinal.log.Logger;

import controllers.yh.arap.AccountAuditLogController;


public class SignKit {
	private static Logger logger = Logger.getLogger(SignKit.class);
    public static void main(String[] args) throws ClientProtocolException, IOException, ParseException, JSONException {
        /*String jsapi_ticket = "jsapi_ticket";

        // 注意 URL 一定要动态获取，不能 hardcode
        //String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx04d6e8e83464b0b4&secret=f67d4c549da09ab818f11f9e862b77e7";
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx1a63f107b6b0815c&secret=7d5db66c2eabfd11d6bd555380e529bb";
        Map<String, String> ret = sign(jsapi_ticket, url);
        for (Map.Entry entry : ret.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
        */
    	
    	CloseableHttpClient httpclient = HttpClients.createDefault();
        try {            
        	HttpGet httpGet = new HttpGet("https://api.weixin.qq.com/sns/oauth2/access_token?appid=123213&secret=1232131&code=1233213&grant_type=authorization_code");
            CloseableHttpResponse response1 = httpclient.execute(httpGet);
            try {
                HttpEntity entity = response1.getEntity();
                JSONObject json = new JSONObject(EntityUtils.toString(entity));  
                String errcode = json.getString("errcode");
                System.out.println("errcode:"+errcode);
                
                String userInfoUrl="https://api.weixin.qq.com/sns/userinfo?access_token=1111&openid=1111&lang=zh_CN";
                httpGet = new HttpGet(userInfoUrl);
                response1 = httpclient.execute(httpGet);
                entity = response1.getEntity();
                System.out.println(EntityUtils.toString(entity));
            }catch(Exception e){
            	e.printStackTrace();
            } finally {
                response1.close();
            }
        } finally {
            httpclient.close();
        }
    };

    public static Map<String, String> sign(String jsapi_ticket, String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                  "&noncestr=" + nonce_str +
                  "&timestamp=" + timestamp +
                  "&url=" + url;
        logger.debug("string1:"+string1);

        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
            logger.debug("signature:"+signature);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
