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
    # Run without arguments if no query is provided
    if [ -n "$2" ]; then
      QUERY="$2"
      mvn spring-boot:run -Dspring-boot.run.profiles=ranker -Dspring-boot.run.arguments="\"${QUERY}\""
    else
      mvn spring-boot:run -Dspring-boot.run.profiles=ranker
    fi
    ;;
  "index")
    mvn spring-boot:run -Dspring-boot.run.profiles=indexer
    ;;
  "query")
    mvn spring-boot:run -Dspring-boot.run.profiles=query
    ;;
  *)
    echo "Usage: $0 [crawl [thread_count] | rank [query_string] | index]"
    echo "Example: $0 crawl 20"
    ;;
esac