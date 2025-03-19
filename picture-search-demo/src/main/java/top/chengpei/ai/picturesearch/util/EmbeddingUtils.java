package top.chengpei.ai.picturesearch.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbeddingUtils implements ApplicationContextAware {

    private static final String url = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal-embedding/multimodal-embedding";

    private static final String model = "multimodal-embedding-v1";

    private static String DASHSCOPE_API_KEY;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DASHSCOPE_API_KEY = applicationContext.getEnvironment().getProperty("spring.ai.dashscope.api-key");
    }

    public static List<Float> embedding(String content, String type) {
        EmbeddingRequest embeddingRequest = new EmbeddingRequest(content, type);
        HttpRequest httpRequest = HttpUtil.createPost(url);
        httpRequest.body(JSONUtil.toJsonStr(embeddingRequest));
        httpRequest.header("Content-Type", "application/json");
        httpRequest.header("Authorization", "Bearer " + DASHSCOPE_API_KEY);
        HttpResponse response = httpRequest.execute();
        String body = response.body();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        JSONArray jsonArray = jsonObject.getJSONObject("output").getJSONArray("embeddings").getJSONObject(0).getJSONArray("embedding");
        return jsonArray.toList(Float.class);
    }

    @Data
    public static class EmbeddingRequest {
        private String model;
        private Input input;

        EmbeddingRequest(String picUrl, String type) {
            this.model = EmbeddingUtils.model;
            JSONObject item = new JSONObject();
            item.set(type, picUrl);
            JSONArray contents = new JSONArray();
            contents.add(item);
            Input input = new Input();
            input.setContents(contents);
            this.input = input;
        }
    }

    @Data
    public static class Input {
        private JSONArray contents;
    }
}
