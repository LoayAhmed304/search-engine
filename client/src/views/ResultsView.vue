<template>
  <TheNavBar></TheNavBar>
  <div class="results-wrapper">
    <div class="results">
      <SearchResult
        v-for="(result, index) in paginatedResults"
        :key="index"
        :title="result.title"
        :url="result.url"
        :content="result.content"
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

import SearchBar from '@/components/ui/SearchBar.vue'
import SearchResult from '@/components/ui/SearchResult.vue'
import TheNavBar from '@/components/layout/TheNavBar.vue'

const currentPage = ref(0)
const maxResultsPerPage = 7

const nextPage = () => currentPage.value++
const prevPage = () => currentPage.value--

// to be removed later 
const mockResults = [
  {
    title: 'Discovering Exoplanets',
    url: 'https://nasa.gov/exoplanets',
    content:
      'Explore distant worlds beyond our solar system and learn how scientists find exoplanets using telescopes and light curves.',
  },
  {
    title: 'What Makes a Planet Habitable?',
    url: 'https://space.org/habitability',
    content:
      'Understand the key elements like water, atmosphere, and temperature range that contribute to a planet’s ability to support life.',
  },
  {
    title: 'Telescope Technologies',
    url: 'https://jwst.nasa.gov/tech',
    content:
      'An in-depth look at how space telescopes like the James Webb Space Telescope are revolutionizing our view of the universe.',
  },
  {
    title: 'Wiki',
    url: '',
    content:
      'Pierre Boulez (26 March 1925 – 5 January 2016) was a French composer and conductor. He was one of the dominant figures of post-war contemporary classical music.As a composer, he played a leading role in the development of integral serialism in the 1950s, and the electronic transformation of instrumental music in real time from the 1970s. Boulez conducted many of...',
  },
  {
    title: 'Discovering Exoplanets',
    url: 'https://nasa.gov/exoplanets',
    content:
      'Explore distant worlds beyond our solar system and learn how scientists find exoplanets using telescopes and light curves.',
  },
  {
    title: 'What Makes a Planet Habitable?',
    url: 'https://space.org/habitability',
    content:
      'Understand the key elements like water, atmosphere, and temperature range that contribute to a planet’s ability to support life.',
  },
  {
    title: 'Telescope Technologies',
    url: 'https://jwst.nasa.gov/tech',
    content:
      'An in-depth look at how space telescopes like the James Webb Space Telescope are revolutionizing our view of the universe.',
  },
  {
    title: 'Wiki',
    url: '',
    content:
      'Pierre Boulez (26 March 1925 – 5 January 2016) was a French composer and conductor. He was one of the dominant figures of post-war contemporary classical music.As a composer, he played a leading role in the development of integral serialism in the 1950s, and the electronic transformation of instrumental music in real time from the 1970s. Boulez conducted many of...',
  },
  {
    title: 'Discovering Exoplanets',
    url: 'https://nasa.gov/exoplanets',
    content:
      'Explore distant worlds beyond our solar system and learn how scientists find exoplanets using telescopes and light curves.',
  },
  {
    title: 'What Makes a Planet Habitable?',
    url: 'https://space.org/habitability',
    content:
      'Understand the key elements like water, atmosphere, and temperature range that contribute to a planet’s ability to support life.',
  },
  {
    title: 'Telescope Technologies',
    url: 'https://jwst.nasa.gov/tech',
    content:
      'An in-depth look at how space telescopes like the James Webb Space Telescope are revolutionizing our view of the universe.',
  },
  {
    title: 'Wiki',
    url: '',
    content:
      'Pierre Boulez (26 March 1925 – 5 January 2016) was a French composer and conductor. He was one of the dominant figures of post-war contemporary classical music.As a composer, he played a leading role in the development of integral serialism in the 1950s, and the electronic transformation of instrumental music in real time from the 1970s. Boulez conducted many of...',
  },
  {
    title: 'Discovering Exoplanets',
    url: 'https://nasa.gov/exoplanets',
    content:
      'Explore distant worlds beyond our solar system and learn how scientists find exoplanets using telescopes and light curves.',
  },
  {
    title: 'What Makes a Planet Habitable?',
    url: 'https://space.org/habitability',
    content:
      'Understand the key elements like water, atmosphere, and temperature range that contribute to a planet’s ability to support life.',
  },
  {
    title: 'Telescope Technologies',
    url: 'https://jwst.nasa.gov/tech',
    content:
      'An in-depth look at how space telescopes like the James Webb Space Telescope are revolutionizing our view of the universe.',
  },
  {
    title: 'Wiki',
    url: '',
    content:
      'Pierre Boulez (26 March 1925 – 5 January 2016) was a French composer and conductor. He was one of the dominant figures of post-war contemporary classical music.As a composer, he played a leading role in the development of integral serialism in the 1950s, and the electronic transformation of instrumental music in real time from the 1970s. Boulez conducted many of...',
  },
]

const paginatedResults = computed(() =>
  mockResults.slice(
    currentPage.value * maxResultsPerPage,
    (currentPage.value + 1) * maxResultsPerPage,
  ),
)

const totalPages = computed(() => Math.ceil(mockResults.length / maxResultsPerPage))

const setCurrentPage = (page) => {
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
