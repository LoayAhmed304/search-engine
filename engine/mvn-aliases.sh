#!/bin/bash
case "$1" in
  "crawl")
    # Default to 20 threads if no thread count is provided
    THREAD_COUNT=${2:-20}
    mvn spring-boot:run -Dspring-boot.run.profiles=crawler -Dspring-boot.run.arguments="--threads=${THREAD_COUNT}"
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
  "query")
    mvn spring-boot:run -Dspring-boot.run.profiles=query
    ;;
  *)
    echo "Usage: $0 [crawl [thread_count]|rank|index]"
    echo "Example: $0 crawl 20"
    ;;
esac