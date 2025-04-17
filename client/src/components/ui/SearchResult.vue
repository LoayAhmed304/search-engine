<template>
  <div class="search-result">
    <div class="search-result__info">
      <img src="@/assets/images/map.png" alt="icon" class="search-result__icon" />

      <div class="search-result__text">
        <a class="search-result__link" :href="url" target="_blank">{{ url }}</a>
        <a :href="url" target="_blank" class="search-result__title-link">
          <h1 class="search-result__title">{{ title }}</h1>
        </a>
      </div>
    </div>

    <p class="search-result__content" v-html="content"></p>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  url: String,
  content: String,
})
</script>

<style scoped>
.search-result {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  padding: 1rem 1.2rem;
}

.search-result__info {
  display: flex;
  align-items: center;
}

.search-result__icon {
  width: 50px;
  height: auto;
  object-fit: contain;
  margin-right: 1rem;
}

.search-result__text {
  display: flex;
  flex-direction: column;
  align-items: start;
  justify-content: center;
  gap: 0.2rem;
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
  font-size: 1.1rem;
  margin: 0;
  color: var(--title-color);
  font-weight: 500;
}

.search-result__link:hover,
.search-result__title-link:hover {
  text-decoration: underline;
}

.search-result__content {
  font-size: 0.9rem;
  color: #333;
  font-weight: 300;
  margin-top: 0.1rem;
  line-height: 1.5;
  padding: 0 1rem;
}
</style>
