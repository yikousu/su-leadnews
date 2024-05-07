package com.su.common.aliyun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.green.model.v20180509.TextScanRequest;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.util.*;

// @Data
// @ConfigurationProperties(prefix = "aliyun")
//这个类也要放入starter 交给容器管理
public class GreenTextScan {
    @Value("${aliyun.AccessKey:scenes}")  //带默认值
    private String AccessKey;
    @Value("${aliyun.AccessKeySecret:scenes}")
    private String AccessKeySecret;

    /**
     * 此函数检测文本
     * @param content
     * @return
     */

    public Map<String, String> verify(String content) throws UnsupportedEncodingException {
        //================================需要改 start=============================================
        DefaultProfile profile = DefaultProfile.getProfile(
                "cn-shanghai",
                AccessKey,
                AccessKeySecret);

        //================================需要改 end===============================================

        DefaultProfile.addEndpoint("cn-shanghai", "Green", "green.cn-shanghai.aliyuncs.com");
        // 注意：此处实例化的client尽可能重复使用，提升检测性能。避免重复建立连接。
        IAcsClient client = new DefaultAcsClient(profile);
        TextScanRequest textScanRequest = new TextScanRequest();
        textScanRequest.setAcceptFormat(FormatType.JSON); // 指定API返回格式。
        textScanRequest.setHttpContentType(FormatType.JSON);
        textScanRequest.setMethod(com.aliyuncs.http.MethodType.POST); // 指定请求方法。
        textScanRequest.setEncoding("UTF-8");
        textScanRequest.setRegionId("cn-shanghai");
        List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
        Map<String, Object> task1 = new LinkedHashMap<String, Object>();
        task1.put("dataId", UUID.randomUUID().toString());

        //================================待检测的文本 start=============================================

        /**
         * 待检测的文本，长度不超过10000个字符。
         */
        task1.put("content", content);
        //================================待检测的文本 end===============================================

        tasks.add(task1);
        JSONObject data = new JSONObject();

        /**
         * 检测场景。文本垃圾检测请传递antispam。
         **/
        data.put("scenes", Arrays.asList("antispam"));
        data.put("tasks", tasks);
        System.out.println(JSON.toJSONString(data, true));
        textScanRequest.setHttpContent(data.toJSONString().getBytes("UTF-8"), "UTF-8", FormatType.JSON);
        // 请务必设置超时时间。
        textScanRequest.setConnectTimeout(3000);
        textScanRequest.setReadTimeout(6000);
        try {
            HttpResponse httpResponse = client.doAction(textScanRequest);
            if (!httpResponse.isSuccess()) {
                System.out.println("response not success. status:" + httpResponse.getStatus());
                // 业务处理。
                return null;
            }
            JSONObject scrResponse = JSON.parseObject(new String(httpResponse.getHttpContent(), "UTF-8"));
            System.out.println(JSON.toJSONString(scrResponse, true));
            if (200 != scrResponse.getInteger("code")) {
                System.out.println("detect not success. code:" + scrResponse.getInteger("code"));
                // 业务处理。
                return null;
            }
            JSONArray taskResults = scrResponse.getJSONArray("data");
            for (Object taskResult : taskResults) {
                if (200 != ((JSONObject) taskResult).getInteger("code")) {
                    System.out.println("task process fail:" + ((JSONObject) taskResult).getInteger("code"));
                    // 业务处理。
                    continue;
                }
                JSONArray sceneResults = ((JSONObject) taskResult).getJSONArray("results");
                //=====================定义map存储返回结果=============
                Map<String, String> resultMap = new HashMap<>();
                for (Object sceneResult : sceneResults) {
                    String scene = ((JSONObject) sceneResult).getString("scene");
                    String suggestion = ((JSONObject) sceneResult).getString("suggestion");
                    // 根据scene和suggestion做相关处理。
                    // suggestion为pass表示未命中垃圾。suggestion为block表示命中了垃圾，可以通过label字段查看命中的垃圾分类。
                    // System.out.println("args = [" + scene + "]");
                    // System.out.println("args = [" + suggestion + "]");
                    resultMap.put(scene, suggestion);
                }
                return resultMap;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) throws Exception {
        /**
         * 阿里云账号AccessKey拥有所有API的访问权限，建议您使用RAM用户进行API访问或日常运维。
         * 常见获取环境变量方式：
         * 方式一：
         *     获取RAM用户AccessKey ID：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
         *     获取RAM用户AccessKey Secret：System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
         * 方式二：
         *     获取RAM用户AccessKey ID：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_ID");
         *     获取RAM用户AccessKey Secret：System.getProperty("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
         */

        //================================需要改 start=============================================
        DefaultProfile profile = DefaultProfile.getProfile(
                "cn-shanghai",
                "xxxxxxx",
                "xxxxxxx");

        //================================需要改 end===============================================

        DefaultProfile.addEndpoint("cn-shanghai", "Green", "green.cn-shanghai.aliyuncs.com");
        // 注意：此处实例化的client尽可能重复使用，提升检测性能。避免重复建立连接。
        IAcsClient client = new DefaultAcsClient(profile);
        TextScanRequest textScanRequest = new TextScanRequest();
        textScanRequest.setAcceptFormat(FormatType.JSON); // 指定API返回格式。
        textScanRequest.setHttpContentType(FormatType.JSON);
        textScanRequest.setMethod(com.aliyuncs.http.MethodType.POST); // 指定请求方法。
        textScanRequest.setEncoding("UTF-8");
        textScanRequest.setRegionId("cn-shanghai");
        List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
        Map<String, Object> task1 = new LinkedHashMap<String, Object>();
        task1.put("dataId", UUID.randomUUID().toString());

        //================================待检测的文本 start=============================================

        /**
         * 待检测的文本，长度不超过10000个字符。
         */
        task1.put("content", "卖白粉我要吸毒");
        //================================待检测的文本 end===============================================

        tasks.add(task1);
        JSONObject data = new JSONObject();

        /**
         * 检测场景。文本垃圾检测请传递antispam。
         **/
        data.put("scenes", Arrays.asList("antispam"));
        data.put("tasks", tasks);
        System.out.println(JSON.toJSONString(data, true));
        textScanRequest.setHttpContent(data.toJSONString().getBytes("UTF-8"), "UTF-8", FormatType.JSON);
        // 请务必设置超时时间。
        textScanRequest.setConnectTimeout(3000);
        textScanRequest.setReadTimeout(6000);
        try {
            HttpResponse httpResponse = client.doAction(textScanRequest);
            if (!httpResponse.isSuccess()) {
                System.out.println("response not success. status:" + httpResponse.getStatus());
                // 业务处理。
                return;
            }
            JSONObject scrResponse = JSON.parseObject(new String(httpResponse.getHttpContent(), "UTF-8"));
            System.out.println(JSON.toJSONString(scrResponse, true));
            if (200 != scrResponse.getInteger("code")) {
                System.out.println("detect not success. code:" + scrResponse.getInteger("code"));
                // 业务处理。
                return;
            }
            JSONArray taskResults = scrResponse.getJSONArray("data");
            for (Object taskResult : taskResults) {
                if (200 != ((JSONObject) taskResult).getInteger("code")) {
                    System.out.println("task process fail:" + ((JSONObject) taskResult).getInteger("code"));
                    // 业务处理。
                    continue;
                }
                JSONArray sceneResults = ((JSONObject) taskResult).getJSONArray("results");
                for (Object sceneResult : sceneResults) {
                    String scene = ((JSONObject) sceneResult).getString("scene");
                    String suggestion = ((JSONObject) sceneResult).getString("suggestion");
                    // 根据scene和suggestion做相关处理。
                    // suggestion为pass表示未命中垃圾。suggestion为block表示命中了垃圾，可以通过label字段查看命中的垃圾分类。
                    System.out.println("args = [" + scene + "]");
                    System.out.println("args = [" + suggestion + "]");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
