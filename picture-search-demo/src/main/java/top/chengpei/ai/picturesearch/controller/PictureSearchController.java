package top.chengpei.ai.picturesearch.controller;

import com.alibaba.fastjson.JSONObject;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.SearchResultData;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.chengpei.ai.picturesearch.util.EmbeddingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class PictureSearchController {

    @Resource
    private MilvusServiceClient milvusServiceClient;

    // 暂时不支持的写法，废弃，详情参考：https://github.com/alibaba/spring-ai-alibaba/issues/375
//    @GetMapping(value = "/embedding", produces = "application/json")
//    public EmbeddingResponse upload() {
//        EmbeddingResponse embeddingResponse = embeddingModel.call(
//                new EmbeddingRequest(List.of("{\n" +
//                        "        \"contents\": [\n" +
//                        "            {\"image\": \"https://home.chengpei.top:8443/root/jiaotong1.png\"}\n" +
//                        "        ]\n" +
//                        "    }"),
//                        DashScopeEmbeddingOptions.builder()
//                                .withModel("multimodal-embedding-v1")
//                                .withDimensions(1024)
//                                .build()));
//        return embeddingResponse;
//    }

    @GetMapping(value = "/init", produces = "application/json")
    public String init() {
        List<String> picUrlList = new ArrayList<>();
        picUrlList.add("https://home.chengpei.top:8443/root/picture/aircraft1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/aircraft2.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/bot1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/bot2.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/car1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/car2.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/car3.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/cat1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/cat2.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/cat3.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/desk1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/desk2.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/dog1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/dog2.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/dog3.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/horse1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/horse2.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/house1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/house2.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/phone1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/phone2.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/phone3.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/tree1.jpg");
        picUrlList.add("https://home.chengpei.top:8443/root/picture/tree2.jpg");

        List<String> docIdArray = new ArrayList<>();
        List<String> contentArray = new ArrayList<>();
        List<JSONObject> metadataArray = new ArrayList<>();
        List<List<Float>> embeddingArray = new ArrayList<>();

        for (String picUrl : picUrlList) {
            docIdArray.add(UUID.randomUUID().toString());
            contentArray.add(picUrl);
            metadataArray.add(new JSONObject());
            embeddingArray.add(EmbeddingUtils.embedding(picUrl, "image"));
        }
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(MilvusVectorStore.DOC_ID_FIELD_NAME, docIdArray));
        fields.add(new InsertParam.Field(MilvusVectorStore.CONTENT_FIELD_NAME, contentArray));
        fields.add(new InsertParam.Field(MilvusVectorStore.METADATA_FIELD_NAME, metadataArray));
        fields.add(new InsertParam.Field(MilvusVectorStore.EMBEDDING_FIELD_NAME, embeddingArray));

        InsertParam insertParam = InsertParam.newBuilder()
                .withDatabaseName(MilvusVectorStore.DEFAULT_DATABASE_NAME)
                .withCollectionName("picture_search")
                .withFields(fields)
                .build();
        this.milvusServiceClient.insert(insertParam);
        return "success";
    }

    @GetMapping(value = "/search", produces = "application/json")
    public List<JSONObject> search(String message) {
        List<String> outFieldNames = new ArrayList<>();
        outFieldNames.add(MilvusVectorStore.DOC_ID_FIELD_NAME);
        outFieldNames.add(MilvusVectorStore.CONTENT_FIELD_NAME);
        var searchParamBuilder = SearchParam.newBuilder()
                .withDatabaseName(MilvusVectorStore.DEFAULT_DATABASE_NAME)
                .withCollectionName("picture_search")
                .withTopK(5)
                .withOutFields(outFieldNames)
                .withVectors(List.of(EmbeddingUtils.embedding(message, "text")))
                .withVectorFieldName(MilvusVectorStore.EMBEDDING_FIELD_NAME);
        R<SearchResults> searchResults = milvusServiceClient.search(searchParamBuilder.build());
        SearchResults resultsData = searchResults.getData();
        System.out.println(resultsData.getResults());
        List<JSONObject> result = new ArrayList<>();
        SearchResultData results = resultsData.getResults();
        for (int i = 0; i < results.getTopK(); i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docId", results.getFieldsData(0).getScalars().getStringData().getData(i));
            jsonObject.put("content", results.getFieldsData(1).getScalars().getStringData().getData(i));
            jsonObject.put("scores", results.getScores(i));
            result.add(jsonObject);
        }
        return result;
    }
}
