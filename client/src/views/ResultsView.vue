<template>
  <div class="container">
    <div class="search">
      <SearchBar />
    </div>
    <SearchResult
      v-for="(result, index) in mockResults"
      :key="index"
      :title="result.title"
      :url="result.url"
      :content="result.content"
    ></SearchResult>
  </div>
</template>

<script setup>
import SearchBar from '@/components/ui/SearchBar.vue'
import SearchResult from '@/components/ui/SearchResult.vue'

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
    content: 'Pierre Boulez (26 March 1925 – 5 January 2016) was a French composer and conductor. He was one of the dominant figures of post-war contemporary classical music.As a composer, he played a leading role in the development of integral serialism in the 1950s, and the electronic transformation of instrumental music in real time from the 1970s. Boulez conducted many of...'
  }
]
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 1000px;
  padding: 1rem;
  margin: 0 auto;
}

.search {
  width: 100%;
}

.results {
  margin: 1rem 2rem;
}
</style>
