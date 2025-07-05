## $\color{rgba(240, 171, )}{\textsf{Hola! Can you say "The best search engine ever" with me? }}$ 


<div align="center">
  <img src="https://media.tenor.com/TdUkba4lsP0AAAAM/dora.gif" width="300" height="300" />
</div>

# 🔍 Search Engine Project

> A full-stack search engine built with Java/Spring Boot backend and Vue.js frontend

## Project Overview

This is a high-performance search engine that crawls web pages, indexes content, calculates PageRank scores, and provides a modern web interface for searching. The system is designed with scalability and performance in mind, featuring multi-threaded crawling, efficient indexing, and intelligent ranking algorithms.


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

### 🧾 Indexer

> Transforms HTML documents into inverted indices for fast search

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


### 🔍 Query processing & Phrase Searching

> Processes user queries, supports phrase search, and generates result snippets.


- **Unified Tokenization**: Applies the same cleanup, stemming, and stop-word removal as the indexer to ensure consistency
- **Exact Phrase Search**: Supports precise phrase matching, even when stop words are present
- **Multi-threading**: Speeds up snippet generation and phrase matching
- **Fast Response Times**:
    - General query: **0.01 – 0.2 seconds**
    - Phrase search: **< 0.3 seconds**

#### Optimization Techniques

- Snippet generation is triggered only when the corresponding result page is requested
- Uses token position lookup from the inverted index — avoids full document scans (no regex!)
- Early filtering (before stemming) to narrow down the result set for phrase searching queries

#### Flow

<div> 
	<img src="https://github.com/user-attachments/assets/c93d6428-d2cd-442c-9861-3c01caa7c3c2" width="500" height="auto" />
</div>


## 📝 Development Guidelines
- Follow the conventions outlined in `Project-Guidelines.md`


## 🛠️ How to Run

### Prerequisites

- **MongoDB** must be running locally 
- **Maven** should be installed and available in your terminal
- Ensure your `application.properties` file is properly configured
- Clone the repository and navigate to the project root directory
### Backend Setup

1. Open a terminal and navigate to the backend directory:
    
    ```bash
    cd engine
    ```
    
2. Run Spring Boot
    
    ```bash
    mvn spring-boot:run
    ```
    
---

To start each module **in order**:


1. Run the Crawler (default thread count is 20)

```bash
make crawl THREADS=10
```

2. Run the Indexer

```bash
make index
```

3. Run the PageRank Module

```bash
make pagerank
```

4, Run the Ranker (with an optional query)

```bash
make rank QUERY="your search terms"
```

---

### Frontend Setup

1. Open a new terminal and go to the frontend directory:
    
    ```bash
    cd client
    ```
    
2. Install dependencies:
    
    ```bash
    npm install
    ```
    
3. Start the development server:
    
    ```bash
    npm run dev
    ```
    
4. Open your browser and visit: [http://localhost:5173](http://localhost:5173/)


## 👥 Contributors 

<div align="center">
    <a href="https://git.io/typing-svg"><img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=16&pause=1000&color=D96C92&width=500&lines=Can+you+say+%22the+best+search+engine+ever%22%3F" alt="Typing SVG" /></a>
</div>

<table align="center">
<tr>
  <td align = "center"> 
	<a href = "https://github.com/habibayman">
	  <img src = "https://github.com/habibayman.png" width = 100>
	  <br />
	  <sub> Habiba Ayman </sub>
	</a>
  </td>
  <td align = "center"> 
	<a href = "https://github.com/Tasneemmohammed0">
	  <img src = "https://github.com/Tasneemmohammed0.png" width = 100>
	  <br />
	  <sub> Tasneem Mohamed </sub>
	</a>
  </td>
  <td align = "center"> 
	<a href = "https://github.com/LoayAhmed304">
	  <img src = "https://github.com/LoayAhmed304.png" width = 100>
	  <br />
	  <sub> Loay Ahmed </sub>
	</a>
  </td>

  <td align = "center"> 
	<a href = "https://github.com/HelanaNady">
	  <img src = "https://github.com/HelanaNady.png" width = 100>
	  <br />
	  <sub> Helana Nady</sub>
	</a>
  </td>
</tr>
</table>



