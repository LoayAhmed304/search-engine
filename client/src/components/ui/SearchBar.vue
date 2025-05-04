<template>
  <div class="container">
    <div class="search-bar">
      <div class="theme-selector">
        <font-awesome-icon 
          icon="fa-solid fa-palette" 
          class="theme-selector__palette-icon" 
          @click="toggleDropdown" 
        />

        <div v-if="isDropdownVisible" class="theme-selector__dropdown">
          <h4>Select theme</h4>
          <div 
            v-for="theme in themes" 
            :key="theme" 
            @click="applyTheme(theme)"
            :class="{ 'active-theme': selectedTheme === theme }"
          >
            {{ theme }}
          </div>
        </div>
      </div>

      <div class="search-bar__form-wrapper">
        <img v-if="isIconShown" :src="iconSource" alt="icon" class="search-bar__dora-icon" @click="goHome()" />
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
          @click="selectSuggestion(index)"
        >
          <font-awesome-icon icon="fa-solid fa-magnifying-glass" class="search-bar__search-icon" />
          <span class="search-bar__suggestion-text">{{ suggestion }}</span>
        </li>
      </ul>
    </div>
  </div>
  <div v-html="snippet"></div>

</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getSearchHistory } from '@/services/searchServices'
import { useTheme } from '@/composables/useTheme'
import BaseButton from './BaseButton.vue'

// Import theme composable
const { 
  themes, 
  selectedTheme, 
  isDropdownVisible, 
  toggleDropdown, 
  applyTheme, 
  initTheme 
} = useTheme()

// Rest of your existing setup code
const props = defineProps({
  isIconShown: Boolean,
  defaultQuery: {
    type: String,
    default: ''
  }
})

// Update iconSource ref to use the selectedTheme from composable
const iconSource = ref(`/images/${selectedTheme.value}.png`)

// Watch for theme changes to update icon
watch(selectedTheme, (newTheme) => {
  iconSource.value = `/images/${newTheme}.png`
})

const router = useRouter()
const route = useRoute()

const searchQuery = ref('')
const allSuggestions = ref([]) 
const suggestions = ref([]) // Store filtered suggestions based on user input
const showSuggestions = ref(false)
const highlightedQueryIndex = ref(-1)

const snippet = ref('')

onMounted(() => {
  // Initialize theme
  initTheme()
  
  // Rest of your onMounted code
  if (props.defaultQuery) {
    searchQuery.value = props.defaultQuery
  } else if (route.query.q) {
    searchQuery.value = route.query.q
  }

  fetchSuggestions()
  
  // Add click outside to close dropdown
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

// Add handler to close dropdown when clicking outside
const handleClickOutside = (event) => {
  const themeSelector = document.querySelector('.theme-selector')
  if (themeSelector && !themeSelector.contains(event.target) && isDropdownVisible.value) {
    isDropdownVisible.value = false
  }
}

const goHome = () => {
  router.push({ path: '/' })
}

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
      allSuggestions.value = data;
      // console.log('Fetched search history:', allSuggestions.value);
    })
    .catch((error) => {
      console.error('Error fetching search history:', error);
    })
}

const handleKeyNavigation = (event) => {
  if (!Array.isArray(suggestions.value) || suggestions.value.length === 0) {
    return
  }

  if (event.key === 'ArrowDown' && highlightedQueryIndex.value < suggestions.value.length - 1) {
    highlightedQueryIndex.value++
  } else if (event.key === 'ArrowUp' && highlightedQueryIndex.value > -1) {
    highlightedQueryIndex.value--
  } else if (event.key === 'Enter' && highlightedQueryIndex.value >= 0) {
    // Select the highlighted suggestion on Enter
    searchQuery.value = suggestions.value[highlightedQueryIndex.value]
    submitSearchQuery()
    return
  } else {
    return
  }
  
  // Update search query to the highlighted suggestion
  if (highlightedQueryIndex.value >= 0) {
    const selectedQuery = suggestions.value[highlightedQueryIndex.value]
    if (selectedQuery) {
      searchQuery.value = selectedQuery
    }
  }
}

const onQueryChange = () => {
  const query = searchQuery.value.trim().toLowerCase()
  highlightedQueryIndex.value = -1 // Reset highlight when query changes
  
  if (query.length === 0) {
    showSuggestions.value = false
    suggestions.value = []
    return
  }

  // Filter suggestions based on current input
  suggestions.value = allSuggestions.value.filter(suggestion => 
    suggestion.toLowerCase().includes(query)
  ).slice(0, 10)
  
  showSuggestions.value = suggestions.value.length > 0
}

const selectSuggestion = (index) => {
  searchQuery.value = suggestions.value[index]
  submitSearchQuery()
  showSuggestions.value = false
}

const voiceSearchQuery = () => {
  console.log('voice search')
}
</script>

<style scoped>
.theme-selector {
  position: absolute;
  top: 50%; 
  left: -200px;
  transform: translateY(-50%);
  z-index: 20;
}

.theme-selector__palette-icon {
  font-size: 2.5rem;
  color: var(--accent-color);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.theme-selector__palette-icon:hover {
  transform: rotate(15deg);
}

.theme-selector__dropdown {
  position: absolute;
  top: 100%; 
  left: -15px; 
  background: var(--background-color);
  border: 1px solid var(--secondary-green-color);
  padding: 15px 20px;
  border-radius: 5px;
  width: 180px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
  z-index: 30;
}

.theme-selector__dropdown h4 {
  margin-bottom: 10px;
  color: var(--accent-color);
}

.theme-selector__dropdown div {
  padding: 8px 10px;
  margin: 3px 0;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s ease;
  text-transform: capitalize;
}

.theme-selector__dropdown div:hover {
  background: var(--search-bar-background);
}

.theme-selector__dropdown div.active-theme {
  background: var(--accent-color);
  color: white;
}

.container { 
  position: relative;
  padding-left: 20px; 
  max-width: 840px;
  margin: 0 auto;
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
  cursor: pointer;
}

.search-bar__dora-icon:hover {
  cursor: pointer;
  transform: scale(1.5);
  transition: transform 0.2s;
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

.search-result {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.pagination__page {
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: all 0.2s ease;
}

.pagination__page:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.pagination__page--active {
  background-color: var(--accent-color);
  color: white !important;
}

.search-result {
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.search-result:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
</style>
