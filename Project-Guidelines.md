
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

> [!TIP] 
> Add a README.md inside the folder of every module(package)
> that describes its functionality.

---
# Appendix
## Writing Javadocs
### **How to Write Javadoc for Your Functions**

### **1. Basic Javadoc Structure**
Place comments **immediately before** the method/class declaration, starting with `/**` and ending with `*/`.

#### **Example: Simple Method**
```java
/**
 * Calculates the sum of two integers.
 *
 * @param a The first integer.
 * @param b The second integer.
 * @return The sum of `a` and `b`.
 */
public int add(int a, int b) {
    return a + b;
}
```

---

### **2. Key Javadoc Tags**
| Tag           | Purpose                                             | Example                                             |
| ------------- | --------------------------------------------------- | --------------------------------------------------- |
| `@param`      | Describes a method parameter.                       | `@param url The webpage URL to fetch.`              |
| `@return`     | Describes the return value.                         | `@return The HTML content as a String.`             |
| `@throws`     | Documents exceptions the method might throw.        | `@throws IOException If the URL cannot be fetched.` |
| `@see`        | Links to related classes/methods.                   | `@see java.net.URL`                                 |
| `@deprecated` | Marks a method as obsolete (with migration advice). | `@deprecated Use {@link #newMethod()} instead.`     |
| `{@code}`     | Formats text as code (monospace).                   | `{@code int x = 5;}`                                |
| `{@link}`     | Creates a clickable link to another class/method.   | `{@link #normalizeUrl(String)}`                     |

---

### **3. Javadoc for a Real-World Example**
#### **Function: Fetching HTML with Jsoup**
```java
/**
 * Fetches the HTML content of a webpage and returns it as a Jsoup {@link Document}.
 * Automatically follows redirects and handles timeouts.
 *
 * @param url The absolute URL of the webpage (e.g., "https://example.com").
 * @param timeoutMs Maximum time to wait (in milliseconds) before aborting the request.
 * @return A parsed HTML {@link Document} object.
 * @throws IOException If the connection fails or the server returns an error (e.g., 404).
 * @throws IllegalArgumentException If `url` is malformed or not HTTP/HTTPS.
 * @see org.jsoup.Jsoup#connect(String)
 */
public static Document fetchHtmlDocument(String url, int timeoutMs) throws IOException {
    if (!url.startsWith("http")) {
        throw new IllegalArgumentException("URL must start with http/https");
    }
    return Jsoup.connect(url).timeout(timeoutMs).get();
}
```

---

### **4. Class-Level Javadoc**
Document the **purpose** of the class and how to use it:
```java
/**
 * A web crawler that downloads and indexes HTML pages from specified URLs.
 * Uses Jsoup for HTML parsing and MongoDB for storage.
 *
 * <p>Example usage:
 * {@code
 * WebCrawler crawler = new WebCrawler();
 * crawler.crawl("https://example.com");
 * }
 *
 * @author Your Name
 * @version 1.0
 */
public class WebCrawler {
    // ...
}
```

---

### **5. Best Practices**
1. **Be concise but descriptive**:  
   - Bad: `/** Adds two numbers. */`  
   - Good: `/** Returns the sum of two integers, handling overflow. */`

2. **Document all parameters and exceptions**:  
   Even "obvious" ones like `@param url` should clarify valid formats (e.g., "Must be absolute URL").

3. **Use `{@code}` for examples**:  
   ```java
   /**
    * Sets the timeout.
    * Example: {@code setTimeout(5000); // 5 seconds}
    */
   ```

4. **Avoid HTML tags**:  
   Use Javadoc’s built-in formatting (e.g., `<p>` for paragraphs is okay, but avoid `<br>`).

5. **Generate Javadoc**:  
   Run Maven to generate HTML documentation:  
   ```bash
   mvn javadoc:javadoc
   ```
   Outputs to `target/site/apidocs/`.

---

### **6. Example: Full Javadoc for a Crawler Method**
```java
/**
 * Normalizes a URL by converting it to lowercase, removing fragments (#), and resolving relative paths.
 * Example: "https://Example.com/path#section" → "https://example.com/path"
 *
 * @param url The URL to normalize (e.g., from {@link #fetchHtmlDocument(String, int)}).
 * @return The normalized URL, or `null` if the input is invalid.
 * @throws MalformedURLException If the URL protocol is unsupported (e.g., "ftp://").
 * @see java.net.URL
 */
public String normalizeUrl(String url) throws MalformedURLException {
    // Implementation...
}
```

---
