<template>
  <div class="search">
    <SearchBar :is-icon-shown="true" />
  </div>

  <div class="divider"></div>

</template>

<script setup>
import SearchBar from '../ui/SearchBar.vue';
</script>

<style scoped>
.divider {
  border-bottom: 1px solid var(--divider-color);
  width: 100%; /* Full viewport width */
  margin-top: 2rem;
  margin-bottom: 2rem;
  overflow-x: hidden;
}

.search {
  margin-top: 1.5rem;
  width: 100%;
  overflow: visible;
}
</style>
