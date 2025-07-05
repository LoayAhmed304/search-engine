# Usage: make <target> [THREADS=<count>] [QUERY="<query string>"]

.PHONY: crawl pagerank rank index help clean test

# Default target
.DEFAULT_GOAL := test

# Variables
THREADS ?= 20
QUERY ?= ""

# Maven wrapper command
MVN = ./mvnw

# Crawl with configurable thread count
crawl:
	@echo "Starting crawler with $(THREADS) threads..."
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=crawler -Dspring-boot.run.arguments="--threads=$(THREADS)"

# Run PageRank calculation
pagerank:
	@echo "Starting PageRank calculation..."
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=pagerank

# Run ranking (with optional query)
rank:
	@echo "Starting ranker..."
ifeq ($(QUERY),"")
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=ranker
else
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=ranker -Dspring-boot.run.arguments="\"$(QUERY)\""
endif

# Run indexer
index:
	@echo "Starting indexer..."
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=indexer

# Clean Maven artifacts
clean:
	@echo "Cleaning Maven artifacts..."
	$(MVN) clean

# Run tests
test:
	@echo "Running tests..."
	$(MVN) test

# Compile only
compile:
	@echo "Compiling..."
	$(MVN) compile

# Package the application
package:
	@echo "Packaging application..."
	$(MVN) package
