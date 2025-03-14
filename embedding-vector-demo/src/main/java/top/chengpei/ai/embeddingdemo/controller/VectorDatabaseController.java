package top.chengpei.ai.embeddingdemo.controller;

import io.milvus.client.MilvusServiceClient;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RestController
public class VectorDatabaseController {

    @Resource
    private VectorStore vectorStore;

    @Autowired
    private ChatClient chatClient;

    @Resource
    private MilvusServiceClient milvusServiceClient;

    @GetMapping("/ai/vector/init")
    public String insert() {
        List<Document> documents = List.of(
                new Document("个人信息：姓名：张三，性别：男，30岁，1995年5月12日生，中国汉族，北京海淀居住，未婚。"),
                new Document("身体状况：身高175cm，体重70kg，健康良好，定期锻炼。"),
                new Document("小学教育：北京市海淀区实验小学毕业。"),
                new Document("初中教育：北京市海淀区第一中学就读。"),
                new Document("高中教育：毕业于北京市第四中学。"),
                new Document("本科学历：北京大学计算机科学与技术专业学士学位。"),
                new Document("研究生学历：清华大学人工智能专业硕士学位。"),
                new Document("第一份工作：百度软件工程师（2018.7-2020.6），负责搜索引擎后端开发。"),
                new Document("第一份工作在百度职责：参与分布式系统设计，优化搜索引擎性能。"),
                new Document("第二份工作：阿里高级算法工程师（2020.7-2022.6），专注推荐系统算法研究。"),
                new Document("第二份工作在阿里职责：提升个性化推荐准确性与效率。"),
                new Document("当前工作：字节AI研究员（2022.7至今），从事NLP与CV研究。"),
                new Document("当前在字节职责：推动AI技术发展与创新。"),
                new Document("2017年荣誉：荣获北京大学优秀毕业生称号。"),
                new Document("2019年荣誉：被评为百度科技有限公司年度优秀员工。"),
                new Document("2021年荣誉：获得阿里巴巴集团技术创新奖。"),
                new Document("2023年荣誉：荣获字节跳动科技有限公司AI研究突出贡献奖。"),
                new Document("阅读爱好：热衷科技、历史和文学书籍，关注AI与未来科技。"),
                new Document("运动爱好：热爱篮球与跑步，每周3-4次锻炼。"),
                new Document("旅行爱好：每年1-2次长途旅行，探索不同文化。"),
                new Document("音乐爱好：喜欢古典与流行音乐，会弹钢琴。"),
                new Document("其他爱好：对摄影和烹饪有兴趣，记录生活美好瞬间。"),
                new Document("编程技能：精通Python、Java、C++，熟悉Golang、JavaScript。"),
                new Document("技术专长：涵盖机器学习、深度学习、NLP与CV。"),
                new Document("可以熟练使用TensorFlow、PyTorch、Keras。"),
                new Document("可以熟练使用的数据科学工具：熟悉Scikit-learn、Pandas。"),
                new Document("社交账号以及联系方式：微信zhangsan_ai，微博@张三_AI研究员，GitHub https://github.com/zhangsan-ai，LinkedIn https://www.linkedin.com/in/zhangsan-ai，个人博客 https://zhangsan-ai.blog。"),
                new Document("个人特质：快速学习，勇于创新，团队合作，责任心强，具备领导力。"),
                new Document("个人格言：不断学习，勇于创新，用技术改变世界。"),
                new Document("未来规划：继续深造AI领域，攻读博士学位，成为行业专家，平衡工作与生活。")
        );
        vectorStore.add(documents);
        return "success";
    }

    @GetMapping(value = "/ai/vector/select", produces = "application/json")
    public List<Document> search(@RequestParam("message") String message) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(10).build());
        System.out.println(results);
        return results;
    }

    @GetMapping(value = "/ai/vector/chat", produces = "text/plain; charset=UTF-8")
    public Flux<String> generation(String message) {
        // 发起聊天请求并处理响应
        Flux<String> output = chatClient.prompt()
                .user(message)
                .advisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build()))
                .stream()
                .content();
        return output;
    }
}
