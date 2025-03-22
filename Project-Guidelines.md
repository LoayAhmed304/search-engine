
# $\color{rgba(240, 171, 27)}{\textsf{Suggested Project Guidelines}}$ 


This document outlines the conventions and best practices for structuring and organizing our Java project. Please follow these guidelines to maintain consistency and readability across the codebase.

---

## **1. Package Naming Conventions**
Packages should follow the reverse domain name convention.

### **Rules**:
- Use **lowercase** letters only.
- Avoid using special characters or underscores (`_`).
- Group related classes into meaningful packages.
- Use the following structure:
  ```
  com.<organization>.<project>.<module>.<submodule>
  ```
  Example:
  ```
  com.cmp.searchengine.crawler
  com.cmp.searchengine.indexer
  com.cmp.searchengine.ranker
  ```

### **Examples**:
- **Crawler Module**:
  ```
  com.cmp.searchengine.crawler
  com.cmp.searchengine.crawler.parser
  com.cmp.searchengine.crawler.downloader
  ```
- **Indexer Module**:
  ```
  com.cmp.searchengine.indexer
  com.cmp.searchengine.indexer.storage
  com.cmp.searchengine.indexer.tokenizer
  ```

---

## **2. Folder Structure**
Follow the standard Maven directory structure. Add additional folders as needed for resources, tests, and configurations.

### **Standard Structure**:
```
src/
├── main/
│   ├── java/              # Java source files
│   └── resources/         # Configuration files, properties, etc.
└── test/
    ├── java/              # Test source files
    └── resources/         # Test-specific resources
```

### **Additional Folders**:
- **`config/`**: For configuration files (e.g., `application.properties`).
- **`docs/`**: For project documentation (e.g., design documents, guidelines).
- **`logs/`**: For log files (if applicable).

---

## **3. Class Naming Conventions**
- Use **PascalCase** for class names.
- Class names should be **nouns** and describe the purpose of the class.
- Avoid abbreviations unless they are widely understood.

### **Examples**:
- `WebCrawler`
- `DocumentIndexer`
- `PageRankCalculator`

---

## **4. Method Naming Conventions**
- Use **camelCase** for method names.
- Method names should be **verbs** and describe the action performed by the method.
- Avoid overly long or vague names.

### **Examples**:
- `downloadPage()`
- `parseHtml()`
- `calculateRelevanceScore()`

---

## **5. Variable Naming Conventions**
- Use **camelCase** for variable names.
- Variable names should be descriptive and reflect their purpose.
- Avoid single-letter variable names (except for loop counters).

### **Examples**:
- `pageUrl`
- `documentContent`
- `isCrawlingEnabled`

---

## **6. File Naming Conventions**
- Use **lowercase** letters for file names.
- Separate words with hyphens (`-`) if needed.
- Use meaningful names that reflect the file's purpose.

### **Examples**:
- `application.properties`
- `log4j2.xml`
- `crawler-config.json`

---

## **7. Code Formatting**
I already added a config file for prettier that adgeres to [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

Just make sure your auto-save settings are on before your next push :)

---

## **8. Testing Conventions**
- Place test classes in the `src/test/java` directory.
- Test class names should match the class being tested, suffixed with `Test`.

### **Example**:
```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WebCrawlerTest {
    @Test
    public void testCrawl() {
        WebCrawler crawler = new WebCrawler("https://example.com");
        assertNotNull(crawler);
    }
}
```

---

## **9. Documentation**
- Add **Javadoc** comments to all public classes and methods.
- Include a `README.md` file in the root directory with:
  - Project overview
  - Setup instructions
  - Usage examples
- Update documentation as the project evolves.

### **Javadoc Example**:
```java
/**
 * A web crawler that downloads and parses web pages.
 */
public class WebCrawler {
    /**
     * Constructs a new WebCrawler with the given base URL.
     *
     * @param baseUrl the base URL to start crawling from
     */
    public WebCrawler(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
```

### Add a README.md inside the folder of every module(package)
that describes its functionality.

---
