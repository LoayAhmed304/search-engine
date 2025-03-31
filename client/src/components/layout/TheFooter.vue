<script setup>
import { ref, watch } from 'vue';

const themes = ["dora", "backpack", "swiper", "boots"];
const isDropdownVisible = ref(false);  

// default theme is 'dora'
const selectedTheme = ref(localStorage.getItem("theme") || "dora");

document.documentElement.setAttribute("data-theme", selectedTheme.value);

const changeTheme = (theme) => {
  selectedTheme.value = theme;  
  console.log("Applied theme: ", selectedTheme.value);
  document.documentElement.setAttribute('data-theme', selectedTheme.value);
  localStorage.setItem("theme", selectedTheme.value);
  isDropdownVisible.value = false;
}

const toggleDropdown = () => {
  isDropdownVisible.value = !isDropdownVisible.value;  
};

watch(selectedTheme, (newTheme) => {
  document.documentElement.setAttribute('data-theme', newTheme);
  localStorage.setItem("theme", newTheme);
});
</script>

<template>
  <footer>
    <div class="theme-selector">
      <font-awesome-icon icon="fa-solid fa-palette" class="palette-icon" @click="toggleDropdown" />

      <div v-if="isDropdownVisible" class="dropdown">
        <h4>Select theme</h4>
        <div v-for="theme in themes" :key="theme" @click="changeTheme(theme)">
          {{ theme }}
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
footer {
  position: fixed;
  bottom: 0;
  width: 90%;
  padding: 25px 15px;
  display: flex;
  justify-content: flex-end;
}

.dropdown {
  position: absolute;
  bottom: 100%;
  right: 0;
  background: var(--background-color);
  border: 1px solid var(--secondary-green-color);
  padding: 15px 20px;
  border-radius: 5px;
  width: 200px;
}

.dropdown div {
  padding: 5px;
  cursor: pointer;
}

.dropdown div:hover {
  background: var(--secondary-green-color);
    border-radius: 5px;
}

.palette-icon {
  font-size: 1.5rem;
  color: var(--accent-green-color);
}

h4{
  margin-bottom: 1rem;
}
</style>