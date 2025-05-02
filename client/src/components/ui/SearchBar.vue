<template>
  <div class="container">
    <div class="search-bar">
      <div class="search-bar__form-wrapper">
        <img v-if="isIconShown" :src="iconSource" alt="icon" class="search-bar__dora-icon" />
        <form @submit.prevent="submitSearchQuery" class="search-bar__form">
          <div class="search-bar__input-group">
            <font-awesome-icon
              icon="fa-solid fa-microphone"
              class="search-bar__voice-icon"
              @click="voiceSearchQuery"
            />
            <input
              class="search-bar__input"
              id="search"
              type="search"
              placeholder="Explore your way!"
              v-model="searchQuery"
              @input="onQueryChange"
              @keyup="handleKeyNavigation($event)"
              @keydown="handleKeyNavigation($event)"
              autocomplete="off"
            />
          </div>
        </form>

        <base-button @button-click="submitSearchQuery">Explore!</base-button>
      </div>

      <ul v-if="showSuggestions" class="search__suggestions">
        <li
          v-for="(suggestion, index) in suggestions"
          :key="index"
          :class="{
            'search-bar__suggestion': true,
            'search-bar__suggestion--active': index === highlightedQueryIndex,
          }"
          @click="highlightedQueryIndex = index"
        >
          <font-awesome-icon icon="fa-solid fa-magnifying-glass" class="search-bar__search-icon" />
          <span class="search-bar__suggestion-text">{{ suggestion }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getSearchHistory } from '@/services/searchServices'

import BaseButton from './BaseButton.vue'

const props = defineProps({
  isIconShown: Boolean,
  defaultQuery: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const route = useRoute()

const searchQuery = ref('')
const suggestions = ref([])
const showSuggestions = ref(false)
const highlightedQueryIndex = ref(-1)

const selectedTheme = ref('dora')
const iconSource = ref(`/images/${selectedTheme.value}.png`)

document.documentElement.setAttribute('data-theme', selectedTheme.value)

const changeTheme = (theme) => {
  selectedTheme.value = theme
  document.documentElement.setAttribute('data-theme', selectedTheme.value)
  localStorage.setItem('theme', selectedTheme.value)
  iconSource.value = `/images/${selectedTheme.value}.png`
  console.log('Applied theme: ', selectedTheme.value, 'icon source', iconSource)
}

onMounted(() => {
  // Set theme
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) selectedTheme.value = savedTheme
  changeTheme(selectedTheme.value)
  
  // Set initial search query from props or URL
  if (props.defaultQuery) {
    searchQuery.value = props.defaultQuery
  } else if (route.query.q) {
    searchQuery.value = route.query.q
  }

  fetchSuggestions();
})

const submitSearchQuery = () => {
  console.log('search query entered: ' + searchQuery.value)

  if (searchQuery.value.length === 0)
    return;

  router.push({ path: '/search', query: { q: searchQuery.value } })

  showSuggestions.value = false
  suggestions.value = []
}

const fetchSuggestions = () => {
  getSearchHistory()
    .then((data) => {
      console.log('Fetched search history:', data)
      // return data
    })
    .catch((error) => {
      console.error('Error fetching search history:', error)
      return []
    })
  return ['temp 1', 'temp 2']
}

const handleKeyNavigation = (event) => {
  if (!Array.isArray(suggestions.value) || suggestions.value.length === 0) {
    return
  }

  if (event.key === 'ArrowDown' && highlightedQueryIndex.value < suggestions.value.length - 1) {
    highlightedQueryIndex.value++
  } else if (event.key === 'ArrowUp' && highlightedQueryIndex.value > -1) {
    highlightedQueryIndex.value--
  } else {
    return
  }
  const selectedQuery = suggestions.value[highlightedQueryIndex.value]

  if (selectedQuery) {
    searchQuery.value = selectedQuery
  }
}

const onQueryChange = async () => {
  const query = searchQuery.value.trim()

  if (query.length === 0) {
    showSuggestions.value = false
    suggestions.value = []
    return
  }

  suggestions.value = await fetchSuggestions(query)

  if (suggestions.value.length > 0) {
    showSuggestions.value = true
  }
}

const voiceSearchQuery = () => {
  console.log('voice search')
}
</script>

<style scoped>
.container { 
  position: relative;
}

.container,
.search-bar {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;}

.search-bar__form-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 1rem;
}

.search-bar__form {
  flex: 1;
  margin: 1rem 0;
}

.search-bar__input-group {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 10px 16px;
  border-radius: 5px;
  background-color: var(--search-bar-background);
  border: var(--divider-color) 1px solid;
  transition: background 0.25s;
}

.search-bar__input {
  flex: 1;
  color: #333;
  margin-left: 10px;
  outline: none;
  border: none;
  background: transparent;
  font-size: 1rem;
  padding: 5px;
}

.search-bar__input::placeholder {
  color: rgba(0, 0, 0, 0.5);
}

.search-bar__voice-icon {
  color: var(--accent-color);
  font-size: 1.1rem;
}

.search-bar__input::-webkit-search-cancel-button {
  -webkit-filter: grayscale(100%);
  filter: grayscale(100%);
}

base-button {
  white-space: nowrap;
  flex-shrink: 0;
}
.search__suggestions {
  width: 100%;
  list-style: none;
  background-color: var(--background-color);
  border: var(--divider-color) 1px solid;
  border-radius: 5px;
  padding: 10px 16px;
  margin: 0.6rem 0;
  position: absolute;

  top: 100%; 
  left: 0;
  right: 0;
  z-index: 10;
}

.search-bar__suggestion {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.5rem;
  border-radius: 5px;
  cursor: pointer;

}

.search-bar__suggestion--active {
  background-color: var(--search-bar-background);
  font-weight: 500;
}

.search-bar__search-icon {
  font-size: 0.9rem;
  color: var(--accent-color);
}

.search-bar__suggestion-text {
  flex: 1;
  font-size: 0.95rem;
}

.search-bar__dora-icon {
  width: 60px;
  height: auto;
  object-fit: contain;
}

@media (max-width: 650px) {
  .search-bar__form-wrapper {
    flex-direction: column;
    gap: 0.5rem;
  }

  .search-bar__form {
    width: 100%;
  }

  .search-bar__input {
    font-size: 0.9rem;
  }
}
</style>
