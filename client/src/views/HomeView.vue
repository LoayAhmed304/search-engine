<template>
  <div class="container">
    <img src="/images/dora-the-explorer.png" alt="dora the explorer"  />
    <SearchBar :is-icon-shown="false"/>
  </div>
  <TheFooter />
</template>

<script setup>
import SearchBar from '@/components/ui/SearchBar.vue'
</script>

<style scoped>
.container {
  display: flex;
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
