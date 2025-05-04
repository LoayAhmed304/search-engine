import { ref, watch } from 'vue'

export function useTheme() {
  const themes = ["dora", "backpack", "swiper", "boots"]
  const isDropdownVisible = ref(false)
  const selectedTheme = ref("dora")
  
  // Initialize theme from localStorage or fallback to default
  const initTheme = () => {
    const savedTheme = localStorage.getItem("theme")
    if (savedTheme) selectedTheme.value = savedTheme
    applyTheme(selectedTheme.value)
  }
  
  const applyTheme = (theme) => {
    selectedTheme.value = theme
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }
  
  const toggleDropdown = () => {
    isDropdownVisible.value = !isDropdownVisible.value
  }
  
  // Watch for theme changes
  watch(selectedTheme, (newTheme) => {
    applyTheme(newTheme)
  })
  
  return {
    themes,
    selectedTheme,
    isDropdownVisible,
    toggleDropdown,
    applyTheme,
    initTheme
  }
}