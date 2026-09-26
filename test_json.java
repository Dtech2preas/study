import com.google.gson.Gson;
import java.util.List;

public class test_json {
    public static class Question {
        String questionText;
        List<String> options;
        int correctOptionIndex;
        String explanation;
    }
    public static class Quiz {
        String title;
        List<Question> questions;
    }
    public static void main(String[] args) {
        String json = "{\n" +
            "  \"title\": \"A short title for the quiz\",\n" +
            "  \"questions\": [\n" +
            "    {\n" +
            "      \"questionText\": \"The question string\",\n" +
            "      \"options\": [\"Option 1\", \"Option 2\", \"Option 3\", \"Option 4\"],\n" +
            "      \"correctOptionIndex\": 0,\n" +
            "      \"explanation\": \"Why this is correct\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        Gson gson = new Gson();
        Quiz quiz = gson.fromJson(json, Quiz.class);
        System.out.println(quiz.title);
    }
}
