<template>
  <div class="wrapper">
    <img src="/images/dora-the-explorer.png" alt="dora the explorer"  />
    <SearchBar :is-icon-shown="false"/>
  </div>
  <TheFooter />
</template>

<script setup>
import SearchBar from '@/components/ui/SearchBar.vue'
</script>

<style scoped>
.wrapper {
  display: flex;
  flex-direction: column;
  overflow: visible;
  margin-top: 6rem;
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
