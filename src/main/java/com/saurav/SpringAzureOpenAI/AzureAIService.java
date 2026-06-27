package com.saurav.SpringAzureOpenAI;

import com.openai.models.audio.speech.SpeechModel;
import com.openai.models.images.ImagesResponse;
import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class AzureAIService {

    @Autowired
    private ImageModel imageModel;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private TranscriptionModel transcriptionModel;

    @Autowired
    private VectorStore vectorStore;

    private ChatClient chatClient;

    public AzureAIService(ChatClient.Builder builder, ChatMemory chatMemory){
        chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }

    public String getAnswer(String prompt, String id) throws IOException {

        return chatClient.prompt(prompt)
                .advisors(x -> x.param(ChatMemory.CONVERSATION_ID, id))
                .system("You are a lyric writer")
                .call().content();
    }

    public void generateImage(String prompt,String name) throws IOException {
        ImageOptions options = OpenAiImageOptions.builder()
                .azure(true)
                .model("sauravazimage")
                .height(1024)
                .width(1024)
                .quality(ImagesResponse.Quality.MEDIUM.toString())
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(prompt,options);
        String image = imageModel.call(imagePrompt).getResult().getOutput().getB64Json();
        Path path = Paths.get("C:\\Users\\saura\\OneDrive\\Desktop\\imgAnalyzer\\").resolve(name);
        Files.write(path, Base64.getDecoder().decode(image), StandardOpenOption.CREATE);
    }

    public String classify(String topic, String sessionId) throws IOException {
        String fileName = "";
        switch (topic) {
            case "Product":
                fileName = "ProductLaunch.txt";
                break;
            case "Movie":
                fileName = "MovieReview.txt";
                break;
            case "Politics":
                fileName = "PoliticalEvent.txt";
                break;
            case "Tech":
                fileName = "TechConference.txt";
                break;
            case "Shopping":
                fileName = "HolidayShopping.txt";
                break;
        }


        String prompt = Files.readString(Path.of(new ClassPathResource(fileName).getURI()));
        prompt = "Please classify the below statements: "+prompt;
        return chatClient.prompt(prompt)
                .advisors(x -> x.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call().content();
    }

    public float[] getEmbedding(String text){
        EmbeddingOptions options = EmbeddingOptions.builder().model("sauravazembedding").build();
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(text),options);
        return embeddingModel.call(request).getResults().get(0).getOutput();
    }

    public double findSimilarity(String text) {
        EmbeddingOptions options = EmbeddingOptions.builder().model("sauravazembedding").build();
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(text.split(",")),options);
        float [] f1 = embeddingModel.call(request).getResults().get(0).getOutput();
        float [] f2 = embeddingModel.call(request).getResults().get(1).getOutput();
        return CosSimilarity(f1,f2);
    }

    public double CosSimilarity(float[] f1, float[] f2) {
        double dot=0,mod1=0,mod2=0;

        for(int i=0;i<f1.length;i++) {
            dot += f1[i]*f2[i];
            mod1 += f1[i]*f1[i];
            mod2 += f2[i]*f2[i];
        }

        return dot/(Math.sqrt(mod1)*Math.sqrt(mod2));
    }

    public double findPlagiarism(String docName) throws IOException {
        String doc = Files.readString(Paths.get(new ClassPathResource(docName+".txt").getURI()));
        String plagiarism = Files.readString(Paths.get(new ClassPathResource("PlagiarismText.txt").getURI()));
        EmbeddingOptions options = EmbeddingOptions.builder().model("sauravazembedding").build();
        EmbeddingRequest request = new EmbeddingRequest(Arrays.asList(doc,plagiarism),options);
        List<Embedding> list = embeddingModel.call(request).getResults();
        System.out.println(list.size());
        float[] f1 = list.get(0).getOutput();
        float[] f2 = list.get(1).getOutput();
        return CosSimilarity(f1,f2)*100;
    }

    public String convertToText(MultipartFile file) throws IOException {
        Path path = Paths.get("C:\\Users\\saura\\OneDrive\\Desktop\\imgAnalyzer\\").resolve(file.getOriginalFilename());
        Files.write(path, file.getBytes(), StandardOpenOption.CREATE);
        AudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .azure(true)
                .model("sauravazstt")
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(new FileSystemResource(path.toString()),options);
        return transcriptionModel.call(prompt).getResult().getOutput();
    }

    public String translate(MultipartFile file) throws IOException {
        Path path = Paths.get("C:\\Users\\saura\\OneDrive\\Desktop\\imgAnalyzer\\").resolve(file.getOriginalFilename());
        Files.write(path, file.getBytes(), StandardOpenOption.CREATE);
        AudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .azure(true)
                .model("sauravazstt")
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(new FileSystemResource(path.toString()),options);
        String actualText = transcriptionModel.call(prompt).getResult().getOutput();
        return chatClient.prompt("Translate the below text to English: "+actualText)
                .advisors(x->x.param(ChatMemory.CONVERSATION_ID, UUID.randomUUID()))
                .call().chatResponse().getResult().getOutput().getText();
    }

    public String summerize(MultipartFile file) throws IOException {
        Path path = Paths.get("C:\\Users\\saura\\OneDrive\\Desktop\\imgAnalyzer\\").resolve(file.getOriginalFilename());
        Files.write(path, file.getBytes(), StandardOpenOption.CREATE);
        AudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .azure(true)
                .model("sauravazstt")
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(new FileSystemResource(path.toString()),options);
        String actualText = transcriptionModel.call(prompt).getResult().getOutput();
        return chatClient.prompt("Summerize into 3 bullet points: "+actualText)
                .advisors(x->x.param(ChatMemory.CONVERSATION_ID, UUID.randomUUID()))
                .call().chatResponse().getResult().getOutput().getText();
    }

    public String generateQuiz(MultipartFile file) throws IOException {
        Path path = Paths.get("C:\\Users\\saura\\OneDrive\\Desktop\\imgAnalyzer\\").resolve(file.getOriginalFilename());
        Files.write(path, file.getBytes(), StandardOpenOption.CREATE);
        AudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .azure(true)
                .model("sauravazstt")
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(new FileSystemResource(path.toString()),options);
        String actualText = transcriptionModel.call(prompt).getResult().getOutput();
        return chatClient.prompt("Generate Quiz with MCQ and answers"+actualText)
                .advisors(x->x.param(ChatMemory.CONVERSATION_ID, UUID.randomUUID()))
                .call().chatResponse().getResult().getOutput().getText();
    }

    public String retrieveFromVector(String query) {
        return chatClient.prompt(query)
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .advisors(x->x.param(ChatMemory.CONVERSATION_ID,UUID.randomUUID()))
                .call().content();
    }

    public String fetchTunedAnswer(String query, String id) {
        return chatClient.prompt(query).advisors(x->x.param(ChatMemory.CONVERSATION_ID,id))
                .options(OpenAiChatOptions.builder().azure(true).model("sauravchatFT"))
                .system("Clippy, your factual chatbot with a sarcastic edge.")
                .system("Clippy, the chatbot combining facts with a pinch of sarcasm.")
                .call().content();
    }

    public Flux<String> fetchStreamAnswer(String query, String id) {
        return chatClient.prompt(query)
                .advisors(x->x.param(ChatMemory.CONVERSATION_ID,id))
                .stream().content();
    }

    public String functionCall(String id,String brand) {
        return chatClient.prompt("Find the stock price of "+brand)
                .advisors(x->x.param(ChatMemory.CONVERSATION_ID,id))
                .tools(new StockTool())
                .call().content();
    }
}
