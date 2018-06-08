# Apache Top
![apache-top](https://user-images.githubusercontent.com/6814254/41112936-a3837354-6a80-11e8-92c9-4803eba1c293.gif)

## Requirements
- [Scala 2.12.6](https://www.scala-lang.org/download/)
- [SBT](https://www.scala-sbt.org/download.html)
- [Docker](https://www.docker.com/get-docker) (optional)

## Usage
```bash
sbt compile
sbt "run apache.log"
```
### Using log-generator
```bash
# Run and test with log-generator
docker-compose -f ./log-generator/docker-compose.yml up -d; sbt "run log-generator/access.log"
# Stop log-generator
docker-compose -f ./log-generator/docker-compose.yml down
```

## Deploy
```bash
sbt assembly
scala target/scala-2.12/apache-top-assembly-1.0.jar apache.log
```

## Unit Testing
```bash
sbt test
sbt "testOnly *ApacheTopPrinterSpec"
sbt "testOnly *ApacheTopParserSpec"
sbt "testOnly *ApacheTopParserSpec -- -z format"
```