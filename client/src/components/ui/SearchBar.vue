<script setup>
import {ref } from "vue"
import BaseButton from "./BaseButton.vue";

const searchQuery = ref("");

const handleButtonClick = () => {
  console.log("search query entered: " + searchQuery.value);
}

const submitSearchQuery = () => {
  console.log("search query entered: " + searchQuery.value);
}
</script>

<template>
  <form @submit.prevent="submitSearchQuery" class="search-container">
    <div class="search">
      <font-awesome-icon icon="fa-solid fa-magnifying-glass" />
      <input class="search-input" id="search" type="search" placeholder="Explore your way!" v-model="searchQuery" />
    </div>
  </form>
  <base-button @button-click="handleButtonClick">
    Explore!
  </base-button>
</template>

<style scoped>
.search-container {
  width: 600px;
}

.search {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px 16px;
  border-radius: 5px;
  transition: background 0.25s;
  background-color: var(--search-bar-background);
  border: var(--divider-color) 1px solid;
}


.search-input {
  flex: 1;
  color: #333;
  margin-left: 10px;
  outline: none;
  border: none;
  background: transparent;
  font-size: 1rem;
  padding:5px;
}

.search-input::placeholder {
  color: rgba(0, 0, 0, 0.5);
}

.search-icon {
  color: rgba(0, 0, 0, 0.7);
}

.search-input::-webkit-search-cancel-button {
  -webkit-filter: grayscale(100%);
  filter: grayscale(100%);
}

@media (max-width: 650px) {
  .search-container {
    width: 90%;
  }

  .search-input {
    font-size: 0.9rem;
  }
}
</style>
