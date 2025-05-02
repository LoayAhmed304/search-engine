<template>
  <TheNavBar></TheNavBar>
  <div class="results-wrapper">
    <div class="results">
      <SearchResult
        v-for="(result, index) in paginatedResults"
        :key="index"
        :title="result.title"
        :url="result.url"
        :snippet="result.snippet"
      />
    </div>
  </div>

  <div class="pagination">
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
      {{ page}}</span
    >
    <font-awesome-icon
      icon="fa-solid fa-chevron-right"
      v-if="(currentPage + 1) * maxResultsPerPage < mockResults.length"
      class="pagination__arrow pagination__arrow--right"
      @click="nextPage"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'

import SearchBar from '@/components/ui/SearchBar.vue'
import SearchResult from '@/components/ui/SearchResult.vue'
import TheNavBar from '@/components/layout/TheNavBar.vue'
import { search } from '@/services/searchServices.js'

const route = useRoute()
const currentPage = ref(0)
const maxResultsPerPage = 20
const results = ref([])

const nextPage = () => currentPage.value++;
const prevPage = () => currentPage.value--;

const mockResults = ref([])

onMounted(() => {
  // Get search query from URL
  const searchQuery = route.query.q || ''
  
  // Fetch results from the API using the actual query
  search(searchQuery, 0)
    .then((data) => {
      console.log(data)
      results.value = data
      mockResults.value = [...data]
      console.log('mockResults', mockResults.value)
    })
    .catch((error) => {
      console.error('Error fetching search results:', error)
    })
})

const paginatedResults = computed(() =>
  mockResults.value.slice(
    currentPage.value * maxResultsPerPage,
    (currentPage.value + 1) * maxResultsPerPage,
  ),
)

const totalPages = computed(() => Math.ceil(mockResults.value.length / maxResultsPerPage))

const setPage = (page) => {
  currentPage.value = page
}
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
  overflow-x: hidden;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 2rem;

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

.pagination__page--active {
  font-weight: bold;
  color: var(--accent-color); 
}
</style>
