with open("app/src/main/java/com/example/studyapp/ui/study/AnswersScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import kotlinx.coroutines.tasks.await\n", "")
content = content.replace("val result = kotlinx.coroutines.tasks.await(recognizer.process(image))", "val result = recognizer.process(image).await()")

with open("app/src/main/java/com/example/studyapp/ui/study/AnswersScreen.kt", "w") as f:
    f.write(content)
