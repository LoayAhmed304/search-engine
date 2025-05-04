<template>
  <div class="search-result">
    <div class="search-result__info">
      <img src="/images/map.png" alt="icon" class="search-result__icon" />

      <div class="search-result__text">
        <a class="search-result__link" :href="url" target="_blank">{{ url }}</a>
        <a :href="url" target="_blank" class="search-result__title-link">
          <h1 class="search-result__title">{{ title }}</h1>
        </a>
      </div>
    </div>

    <p class="search-result__content" v-html="snippet"></p>
  </div>
</template>

<script setup>
defineProps({
  url: String,
  title: String,
  snippet: String,
})
</script>

<style scoped>
.search-result {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1.4rem 0;
  width: 100%;
  word-wrap: break-word;
  overflow-wrap: break-word;
  transition: all 0.3s ease; 
  border-radius: 8px;
  border-left: 3px solid transparent;
}

.search-result:hover {
  background-color: rgba(0, 0, 0, 0.02);
  border-left: 3px solid var(--accent-color, #4285f4);
  padding-left: 10px;
}

.search-result__info {
  display: flex;
  align-items: center;
}

.search-result__icon {
  width: 50px;
  height: auto;
  object-fit: contain;
  margin-right: 0.5rem;
  transition: all 0.3s ease;
}

.search-result:hover .search-result__icon {
  animation: wiggle 1.5s ease-in-out infinite;
}

.search-result__text {
  display: flex;
  flex-direction: column;
  align-items: start;
  justify-content: center;
  font-weight: 200;
}

.search-result__link {
  text-decoration: none;
  font-size: 0.8rem;
  color: var(--link-color);
  word-break: break-word;
}

.search-result__title-link {
  text-decoration: none;
}

.search-result__title {
  font-size: 1.3rem;
  margin: 0;
  color: var(--title-color);
  font-weight: 550;
}

.search-result__link:hover,
.search-result__title-link:hover {
  text-decoration: underline;
  color: var(--accent-color);
}

.search-result__content {
  font-size: 0.9rem;
  color: #333;
  font-weight: 300;
  margin-top: 0.1rem;
  line-height: 1.5;
  padding: 0 0.8rem;
}

@keyframes wiggle {
  0% { transform: translateX(0); }
  25% { transform: translateX(4px); }
  50% { transform: translateX(0); }
  75% { transform: translateX(-4px); }
  100% { transform: translateX(0); }
}
</style>
