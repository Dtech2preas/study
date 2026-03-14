with open("app/src/main/java/com/example/studyapp/ui/study/AnswersScreen.kt", "r") as f:
    lines = f.readlines()

if lines[0].startswith("import kotlinx.coroutines.tasks.await"):
    lines.pop(0)

for i, line in enumerate(lines):
    if line.startswith("import android.content.Context"):
        lines.insert(i, "import kotlinx.coroutines.tasks.await\n")
        break

with open("app/src/main/java/com/example/studyapp/ui/study/AnswersScreen.kt", "w") as f:
    f.writelines(lines)
