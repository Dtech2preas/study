import com.google.gson.Gson
data class Question(
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)
data class Quiz(
    val title: String,
    val questions: List<Question>
)
fun main() {
    val json = """
    {
      "title": "A short title for the quiz",
      "questions": [
        {
          "questionText": "The question string",
          "options": ["Option 1", "Option 2", "Option 3", "Option 4"],
          "correctOptionIndex": 0,
          "explanation": "Why this is correct"
        }
      ]
    }
    """
    val quiz = Gson().fromJson(json, Quiz::class.java)
    println(quiz)
}
