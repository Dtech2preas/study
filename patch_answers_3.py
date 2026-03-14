with open("app/src/main/java/com/example/studyapp/ui/study/AnswersScreen.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if line.startswith("import kotlinx.coroutines.Dispatchers"):
        lines.insert(i, "import kotlinx.coroutines.tasks.await\n")
        break

with open("app/src/main/java/com/example/studyapp/ui/study/AnswersScreen.kt", "w") as f:
    f.writelines(lines)
