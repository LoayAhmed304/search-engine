#!/bin/bash
case "$1" in
  "crawl")
    mvn spring-boot:run -Dspring-boot.run.profiles=crawler
    ;;
  "pagerank")
    mvn spring-boot:run -Dspring-boot.run.profiles=pagerank
    ;;
  "rank")
    mvn spring-boot:run -Dspring-boot.run.profiles=ranker
    ;;
  "index")
    mvn spring-boot:run -Dspring-boot.run.profiles=indexer
    ;;
  *)
    echo "Usage: $0 [run crawler|run ranker|run indexer]"
    ;;
esac