import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

deps_to_add = """
    // Google ML Kit for Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Accompanist for Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Coil for Image Loading (Compose)
    implementation("io.coil-kt:coil-compose:2.5.0")
"""

content = re.sub(r'dependencies\s*\{', 'dependencies {\n' + deps_to_add, content)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
