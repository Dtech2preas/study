import re

with open("app/src/main/java/com/example/studyapp/ui/study/AnswersScreen.kt", "r") as f:
    content = f.read()

content = content.replace('import kotlinx.coroutines.tasks.await', '')
content = content.replace('result.text', 'result.text') # Just leaving this comment here for reference
content = content.replace('colors = ButtonDefaults.buttonColors(containerColor = DarkGray)', 'colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)')
content = content.replace('colors = CardDefaults.cardColors(containerColor = DarkGray)', 'colors = CardDefaults.cardColors(containerColor = Color.DarkGray)')
content = content.replace('RichTextView(\n                            text = answerText,\n                            modifier = Modifier.fillMaxSize()\n                        )', 'RichTextView(\n                            markdown = answerText,\n                            modifier = Modifier.fillMaxSize()\n                        )')
content = content.replace('val result = recognizer.process(image).await()', 'val result = kotlinx.coroutines.tasks.await(recognizer.process(image))')
content = "import kotlinx.coroutines.tasks.await\n" + content

with open("app/src/main/java/com/example/studyapp/ui/study/AnswersScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/studyapp/ai/OnlineAIManager.kt", "r") as f:
    content = f.read()

content = content.replace('RetrofitClient.api.getChatCompletion', 'api.getChatCompletion')

with open("app/src/main/java/com/example/studyapp/ai/OnlineAIManager.kt", "w") as f:
    f.write(content)
