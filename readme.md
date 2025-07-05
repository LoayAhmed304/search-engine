# $\color{rgba(255, 192, 203, 1)}{\textsf{Crawler}}$ 

> Collects pages and stores them in the database to be later processed

### Core Features

>[!TIP]
> - Implemented to priotirize popular pages first by using batching while keeping track of a `frequency` count in every URL's document in the database.
> - Crawls 1000 documents using 5 threads in **less than 1 minute!!** (check optimization section :eyes: )

- **Multi-threaded Architecture**: Implements concurrent crawling using a configurable thread pool (default: 20 threads) to maximize throughput and efficiency
- **Frontier Management**: Maintains a priority queue of URLs to be crawled, organized in batches for efficient processing
- **Robots.txt Compliance**: Respects web server policies by parsing and adhering to robots.txt directives with a robust caching mechanism
- **Duplicate Detection**: Employs content hashing to identify and skip duplicate pages, preventing redundant processing
- **URL Normalization & Filtering**: Standardizes URLs and filters out invalid or unwanted schemes (JavaScript, mailto links, etc.)
- **Document Storage**: Compresses and persistently stores crawled content for later indexing

### Optimization
- Uses documents compression and decompression to store data of much less size in the database for faster operations.
- The RobotsHandler implements a domain-based caching system, maps hostnames to parsed robots.txt rules, ensuring each domain's rules are fetched only once regardless of how many URLs from that domain are crawled.
---
# $\color{rgba(255, 192, 203, 1)}{\textsf{Ranker}}$ 

> Ranks pages based on their PageRank, TF, and IDF scores
### Features
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
