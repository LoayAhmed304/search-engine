<template>
  <div class="container">
    <img src="../assets/images/dora-the-explorer.png" alt="dora the explorer" />
    <SearchBar />
  </div>
</template>

<script setup>
import SearchBar from "../components/ui/SearchBar.vue"
</script>

<style scoped>
.container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  padding: 2rem;
  margin: 1rem;
}

img {
  max-width: 600px;
  height: auto;
  margin: 1rem;
}

@media (max-width: 650px) {
  img {
    width: 90%;
  }
}
</style>
