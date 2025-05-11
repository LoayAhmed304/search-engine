## Ranker
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