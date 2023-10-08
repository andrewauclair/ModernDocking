cd docking-api

call gradlew publishToMavenLocal
call gradlew publish


cd ..\docking-ui

call gradlew publishToMavenLocal
call gradlew publish


cd ..\docking-single-app

call gradlew publishToMavenLocal
call gradlew publish


cd ..\docking-multi-app

call gradlew publishToMavenLocal
call gradlew publish

cd ..
