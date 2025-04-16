// package com.project.searchengine;

// import com.project.searchengine.server.model.UrlDocument;
// import com.project.searchengine.server.repository.UrlsFrontierRepository;
// import com.project.searchengine.server.service.UrlsFrontierService;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.junit.jupiter.SpringExtension;

// import java.text.SimpleDateFormat;
// import java.util.*;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// @SpringBootTest
// @ExtendWith({ SpringExtension.class, MockitoExtension.class })
// public class UrlsFrontierServiceTest {

//     @Mock
//     private UrlsFrontierRepository urlsFrontierRepository;

//     @InjectMocks
//     private UrlsFrontierService urlsFrontierService;

//     @Autowired
//     private UrlsFrontierRepository realRepository;

//     @Autowired
//     private UrlsFrontierService realService;

//     @BeforeEach
//     void setUp() throws Exception {
//         // Ensure the database has the exact three documents
//         realRepository.deleteAll();

//         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

//         // Document 1
//         UrlDocument doc1 = new UrlDocument();
//         doc1.setId("67fe8355eae40c0b3d4e0bfe");
//         doc1.setNormalizedUrl("https://example.com/new-page");
//         doc1.setFrequency(1L);
//         doc1.setCrawled(false);
//         doc1.setDocument("raw html content");
//         doc1.setHashedDocContent("sha256hash123");
//         doc1.setLinkedPages(Arrays.asList("https://example.com/1", "https://example.com/2"));
//         doc1.setLastCrawled(sdf.parse("2024-04-20T12:00:00.000+00:00"));

//         // Document 2
//         UrlDocument doc2 = new UrlDocument();
//         doc2.setId("67fe834beae40c0b3d4e0c06");
//         doc2.setNormalizedUrl("https://example.com/1");
//         doc2.setFrequency(1L);
//         doc2.setCrawled(false);
//         doc2.setDocument("raw html content");
//         doc2.setHashedDocContent("");
//         doc2.setLinkedPages(Arrays.asList());
//         doc2.setLastCrawled(sdf.parse("2024-01-01T00:00:00.000+00:00")); // Adjusted for "2024"

//         // Document 3
//         UrlDocument doc3 = new UrlDocument();
//         doc3.setId("67fe834beae40c0b3d4e0c08");
//         doc3.setNormalizedUrl("https://example.com/2");
//         doc3.setFrequency(2L);
//         doc3.setCrawled(true);
//         doc3.setDocument("raw html content");
//         doc3.setHashedDocContent("");
//         doc3.setLinkedPages(Arrays.asList());
//         doc3.setLastCrawled(sdf.parse("2024-01-01T00:00:00.000+00:00")); // Assumed

//         realRepository.saveAll(Arrays.asList(doc1, doc2, doc3));
//     }

//     // Unit Tests (Mocking Repository)
//     @Test
//     void testGetTop100UrlsByFrequency_Mock() {
//         UrlDocument doc1 = new UrlDocument();
//         doc1.setNormalizedUrl("https://example.com/2");
//         doc1.setFrequency(2L);
//         UrlDocument doc2 = new UrlDocument();
//         doc2.setNormalizedUrl("https://example.com/new-page");
//         doc2.setFrequency(1L);
//         UrlDocument doc3 = new UrlDocument();
//         doc3.setNormalizedUrl("https://example.com/1");
//         doc3.setFrequency(1L);
//         List<UrlDocument> mockDocs = Arrays.asList(doc1, doc2, doc3);

//         when(urlsFrontierRepository.findTop100ByFrequency()).thenReturn(mockDocs);

//         List<UrlDocument> result = urlsFrontierService.getTop100UrlsByFrequency();

//         assertEquals(3, result.size());
//         assertEquals("https://example.com/2", result.get(0).getNormalizedUrl());
//         assertEquals(2L, result.get(0).getFrequency());
//         verify(urlsFrontierRepository, times(1)).findTop100ByFrequency();
//     }

//     @Test
//     void testIncrementFrequency_Mock() {
//         doNothing().when(urlsFrontierRepository).incrementFrequency(anyString());

//         urlsFrontierService.incrementFrequency("https://example.com/new-page");

//         verify(urlsFrontierRepository, times(1)).incrementFrequency("https://example.com/new-page");
//     }

//     @Test
//     void testExistsByNormalizedUrl_Mock_Exists() {
//         when(urlsFrontierRepository.existsByNormalizedUrl("https://example.com/new-page")).thenReturn(true);

//         boolean exists = urlsFrontierService.existsByNormalizedUrl("https://example.com/new-page");

//         assertTrue(exists);
//         verify(urlsFrontierRepository, times(1)).existsByNormalizedUrl("https://example.com/new-page");
//     }

//     @Test
//     void testExistsByNormalizedUrl_Mock_NotExists() {
//         when(urlsFrontierRepository.existsByNormalizedUrl("https://example.com/non-existent")).thenReturn(false);

//         boolean exists = urlsFrontierService.existsByNormalizedUrl("https://example.com/non-existent");

