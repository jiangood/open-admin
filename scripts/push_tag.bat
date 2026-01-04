cd ../
set  "tag_name=v0.1.0"

:: 测试
call mvnw clean test -DskipTests

git tag -d %tag_name%

git push origin --delete %tag_name%

git tag %tag_name%

git push origin %tag_name%


