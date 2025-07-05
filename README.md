## $\color{rgba(240, 171, )}{\textsf{Hola! Can you say "The best search engine ever" with me? }}$ 


<div align="center">
<img src="https://media.tenor.com/TdUkba4lsP0AAAAM/dora.gif" width="300" height="300" />
</div>

# 🔍 Search Engine Project

> A full-stack search engine built with Java/Spring Boot backend and Vue.js frontend

##  Overview

This is a high-performance search engine that crawls web pages, indexes content, calculates PageRank scores, and provides a modern web interface for searching. The system is designed with scalability and performance in mind, featuring multi-threaded crawling, efficient indexing, and intelligent ranking algorithms.

---
## ✨ Features
### 🕷️ Web Crawler

- **Multi-threaded Architecture**: Configurable thread pool (default: 20 threads)
- **High Performance**: Crawls 1000 documents in under 1 minute using 5 threads
- **Smart Batching**: Prioritizes popular pages using frequency-based batching
- **Robots.txt Compliance**: Respects web server policies with robust caching
- **Duplicate Detection**: Content hashing prevents redundant processing
- **URL Normalization**: Standardizes and filters invalid URLs
- **Compression**: Stores crawled content efficiently

##### Optimization Techniques
- Uses documents compression and decompression to store data of much less size in the database for faster operations.
- The RobotsHandler implements a domain-based caching system, maps hostnames to parsed robots.txt rules, ensuring each domain's rules are fetched only once regardless of how many URLs from that domain are crawled.

### 🔍 Indexer

- **Advanced Tokenization**: Intelligent text processing and cleanup
- **Stop Word Filtering**: Removes common words for better relevance
- **Stemming Support**: Reduces words to their root forms
- **Field Extraction**: Processes titles, headers, and content separately
- **Efficient Storage**: Optimized database operations

### 📊 Ranking System

> Ranks pages based on their PageRank, TF, and IDF scores

- **TF-IDF** scoring for term relevance per page
- **Normalized PageRank** influence for domain authority
    - Using PageRank algorithm, which takes ~10ms on 6,000 documents
- **Structural field boosts** for `<title>` and `<h1>` tag matches
- **Penalty** applied for missing `<h1>` tags
- **Score capping** to avoid overinflation
- Computes PageRank as an **offline process** for all crawled URLs
- **Optimized database operations** for fetching & saving ranks,
    - targets: <200ms for 6,000 documents.
    - Ranking logic runs in **8~50ms** depending on query  
---
### Development Guidelines
- Follow the conventions outlined in `Project-Guidelines.md`

---
## Getting started
// add how to run etc