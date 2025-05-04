<template>
  <TheNavBar></TheNavBar>
  <div class="results-wrapper">
    <BaseSpinner v-if="isLoading" text="Exploring results..." />
    <div v-else-if="isChangingPage" class="changing-page">
      <BaseSpinner size="small" text="Loading page..." />
    </div>
    <div v-else class="results">
      <div v-if="fetchTime !== null" class="fetch-time-counter">
        Exploring took: {{ fetchTime.toFixed(2) }} seconds
      </div>
      
      <SearchResult
        v-for="(result, index) in results"
        :key="`${currentPage}-${index}`" 
        :title="result.title"
        :url="result.url"
        :snippet="result.snippet"
      />
      <p v-if="results.length === 0" class="no-results">
        No results found for "{{ route.query.q }}"
      </p>
    </div>
  </div>

  <div class="pagination" v-if="!isLoading && !isChangingPage && totalPages > 1">
    <font-awesome-icon
      icon="fa-solid fa-chevron-left"
      v-if="currentPage !== 0"
      class="pagination__arrow pagination__arrow--left"
      @click="prevPage"
    />
    <span
      v-for="page in totalPages"
      :key="page"
      class="pagination__page"
      :class="{ 'pagination__page--active': page - 1 === currentPage }"
      @click="setPage(page - 1)"
    >
      {{ page }}</span
    >
    <font-awesome-icon
      icon="fa-solid fa-chevron-right"
      v-if="currentPage < totalPages - 1"
      class="pagination__arrow pagination__arrow--right"
      @click="nextPage"
    />
    <button 
      v-show="showBackToTop"
      class="back-to-top" 
      @click="scrollToTop"
    >
      <font-awesome-icon icon="fa-solid fa-arrow-up" class="back-to-top__icon" />
      <p>New Adventure?</p>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'

import SearchBar from '@/components/ui/SearchBar.vue'
import SearchResult from '@/components/ui/SearchResult.vue'
import TheNavBar from '@/components/layout/TheNavBar.vue'
import BaseSpinner from '@/components/ui/BaseSpinner.vue'
import { search } from '@/services/searchServices.js'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const route = useRoute()
const currentPage = ref(0)
const maxResultsPerPage = 20
const totalPagesNumber = ref(0)
const results = ref([])
const isLoading = ref(true)
const fetchTime = ref(null)
const fetchStartTime = ref(null)

const showBackToTop = ref(false)

const scrollToTop = () => {
  window.scrollTo({top: 0, behavior: 'smooth'});
}

// Cache object to store results for each page
const resultsCache = ref({})

// Track if we're changing pages
const isChangingPage = ref(false)

const nextPage = () => {
  if (currentPage.value < totalPagesNumber.value - 1) {
    currentPage.value++;
    console.log("Next page clicked, now on page:", currentPage.value);
    loadPageResults(currentPage.value);
  }
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--;
    console.log("Prev page clicked, now on page:", currentPage.value);
    loadPageResults(currentPage.value);
  }
}

const loadPageResults = (pageNumber) => {
  const searchQuery = route.query.q || '';
  const cacheKey = `${searchQuery}_${pageNumber}`;
  
  isChangingPage.value = true;
  
  if (resultsCache.value[cacheKey]) {
    console.log('Using cached results for page', pageNumber);
    setTimeout(() => {  // show loading state even for cached results
      results.value = resultsCache.value[cacheKey];
      isChangingPage.value = false;
      fetchTime.value = 100/1000
    }, 100);
    return;
  }
  
  fetchStartTime.value = performance.now();
  search(searchQuery, pageNumber)
    .then((data) => {
      console.log('hi Fetched results for page', pageNumber, data);
      
      if (data && data.results) {
        results.value = data.results;
        
        if (data.totalPages) {
          totalPagesNumber.value = Math.ceil(data.totalPages / maxResultsPerPage);
        }
        
        // Cache the results
        resultsCache.value[cacheKey] = data.results;
      } else {
        console.error('Unexpected response format:', data);
      }
      
      
        const endTime = performance.now()
        fetchTime.value = (endTime - fetchStartTime.value) / 1000
    })
    .catch((error) => {
      console.error('Error fetching search results:', error)
    })
    .finally(() => {
      isChangingPage.value = false;
    });
}

