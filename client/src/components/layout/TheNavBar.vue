<template>
    <header>
        <div class="search-bar">
            <SearchBar/>
        </div>
    </header>
</template>

<script setup>
import SearchBar from '../ui/SearchBar.vue';
</script>

<style scoped>
header {
width: 100%;
    border: red 1px solid;
    margin-top: 1.5rem;
}
.search-bar {
    width: 60%;
}
</style>