//         assertFalse(exists);
//         verify(urlsFrontierRepository, times(1)).existsByNormalizedUrl("https://example.com/non-existent");
//     }

//     @Test
//     void testUpsertUrl_Mock() {
//         when(urlsFrontierRepository.upsertUrl("https://example.com/new-page")).thenReturn(true);

//         boolean result = urlsFrontierService.upsertUrl("https://example.com/new-page");

//         assertTrue(result);
//         verify(urlsFrontierRepository, times(1)).upsertUrl("https://example.com/new-page");
//     }

//     @Test
//     void testInitializeFrontier_Mock() {
//         when(urlsFrontierRepository.upsertUrl(anyString())).thenReturn(true);

//         List<String> seedUrls = Arrays.asList("https://example.com/new-page", "https://example.com/1");
//         urlsFrontierService.initializeFrontier(seedUrls);

//         verify(urlsFrontierRepository, times(2)).upsertUrl(anyString());
//         verify(urlsFrontierRepository, times(1)).upsertUrl("https://example.com/new-page");
//         verify(urlsFrontierRepository, times(1)).upsertUrl("https://example.com/1");
//     }

//     // Integration Tests (Real MongoDB)
//     @Test
//     void testGetTop100UrlsByFrequency_Integration() {
//         List<UrlDocument> result = realService.getTop100UrlsByFrequency();

//         assertEquals(3, result.size());
//         assertEquals("https://example.com/2", result.get(0).getNormalizedUrl());
//         assertEquals(2L, result.get(0).getFrequency());
//         assertTrue(result.get(1).getFrequency() <= result.get(0).getFrequency());
//         assertTrue(result.get(2).getFrequency() <= result.get(1).getFrequency());
//     }

//     @Test
//     void testIncrementFrequency_Integration() {
//         realService.incrementFrequency("https://example.com/new-page");

//         UrlDocument updatedDoc = realRepository.findById("67fe8355eae40c0b3d4e0bfe").orElse(null);
//         assertNotNull(updatedDoc);
//         assertEquals(2L, updatedDoc.getFrequency());
//     }

//     @Test
//     void testExistsByNormalizedUrl_Integration_Exists() {
//         boolean exists = realService.existsByNormalizedUrl("https://example.com/new-page");

//         assertTrue(exists);
//     }

//     @Test
//     void testExistsByNormalizedUrl_Integration_NotExists() {
//         boolean exists = realService.existsByNormalizedUrl("https://example.com/non-existent");

//         assertFalse(exists);
//     }

//     @Test
//     void testUpsertUrl_Integration_ExistingUrl() {
//         // Store the original frequency
//         UrlDocument originalDoc = realRepository.findById("67fe8355eae40c0b3d4e0bfe").orElse(null);
//         assertNotNull(originalDoc);
//         long originalFrequency = originalDoc.getFrequency();

//         // Call upsertUrl without mocking
//         realService.upsertUrl("https://example.com/new-page");

//         // Verify the frequency was incremented
//         UrlDocument updatedDoc = realRepository.findById("67fe8355eae40c0b3d4e0bfe").orElse(null);
//         assertNotNull(updatedDoc);
//         assertEquals(originalFrequency + 1, updatedDoc.getFrequency());
//     }

//     @Test
//     void testUpsertUrl_Integration_NewUrl() {
//         // Call upsertUrl without mocking
//         realService.upsertUrl("https://example.com/new-url");

//         // Verify the new URL was added
//         assertTrue(realService.existsByNormalizedUrl("https://example.com/new-url"));
//         UrlDocument newDoc = realRepository.findAll().stream()
//                 .filter(doc -> doc.getNormalizedUrl().equals("https://example.com/new-url"))
//                 .findFirst()
//                 .orElse(null);
//         assertNotNull(newDoc);
//         assertEquals(1L, newDoc.getFrequency());
//     }

//     @Test
//     void testInitializeFrontier_Integration() throws Exception {
//         // Store the original frequency of an existing URL
//         UrlDocument originalDoc = realRepository.findById("67fe8355eae40c0b3d4e0bfe").orElse(null);
//         assertNotNull(originalDoc);
//         long originalFrequency = originalDoc.getFrequency();

//         List<String> seedUrls = Arrays.asList("https://example.com/new-page", "https://example.com/new-url");
//         realService.initializeFrontier(seedUrls);

//         // Verify the existing URL's frequency was incremented
//         UrlDocument updatedDoc = realRepository.findById("67fe8355eae40c0b3d4e0bfe").orElse(null);
//         assertNotNull(updatedDoc);
//         assertEquals(originalFrequency + 1, updatedDoc.getFrequency());

//         // Verify the new URL was added
//         List<UrlDocument> docs = realRepository.findAll();
//         assertEquals(4, docs.size()); // 3 original + 1 new
//         assertTrue(docs.stream().anyMatch(doc -> doc.getNormalizedUrl().equals("https://example.com/new-url")));
//     }
// }