const performSearch = (searchQuery) => {
  isLoading.value = true
  currentPage.value = 0 // Reset to first page
  fetchStartTime.value = performance.now()
  resultsCache.value = {} // Clear cache when performing new search
  
  NProgress.start() // Start progress bar
  
  search(searchQuery, 0)
    .then((data) => {
      console.log(data)
      results.value = data.results;
      totalPagesNumber.value = Math.ceil(data.totalPages / maxResultsPerPage);
      
      // Cache the results for page 0
      const cacheKey = `${searchQuery}_0`;
      resultsCache.value[cacheKey] = data.results;
      
      // Calculate fetch time in seconds
      const endTime = performance.now()
      fetchTime.value = (endTime - fetchStartTime.value) / 1000
    })
    .catch((error) => {
      console.error('Error fetching search results:', error)
    })
    .finally(() => {
      isLoading.value = false
      NProgress.done() // Complete progress bar
    })
}

watch(() => route.query.q, (newQuery) => {
  if (newQuery !== undefined) {
    performSearch(newQuery || '')
  }
})

onMounted(() => {
  const searchQuery = route.query.q || ''
  performSearch(searchQuery)
  
  // Add keyboard navigation for pagination
  window.addEventListener('keydown', handleKeydown);
  
  // Add scroll listener for back-to-top button
  window.addEventListener('scroll', handleScroll);
})

// Don't forget to remove the listener when component unmounts
onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown);
  window.removeEventListener('scroll', handleScroll);
});

// Define the handler separately for clean removal
const handleKeydown = (e) => {
  if (e.altKey && e.key === 'ArrowRight') {
    nextPage();
  } else if (e.altKey && e.key === 'ArrowLeft') {
    prevPage();
  }
};

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300;
};

const setPage = (page) => {
  console.log("Setting page to:", page);
  if (page !== currentPage.value) {
    currentPage.value = page;
    console.log("Current page value after update:", currentPage.value);
    loadPageResults(page); 
  }
}

const totalPages = computed(() => totalPagesNumber.value || 1)
</script>

<style scoped>
.results-wrapper {
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  max-width: 100%;
  width: 100%;
  overflow-x: hidden;
}

.results {
  display: flex;
  flex-direction: column;
  max-width: 1000px;
  width: 100%;
  overflow-x: hidden;
  align-items: flex-start; 
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 2rem;
  padding-bottom: 1rem;

  flex-wrap: wrap;
  gap: 0.5rem;
}

.pagination__arrow {
  font-size: 1.2rem;
  cursor: pointer;
  transition: color 0.2s;
  color: var(--accent-color);
}

.pagination__arrow--left {
  margin-right: 0.5rem;
}

.pagination__arrow--right {
  margin-left: 0.5rem;
}

.pagination__page {
  width: 2rem;
  height: 2rem;
  display: grid;
  place-items: center;
  border-radius: 50%;
  cursor: pointer;
  transition:
    background 0.2s,
    color 0.2s;
}

.pagination__page:hover {
  background-color: var(--accent-color);
  color: #fff;
}

.pagination__page--active {
  font-weight: bold;
  color: var(--accent-color); 
}

.no-results {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-style: italic;
  align-self: center; 
  width: 100%; 
}

.fetch-time-counter {
  background-color: #f8f9fa;
  padding: 8px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  text-align: left;
  color: #555;
  font-size: 0.9rem;
  border-left: 3px solid var(--accent-color);
  align-self: flex-start; 
  width: 100%; 
}

.changing-page {
  display: flex;
  justify-content: center;
  width: 100%;
  padding: 2rem 0;
}

.back-to-top {
  position: fixed;
  bottom: 70px;
  right: 100px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background-color: var(--accent-color);
  color: white;
  border: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  display: flex;
  flex-direction: column; 
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 100;
  opacity: 0.8;
  padding: 10px; 
}

.back-to-top:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  opacity: 1;
}

.back-to-top__icon {
  font-size: 1.8rem; 
  margin-bottom: 3px;
}

.back-to-top p {
  margin: 5px 0 0 0; 
  font-size: 0.8rem;
  text-align: center; 
  line-height: 1; 
  margin-bottom: 3px;
}
</style>
