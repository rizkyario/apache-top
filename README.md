# Apache Top
![apache-top](https://user-images.githubusercontent.com/6814254/41112936-a3837354-6a80-11e8-92c9-4803eba1c293.gif)

## Required Tools
- [Scala 2.12.6](https://www.scala-lang.org/download/)
- [SBT](https://www.scala-sbt.org/download.html)

## Usage
```
sbt compile
sbt "run apache.log"
```

## Unit Testing
```
sbt test
sbt "testOnly *ApacheTopParserSpec -- -z format"
```